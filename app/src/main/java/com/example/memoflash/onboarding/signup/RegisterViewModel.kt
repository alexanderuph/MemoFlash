package com.example.memoflash.onboarding.signup

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memoflash.core.AuthRepository
import com.example.memoflash.core.MemoUser
import com.example.memoflash.core.ResponseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _registerState = MutableStateFlow<ResponseService<MemoUser>?>(null)
    val registerState: StateFlow<ResponseService<MemoUser>?> = _registerState.asStateFlow()

    fun validateName(name: String): String? {
        if (name.isBlank()) return "El nombre es requerido"
        if (name.length < 2) return "Mínimo 2 caracteres"
        return null
    }

    fun validateEmail(email: String): String? {
        if (email.isBlank()) return "El correo es requerido"
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Correo inválido"
        return null
    }

    fun validatePassword(password: String): String? {
        if (password.isBlank()) return "La contraseña es requerida"
        if (password.length < 8) return "Mínimo 8 caracteres"
        return null
    }

    fun validateConfirmPassword(password: String, confirm: String): String? {
        if (confirm.isBlank()) return "Confirma tu contraseña"
        if (password != confirm) return "Las contraseñas no coinciden"
        return null
    }

    fun isRegisterFormValid(
        name: String,
        email: String,
        password: String,
        confirm: String
    ): Boolean {
        return validateName(name) == null &&
            validateEmail(email) == null &&
            validatePassword(password) == null &&
            validateConfirmPassword(password, confirm) == null
    }

    fun requestSignUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            _registerState.value = ResponseService.Loading
            _registerState.value = authRepository.requestSignUp(name, email, password)
        }
    }
}
