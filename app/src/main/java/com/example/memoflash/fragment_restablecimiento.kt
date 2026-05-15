package com.example.memoflash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.memoflash.databinding.FragmentRestablecimientoBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class fragment_restablecimiento : Fragment(R.layout.fragment_restablecimiento) {
    private var _binding: FragmentRestablecimientoBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRestablecimientoBinding.bind(view)
        val auth = FirebaseAuth.getInstance()

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnEnviarInstrucciones.setOnClickListener {
            val email = binding.editCorreoRecuperar.editText?.text?.toString()?.trim().orEmpty()
            if (email.isBlank()) {
                Snackbar.make(
                    binding.root,
                    "Escribe tu correo para recuperar tu contraseña",
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Snackbar.make(
                        binding.root,
                        "Te enviamos instrucciones a $email",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                .addOnFailureListener {
                    Snackbar.make(
                        binding.root,
                        it.localizedMessage ?: "No se pudo enviar el correo",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
        }

        binding.txtVolverLogin.setOnClickListener {
            findNavController().popBackStack(R.id.fragment_login, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
