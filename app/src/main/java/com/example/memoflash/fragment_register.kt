package com.example.memoflash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.memoflash.core.FragmentCommunicator
import com.example.memoflash.core.ResponseService
import com.example.memoflash.databinding.FragmentRegisterBinding
import com.example.memoflash.onboarding.signup.RegisterViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class fragment_register : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<RegisterViewModel>()
    private lateinit var communicator: FragmentCommunicator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        communicator = requireActivity() as FragmentCommunicator
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupValidation()
        setupClickListeners()
        observeState()
    }

    private fun setupValidation() {
        binding.btnRegistrar.isEnabled = false
        binding.editTextText.editText?.addTextChangedListener { validateAndEnable() }
        binding.editTextCorreo.editText?.addTextChangedListener { validateAndEnable() }
        binding.editTextContrasena.editText?.addTextChangedListener { validateAndEnable() }
        binding.confirmPasswordTil.editText?.addTextChangedListener { validateAndEnable() }
    }

    private fun validateAndEnable() {
        val name = binding.editTextText.editText?.text?.toString()?.trim().orEmpty()
        val email = binding.editTextCorreo.editText?.text?.toString()?.trim().orEmpty()
        val password = binding.editTextContrasena.editText?.text?.toString()?.trim().orEmpty()
        val confirm = binding.confirmPasswordTil.editText?.text?.toString()?.trim().orEmpty()

        binding.editTextText.error = viewModel.validateName(name)
        binding.editTextCorreo.error = viewModel.validateEmail(email)
        binding.editTextContrasena.error = viewModel.validatePassword(password)
        binding.confirmPasswordTil.error = viewModel.validateConfirmPassword(password, confirm)
        binding.btnRegistrar.isEnabled =
            viewModel.isRegisterFormValid(name, email, password, confirm)
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnRegistrar.setOnClickListener {
            val name = binding.editTextText.editText?.text?.toString()?.trim().orEmpty()
            val email = binding.editTextCorreo.editText?.text?.toString()?.trim().orEmpty()
            val password = binding.editTextContrasena.editText?.text?.toString()?.trim().orEmpty()
            viewModel.requestSignUp(name, email, password)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registerState.collect { state ->
                    when (state) {
                        is ResponseService.Loading -> {
                            communicator.manageLoader(true)
                            binding.btnRegistrar.isEnabled = false
                        }
                        is ResponseService.Success -> {
                            communicator.manageLoader(false)
                            findNavController().navigate(R.id.action_fragment_register_to_fragment_infoPersonal)
                        }
                        is ResponseService.Error -> {
                            communicator.manageLoader(false)
                            binding.btnRegistrar.isEnabled = true
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
