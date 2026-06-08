package com.example.memoflash.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeckJsonParserTest {
    @Test
    fun parse_returnsDecksAndCards() {
        val json = """
            [
              {
                "id": "deck-1",
                "title": "Álgebra",
                "subject": "Matemáticas",
                "description": "Conceptos básicos",
                "source": "algebra.pdf",
                "cards": [
                  {
                    "id": "card-1",
                    "question": "¿Qué es una variable?",
                    "answer": "Un símbolo que representa un valor."
                  }
                ]
              }
            ]
        """.trimIndent()

        val decks = DeckJsonParser.parse(json)

        assertEquals(1, decks.size)
        assertEquals("Álgebra", decks.first().title)
        assertEquals("¿Qué es una variable?", decks.first().cards.first().question)
    }

    @Test
    fun parse_acceptsEmptyDeckList() {
        assertTrue(DeckJsonParser.parse("[]").isEmpty())
    }
}
