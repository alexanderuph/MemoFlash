package com.example.memoflash.core.repositories

import com.example.memoflash.core.ResponseService
import com.example.memoflash.core.model.StudyDeck

interface DeckService {
    suspend fun getDecks(): ResponseService<List<StudyDeck>>
    suspend fun getDeck(deckId: String): ResponseService<StudyDeck>
    suspend fun saveDeck(deck: StudyDeck): ResponseService<StudyDeck>
    suspend fun deleteDeck(deckId: String): ResponseService<Unit>
}
