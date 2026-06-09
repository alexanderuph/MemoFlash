package com.example.memoflash.home.agregar

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memoflash.R
import com.example.memoflash.core.ResponseService
import com.example.memoflash.core.model.Flashcard
import com.example.memoflash.core.model.StudyDeck
import com.example.memoflash.databinding.FragmentAgregarBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class AddDeckFragment : Fragment(R.layout.fragment_agregar) {
    private var _binding: FragmentAgregarBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<AddDeckViewModel>()
    private val adapter = EditableCardsAdapter(::editCard, ::deleteCard)
    private var editingCardId: String? = null
    private var deckRendered = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAgregarBinding.bind(view)
        binding.rvEditableCards.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEditableCards.adapter = adapter
        setupListeners()
        observeState()
        arguments?.getString(ARG_DECK_ID).orEmpty().let(viewModel::loadDeck)
    }

    private fun setupListeners() {
        binding.btnBackAdd.setOnClickListener { findNavController().navigateUp() }
        listOf(
            binding.inputDeckTitle,
            binding.inputDeckSubject,
            binding.inputDeckDescription
        ).forEach { layout ->
            layout.editText?.addTextChangedListener { validateDeck() }
        }
        binding.btnAddCard.setOnClickListener {
            val question = binding.inputCardQuestion.editText?.text?.toString().orEmpty()
            val answer = binding.inputCardAnswer.editText?.text?.toString().orEmpty()
            binding.inputCardQuestion.error = viewModel.validateQuestion(question)
            binding.inputCardAnswer.error = viewModel.validateAnswer(answer)
            if (viewModel.addOrUpdateCard(editingCardId, question, answer)) {
                clearCardEditor()
            }
        }
        binding.btnCancelCardEdit.setOnClickListener { clearCardEditor() }
        binding.btnSaveDeck.setOnClickListener {
            viewModel.saveDeck(title(), subject(), description())
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.cards.collect {
                        adapter.submitList(it)
                        binding.txtNoCards.visibility =
                            if (it.isEmpty()) View.VISIBLE else View.GONE
                        validateDeck()
                    }
                }
                launch {
                    viewModel.deckState.collect { state ->
                        binding.editorProgress.visibility =
                            if (state is ResponseService.Loading) View.VISIBLE else View.GONE
                        when (state) {
                            is ResponseService.Success -> renderDeck(state.data)
                            is ResponseService.Error ->
                                Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show()
                            ResponseService.Loading, null -> Unit
                        }
                    }
                }
                launch {
                    viewModel.saveState.collect { state ->
                        binding.saveProgress.visibility =
                            if (state is ResponseService.Loading) View.VISIBLE else View.GONE
                        when (state) {
                            is ResponseService.Success -> findNavController().navigateUp()
                            is ResponseService.Error ->
                                Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show()
                            ResponseService.Loading, null -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun renderDeck(deck: StudyDeck) {
        if (deckRendered) return
        deckRendered = true
        binding.txtEditorTitle.setText(R.string.edit_deck_title)
        binding.inputDeckTitle.editText?.setText(deck.title)
        binding.inputDeckSubject.editText?.setText(deck.subject)
        binding.inputDeckDescription.editText?.setText(deck.description)
    }

    private fun editCard(card: Flashcard) {
        editingCardId = card.id
        binding.inputCardQuestion.editText?.setText(card.question)
        binding.inputCardAnswer.editText?.setText(card.answer)
        binding.btnAddCard.setText(R.string.update_card)
        binding.btnCancelCardEdit.visibility = View.VISIBLE
        binding.inputCardQuestion.requestFocus()
    }

    private fun deleteCard(card: Flashcard) {
        viewModel.removeCard(card.id)
        if (editingCardId == card.id) clearCardEditor()
    }

    private fun clearCardEditor() {
        editingCardId = null
        binding.inputCardQuestion.editText?.text?.clear()
        binding.inputCardAnswer.editText?.text?.clear()
        binding.inputCardQuestion.error = null
        binding.inputCardAnswer.error = null
        binding.btnAddCard.setText(R.string.add_card)
        binding.btnCancelCardEdit.visibility = View.GONE
    }

    private fun validateDeck() {
        binding.inputDeckTitle.error =
            title().takeIf { it.isNotBlank() }?.let(viewModel::validateTitle)
        binding.inputDeckSubject.error =
            subject().takeIf { it.isNotBlank() }?.let(viewModel::validateSubject)
        binding.inputDeckDescription.error =
            description().takeIf { it.isNotBlank() }?.let(viewModel::validateDescription)
        binding.btnSaveDeck.isEnabled = viewModel.canSave(title(), subject(), description())
    }

    private fun title() = binding.inputDeckTitle.editText?.text?.toString().orEmpty()
    private fun subject() = binding.inputDeckSubject.editText?.text?.toString().orEmpty()
    private fun description() =
        binding.inputDeckDescription.editText?.text?.toString().orEmpty()

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvEditableCards.adapter = null
        _binding = null
    }

    private companion object {
        const val ARG_DECK_ID = "deckId"
    }
}
