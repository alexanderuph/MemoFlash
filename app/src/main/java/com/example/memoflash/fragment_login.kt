package com.example.memoflash

import android.content.Intent
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
import com.example.memoflash.databinding.FragmentLoginBinding
import com.example.memoflash.home.HomeActivity
import com.example.memoflash.onboarding.signIn.SignInViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class fragment_login : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<SignInViewModel>()
    private lateinit var communicator: FragmentCommunicator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
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
        binding.btnIngresar.isEnabled = false
        binding.editTextCorreo.editText?.addTextChangedListener { validateAndEnable() }
        binding.editTextContrasena.editText?.addTextChangedListener { validateAndEnable() }
    }

    private fun validateAndEnable() {
        val email = binding.editTextCorreo.editText?.text?.toString()?.trim().orEmpty()
        val password = binding.editTextContrasena.editText?.text?.toString()?.trim().orEmpty()

        binding.editTextCorreo.error = viewModel.validateEmail(email)
        binding.editTextContrasena.error = viewModel.validatePassword(password)
        binding.btnIngresar.isEnabled = viewModel.isLoginFormValid(email, password)
    }

    private fun setupClickListeners() {
        binding.btnIngresar.setOnClickListener {
            val email = binding.editTextCorreo.editText?.text?.toString()?.trim().orEmpty()
            val password = binding.editTextContrasena.editText?.text?.toString()?.trim().orEmpty()
            viewModel.requestLogin(email, password)
        }

        binding.txtRegistrarse.setOnClickListener {
            findNavController().navigate(R.id.action_fragment_login_to_fragment_register)
        }

        binding.txtRestablecer.setOnClickListener {
            findNavController().navigate(R.id.action_fragment_login_to_fragment_recuperar)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.signInState.collect { state ->
                    when (state) {
                        is ResponseService.Loading -> {
                            communicator.manageLoader(true)
                            binding.btnIngresar.isEnabled = false
                        }
                        is ResponseService.Success -> {
                            communicator.manageLoader(false)
                            val intent = Intent(requireContext(), HomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        is ResponseService.Error -> {
                            communicator.manageLoader(false)
                            binding.btnIngresar.isEnabled = true
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
