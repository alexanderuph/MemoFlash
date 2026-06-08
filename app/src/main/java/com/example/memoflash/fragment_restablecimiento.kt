package com.example.memoflash

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.memoflash.core.ResponseService
import com.example.memoflash.databinding.FragmentRestablecimientoBinding
import com.example.memoflash.onboarding.recovery.PasswordRecoveryViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class fragment_restablecimiento : Fragment(R.layout.fragment_restablecimiento) {
    private var _binding: FragmentRestablecimientoBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<PasswordRecoveryViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRestablecimientoBinding.bind(view)

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.txtVolverLogin.setOnClickListener {
            findNavController().popBackStack(R.id.fragment_login, false)
        }
        binding.editCorreoRecuperar.editText?.addTextChangedListener {
            validateEmail(showError = false)
        }
        binding.btnEnviarInstrucciones.setOnClickListener {
            if (validateEmail(showError = true)) {
                viewModel.sendInstructions(email())
            }
        }
        observeResetState()
    }

    private fun validateEmail(showError: Boolean): Boolean {
        val valid = viewModel.isValidEmail(email())
        binding.editCorreoRecuperar.error =
            if (!valid && showError) getString(R.string.invalid_email) else null
        binding.btnEnviarInstrucciones.isEnabled = valid
        return valid
    }

    private fun email(): String =
        binding.editCorreoRecuperar.editText?.text?.toString().orEmpty()

    private fun observeResetState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.resetState.collect { state ->
                    val loading = state is ResponseService.Loading
                    binding.recoveryProgress.visibility =
                        if (loading) View.VISIBLE else View.GONE
                    binding.btnEnviarInstrucciones.isEnabled =
                        !loading && viewModel.isValidEmail(email())
                    when (state) {
                        is ResponseService.Success -> Snackbar.make(
                            binding.root,
                            getString(R.string.recovery_sent, email().trim()),
                            Snackbar.LENGTH_LONG
                        ).show()
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
        _binding = null
    }
}
