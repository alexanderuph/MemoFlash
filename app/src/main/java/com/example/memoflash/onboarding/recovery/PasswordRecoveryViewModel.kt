package com.example.memoflash.onboarding.recovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memoflash.core.AuthRepository
import com.example.memoflash.core.ResponseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PasswordRecoveryViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _resetState = MutableStateFlow<ResponseService<Unit>?>(null)
    val resetState: StateFlow<ResponseService<Unit>?> = _resetState.asStateFlow()

    fun isValidEmail(email: String): Boolean = EmailValidation.isValid(email)

    fun sendInstructions(email: String) {
        val normalizedEmail = email.trim()
        if (!isValidEmail(normalizedEmail)) return
        viewModelScope.launch {
            _resetState.value = ResponseService.Loading
            _resetState.value = repository.requestPasswordReset(normalizedEmail)
        }
    }
}
