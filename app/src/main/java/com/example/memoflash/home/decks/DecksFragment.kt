package com.example.memoflash.home.decks

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
import com.example.memoflash.databinding.FragmentDecksBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class DecksFragment : Fragment(R.layout.fragment_decks) {
    private var _binding: FragmentDecksBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<DecksViewModel>()
    private val adapter = DecksAdapter(::openDeck)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDecksBinding.bind(view)
        binding.rvDecks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDecks.adapter = adapter
        binding.btnCreateDeck.setOnClickListener {
            findNavController().navigate(R.id.action_decksFragment_to_addDeckFragment)
        }
        observeDecks()
        viewModel.loadDecks()
    }

    private fun observeDecks() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deckState.collect { state ->
                    binding.deckProgress.visibility =
                        if (state is ResponseService.Loading) View.VISIBLE else View.GONE
                    when (state) {
                        is ResponseService.Success -> {
                            adapter.submitList(state.data)
                            binding.txtEmptyDecks.visibility =
                                if (state.data.isEmpty()) View.VISIBLE else View.GONE
                        }
                        is ResponseService.Error -> {
                            Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show()
                        }
                        ResponseService.Loading, null -> Unit
                    }
                }
            }
        }
    }

    private fun openDeck(deck: com.example.memoflash.core.model.StudyDeck) {
        val arguments = Bundle().apply { putString("deckId", deck.id) }
        findNavController().navigate(
            R.id.action_decksFragment_to_deckDetailFragment,
            arguments
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvDecks.adapter = null
        _binding = null
    }
}
