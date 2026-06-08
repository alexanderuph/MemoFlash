package com.example.memoflash.home.agregar

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeckValidationTest {
    @Test
    fun validDeckFields_areAccepted() {
        assertTrue(
            DeckValidation.isValid(
                "Biología",
                "Ciencias",
                "Resumen de células y organelos",
                "¿Qué es una célula?",
                "La unidad básica de la vida"
            )
        )
    }

    @Test
    fun shortFields_areRejected() {
        assertFalse(DeckValidation.isValid("A", "B", "Corta", "Qué", "No"))
    }
}
