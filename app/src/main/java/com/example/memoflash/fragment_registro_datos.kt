package com.example.memoflash

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.memoflash.core.FragmentCommunicator
import com.example.memoflash.core.ResponseService
import com.example.memoflash.databinding.FragmentRegistroDatosBinding
import com.example.memoflash.home.HomeActivity
import com.example.memoflash.onboarding.personal.PersonalInfoViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Calendar

class fragment_registro_datos : Fragment(R.layout.fragment_registro_datos) {
    private var _binding: FragmentRegistroDatosBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<PersonalInfoViewModel>()
    private lateinit var communicator: FragmentCommunicator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegistroDatosBinding.bind(view)
        communicator = requireActivity() as FragmentCommunicator

        setupValidation()
        setupDatePicker()
        setupClickListeners()
        observeState()
    }

    private fun setupValidation() {
        binding.btnFinalizarRegistro.isEnabled = false
        binding.editNombreInfo.editText?.addTextChangedListener { validateAndEnable() }
        binding.editApellidosInfo.editText?.addTextChangedListener { validateAndEnable() }
        binding.editUsuarioInfo.editText?.addTextChangedListener { validateAndEnable() }
        binding.editTelefonoInfo.editText?.addTextChangedListener { validateAndEnable() }
        binding.editFechaInfo.editText?.addTextChangedListener { validateAndEnable() }
    }

    private fun validateAndEnable() {
        val firstName = binding.editNombreInfo.editText?.text?.toString()?.trim().orEmpty()
        val lastName = binding.editApellidosInfo.editText?.text?.toString()?.trim().orEmpty()
        val username = binding.editUsuarioInfo.editText?.text?.toString()?.trim().orEmpty()
        val phone = binding.editTelefonoInfo.editText?.text?.toString()?.trim().orEmpty()
        val birthDate = binding.editFechaInfo.editText?.text?.toString()?.trim().orEmpty()

        binding.editNombreInfo.error = viewModel.validateFirstName(firstName)
        binding.editApellidosInfo.error = viewModel.validateLastName(lastName)
        binding.editUsuarioInfo.error = viewModel.validateUsername(username)
        binding.editTelefonoInfo.error = viewModel.validatePhone(phone)
        binding.editFechaInfo.error = viewModel.validateBirthDate(birthDate)
        binding.btnFinalizarRegistro.isEnabled =
            viewModel.isFormValid(firstName, lastName, username, phone, birthDate)
    }

    private fun setupDatePicker() {
        binding.editFechaInfo.editText?.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val formatted = "%04d-%02d-%02d".format(year, month + 1, day)
                    binding.editFechaInfo.editText?.setText(formatted)
                },
                cal.get(Calendar.YEAR) - 18,
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.maxDate = System.currentTimeMillis()
            }.show()
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnFinalizarRegistro.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.session_expired),
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            viewModel.saveProfile(
                uid = uid,
                firstName = binding.editNombreInfo.editText?.text?.toString()?.trim().orEmpty(),
                lastName = binding.editApellidosInfo.editText?.text?.toString()?.trim().orEmpty(),
                username = binding.editUsuarioInfo.editText?.text?.toString()?.trim().orEmpty(),
                phone = binding.editTelefonoInfo.editText?.text?.toString()?.trim().orEmpty(),
                birthDate = binding.editFechaInfo.editText?.text?.toString()?.trim().orEmpty()
            )
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveState.collect { state ->
                    when (state) {
                        is ResponseService.Loading -> {
                            communicator.manageLoader(true)
                            binding.btnFinalizarRegistro.isEnabled = false
                        }
                        is ResponseService.Success -> {
                            communicator.manageLoader(false)
                            val intent = Intent(requireContext(), HomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        is ResponseService.Error -> {
                            communicator.manageLoader(false)
                            binding.btnFinalizarRegistro.isEnabled = true
                            Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show()
                        }
                        null -> Unit
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
