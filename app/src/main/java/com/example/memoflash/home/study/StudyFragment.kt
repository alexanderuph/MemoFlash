package com.example.memoflash.home.study

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStudyBinding.bind(view)
        binding.rvFeaturedDecks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFeaturedDecks.adapter = adapter
        binding.btnUploadFile.setOnClickListener {
            findNavController().navigate(R.id.action_studyFragment_to_addDeckFragment)
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
                        is ResponseService.Success -> {
                            val decks = state.data.take(4)
                            adapter.submitList(decks)
                            binding.txtCardsReady.text = state.data.sumOf { it.cards.size }.toString()
                            binding.txtEmptyStudy.visibility =
                                if (decks.isEmpty()) View.VISIBLE else View.GONE
                        }
                        is ResponseService.Error ->
                            Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show()
                        ResponseService.Loading, null -> Unit
                    }
                }
            }
        }
    }

    private fun openDeck(deck: StudyDeck) {
        val arguments = Bundle().apply { putString("deckId", deck.id) }
        findNavController().navigate(
            R.id.action_studyFragment_to_deckDetailFragment,
            arguments
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvFeaturedDecks.adapter = null
        _binding = null
    }
}
