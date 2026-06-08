package com.example.memoflash.home.study

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.memoflash.databinding.FragmentStudyBinding
import com.example.memoflash.home.decks.DecksAdapter
import com.example.memoflash.home.decks.DecksViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class StudyFragment : Fragment(R.layout.fragment_study) {
    private var _binding: FragmentStudyBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<DecksViewModel>()
    private val adapter = DecksAdapter(::openDeck)

    private val filePicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@registerForActivityResult
        val currentBinding = _binding ?: return@registerForActivityResult
        currentBinding.txtSelectedStudyFile.text = getString(
            R.string.selected_file_format,
            displayName(uri)
        )
        currentBinding.txtSelectedStudyFile.visibility = View.VISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStudyBinding.bind(view)
        binding.rvFeaturedDecks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFeaturedDecks.adapter = adapter
        binding.btnUploadFile.setOnClickListener {
            filePicker.launch(
                arrayOf(
                    "application/pdf",
                    "text/plain",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                )
            )
        }
        observeDecks()
        viewModel.loadDecks()
    }

    private fun observeDecks() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deckState.collect { state ->
                    binding.studyProgress.visibility =
                        if (state is ResponseService.Loading) View.VISIBLE else View.GONE
                    when (state) {
                        is ResponseService.Success -> renderDecks(state.data)
                        is ResponseService.Error -> {
                            binding.txtEmptyStudy.visibility = View.VISIBLE
                            Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show()
                        }
                        ResponseService.Loading, null -> Unit
                    }
                }
            }
        }
    }

    private fun renderDecks(decks: List<StudyDeck>) {
        adapter.submitList(decks)
        binding.txtCardsReady.text = getString(
            R.string.number_format,
            decks.sumOf { it.cards.size }
        )
        binding.txtEmptyStudy.visibility = if (decks.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun openDeck(deck: StudyDeck) {
        findNavController().navigate(
            R.id.action_studyFragment_to_deckDetailFragment,
            Bundle().apply { putString(ARG_DECK_ID, deck.id) }
        )
    }

    private fun displayName(uri: Uri): String {
        requireContext().contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                return cursor.getString(nameIndex)
            }
        }
        return getString(R.string.selected_document)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvFeaturedDecks.adapter = null
        _binding = null
    }

    private companion object {
        const val ARG_DECK_ID = "deckId"
    }
}
