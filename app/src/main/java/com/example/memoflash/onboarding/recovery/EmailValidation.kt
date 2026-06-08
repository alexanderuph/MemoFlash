package com.example.memoflash.onboarding.recovery

object EmailValidation {
    private val pattern = Regex(
        "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
        RegexOption.IGNORE_CASE
    )

    fun isValid(email: String): Boolean = pattern.matches(email.trim())
}
