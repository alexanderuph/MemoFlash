package com.example.memoflash.home.agregar

object DeckValidation {
    fun isValid(
        title: String,
        subject: String,
        description: String,
        question: String,
        answer: String
    ): Boolean =
        title.trim().length >= 3 &&
            subject.trim().length >= 3 &&
            description.trim().length >= 10 &&
            question.trim().length >= 5 &&
            answer.trim().length >= 3
}
