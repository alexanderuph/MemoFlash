package com.example.memoflash.onboarding.recovery

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EmailValidationTest {
    @Test
    fun validEmail_isAccepted() {
        assertTrue(EmailValidation.isValid("estudiante@correo.com"))
    }

    @Test
    fun malformedEmail_isRejected() {
        assertFalse(EmailValidation.isValid("estudiante-correo"))
    }
}
