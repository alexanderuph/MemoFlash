package com.example.memoflash.home.detail

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memoflash.R
import com.example.memoflash.core.ResponseService
import com.example.memoflash.core.model.StudyDeck
import com.example.memoflash.databinding.FragmentDeckDetailBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class DeckDetailFragment : Fragment(R.layout.fragment_deck_detail) {
    private var _binding: FragmentDeckDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<DeckDetailViewModel>()
    private val adapter = FlashcardsAdapter()
    private var currentDeck: StudyDeck? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDeckDetailBinding.bind(view)
        val deckId = requireArguments().getString("deckId").orEmpty()
        binding.btnBackDetail.setOnClickListener { findNavController().navigateUp() }
        binding.btnEditDeck.setOnClickListener {
            currentDeck?.let { deck ->
                findNavController().navigate(
                    R.id.action_deckDetailFragment_to_addDeckFragment,
                    Bundle().apply { putString("deckId", deck.id) }
                )
            }
        }
        binding.btnStudyDeck.setOnClickListener {
            currentDeck?.let { deck ->
                findNavController().navigate(
                    R.id.action_deckDetailFragment_to_studySessionFragment,
                    Bundle().apply { putString("deckId", deck.id) }
                )
            }
        }
        binding.btnDeleteDeck.setOnClickListener {
            currentDeck?.let { deck -> viewModel.deleteDeck(deck.id) }
        }
        binding.rvFlashcards.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFlashcards.adapter = adapter
        observeDeck()
        observeDelete()
        viewModel.loadDeck(deckId)
    }

    private fun observeDeck() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deckState.collect { state ->
                    binding.detailProgress.visibility =
                        if (state is ResponseService.Loading) View.VISIBLE else View.GONE
                    when (state) {
                        is ResponseService.Success -> renderDeck(state.data)
                        is ResponseService.Error ->
                            Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show()
                        ResponseService.Loading, null -> Unit
                    }
                }
            }
        }
    }

    private fun renderDeck(deck: StudyDeck) {
        currentDeck = deck
        binding.txtDetailTitle.text = deck.title
        binding.txtDetailSubject.text = deck.subject
        binding.txtDetailDescription.text = deck.description
        binding.txtDetailSource.visibility =
            if (deck.source.isBlank()) View.GONE else View.VISIBLE
        binding.txtDetailSource.text = getString(R.string.deck_source_format, deck.source)
        binding.txtDetailCount.text = resources.getQuantityString(
            R.plurals.deck_card_count,
            deck.cards.size,
            deck.cards.size
        )
        adapter.submitList(deck.cards)
        binding.btnDeleteDeck.visibility =
            if (
                deck.ownerId.isNotBlank() &&
                deck.ownerId == FirebaseAuth.getInstance().currentUser?.uid
            ) View.VISIBLE else View.GONE
        binding.btnEditDeck.visibility = binding.btnDeleteDeck.visibility
    }

    private fun observeDelete() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deleteState.collect { state ->
                    binding.btnDeleteDeck.isEnabled = state !is ResponseService.Loading
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvFlashcards.adapter = null
        _binding = null
    }
}
