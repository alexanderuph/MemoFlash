package com.example.memoflash.core.model

data class StudyDeck(
    val id: String = "",
    val title: String = "",
    val subject: String = "",
    val description: String = "",
    val source: String = "",
    val ownerId: String = "",
    val createdAt: Long = 0L,
    val cards: List<Flashcard> = emptyList()
)

data class Flashcard(
    val id: String = "",
    val question: String = "",
    val answer: String = ""
)
