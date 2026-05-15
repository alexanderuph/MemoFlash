package com.example.memoflash.onboarding.signIn

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

class SignInViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _signInState = MutableStateFlow<ResponseService<MemoUser>?>(null)
    val signInState: StateFlow<ResponseService<MemoUser>?> = _signInState.asStateFlow()

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

    fun isLoginFormValid(email: String, password: String): Boolean {
        return validateEmail(email) == null && validatePassword(password) == null
    }

    fun requestLogin(email: String, password: String) {
        viewModelScope.launch {
            _signInState.value = ResponseService.Loading
            _signInState.value = repository.requestLogin(email, password)
        }
    }
}
