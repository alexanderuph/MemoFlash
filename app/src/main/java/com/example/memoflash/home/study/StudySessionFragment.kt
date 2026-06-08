package com.example.memoflash.home.study

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.memoflash.R
import com.example.memoflash.core.ResponseService
import com.example.memoflash.databinding.FragmentStudySessionBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class StudySessionFragment : Fragment(R.layout.fragment_study_session) {
    private var _binding: FragmentStudySessionBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<StudySessionViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStudySessionBinding.bind(view)
        binding.btnCloseStudy.setOnClickListener { findNavController().navigateUp() }
        binding.btnRevealAnswer.setOnClickListener { viewModel.revealAnswer() }
        binding.btnPreviousCard.setOnClickListener { viewModel.previousCard() }
        binding.btnNextCard.setOnClickListener { viewModel.nextCard() }
        observeState()
        viewModel.load(requireArguments().getString("deckId").orEmpty())
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.loadState.collect { state ->
                        binding.studySessionProgress.visibility =
                            if (state is ResponseService.Loading) View.VISIBLE else View.GONE
                        if (state is ResponseService.Error) {
                            Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
                launch {
                    viewModel.session.collect { state ->
                        state ?: return@collect
                        val card = state.deck.cards[state.cardIndex]
                        binding.txtStudyDeckTitle.text = state.deck.title
                        binding.txtStudyProgress.text = getString(
                            R.string.study_progress_format,
                            state.cardIndex + 1,
                            state.deck.cards.size
                        )
                        binding.txtStudyQuestion.text = card.question
                        binding.txtStudyAnswer.text = card.answer
                        binding.txtStudyAnswer.visibility =
                            if (state.answerVisible) View.VISIBLE else View.GONE
                        binding.btnRevealAnswer.visibility =
                            if (state.answerVisible) View.GONE else View.VISIBLE
                        binding.btnPreviousCard.isEnabled = state.cardIndex > 0
                        binding.btnNextCard.isEnabled =
                            state.cardIndex < state.deck.cards.lastIndex
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
