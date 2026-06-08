package com.example.memoflash.home.agregar

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.memoflash.R
import com.example.memoflash.core.ResponseService
import com.example.memoflash.databinding.FragmentAgregarBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class AddDeckFragment : Fragment(R.layout.fragment_agregar) {
    private var _binding: FragmentAgregarBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<AddDeckViewModel>()
    private var selectedFileName = ""

    private val documentPicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            selectedFileName = resolveFileName(it)
            binding.txtSelectedFile.text = selectedFileName
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAgregarBinding.bind(view)
        setupListeners()
        observeSaveState()
    }

    private fun setupListeners() {
        binding.btnBackAdd.setOnClickListener { findNavController().navigateUp() }
        binding.btnChooseFile.setOnClickListener {
            documentPicker.launch(
                arrayOf(
                    "application/pdf",
                    "text/plain",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                )
            )
        }
        listOf(
            binding.inputDeckTitle,
            binding.inputDeckSubject,
            binding.inputDeckDescription,
            binding.inputCardQuestion,
            binding.inputCardAnswer
        ).forEach { layout ->
            layout.editText?.addTextChangedListener { validateForm() }
        }
        binding.btnSaveDeck.setOnClickListener {
            val values = formValues()
            validateForm()
            viewModel.saveDeck(
                values.title,
                values.subject,
                values.description,
                selectedFileName,
                values.question,
                values.answer
            )
        }
    }

    private fun validateForm() {
        val values = formValues()
        binding.inputDeckTitle.error = viewModel.validateTitle(values.title)
        binding.inputDeckSubject.error = viewModel.validateSubject(values.subject)
        binding.inputDeckDescription.error = viewModel.validateDescription(values.description)
        binding.inputCardQuestion.error = viewModel.validateQuestion(values.question)
        binding.inputCardAnswer.error = viewModel.validateAnswer(values.answer)
        binding.btnSaveDeck.isEnabled = viewModel.isValid(
            values.title,
            values.subject,
            values.description,
            values.question,
            values.answer
        )
    }

    private fun formValues() = DeckForm(
        title = binding.inputDeckTitle.editText?.text?.toString().orEmpty(),
        subject = binding.inputDeckSubject.editText?.text?.toString().orEmpty(),
        description = binding.inputDeckDescription.editText?.text?.toString().orEmpty(),
        question = binding.inputCardQuestion.editText?.text?.toString().orEmpty(),
        answer = binding.inputCardAnswer.editText?.text?.toString().orEmpty()
    )

    private fun observeSaveState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveState.collect { state ->
                    binding.saveProgress.visibility =
                        if (state is ResponseService.Loading) View.VISIBLE else View.GONE
                    when (state) {
                        is ResponseService.Success -> {
                            Snackbar.make(
                                binding.root,
                                R.string.deck_saved,
                                Snackbar.LENGTH_SHORT
                            ).show()
                            findNavController().navigateUp()
                        }
                        is ResponseService.Error ->
                            Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show()
                        ResponseService.Loading, null -> Unit
                    }
                }
            }
        }
    }

    private fun resolveFileName(uri: Uri): String {
        var name = getString(R.string.selected_document)
        requireContext().contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            }
        }
        return name
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private data class DeckForm(
        val title: String,
        val subject: String,
        val description: String,
        val question: String,
        val answer: String
    )
}
