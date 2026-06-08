package com.example.memoflash.home.agregar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.memoflash.R
import com.example.memoflash.core.ResponseService
import com.example.memoflash.core.model.Flashcard
import com.example.memoflash.core.model.StudyDeck
import com.example.memoflash.core.repositories.DeckRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AddDeckViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DeckRepository(application)
    private val _saveState = MutableStateFlow<ResponseService<StudyDeck>?>(null)
    val saveState: StateFlow<ResponseService<StudyDeck>?> = _saveState.asStateFlow()

    fun validateTitle(value: String): String? =
        if (value.trim().length < 3) text(R.string.deck_title_error) else null

    fun validateSubject(value: String): String? =
        if (value.trim().length < 3) text(R.string.deck_subject_error) else null

    fun validateDescription(value: String): String? =
        if (value.trim().length < 10) text(R.string.deck_description_error) else null

    fun validateQuestion(value: String): String? =
        if (value.trim().length < 5) text(R.string.deck_question_error) else null

    fun validateAnswer(value: String): String? =
        if (value.trim().length < 3) text(R.string.deck_answer_error) else null

    fun isValid(
        title: String,
        subject: String,
        description: String,
        question: String,
        answer: String
    ): Boolean = DeckValidation.isValid(title, subject, description, question, answer)

    fun saveDeck(
        title: String,
        subject: String,
        description: String,
        source: String,
        question: String,
        answer: String
    ) {
        if (!isValid(title, subject, description, question, answer)) return
        viewModelScope.launch {
            _saveState.value = ResponseService.Loading
            _saveState.value = repository.saveDeck(
                StudyDeck(
                    title = title.trim(),
                    subject = subject.trim(),
                    description = description.trim(),
                    source = source.ifBlank { text(R.string.manual_source) },
                    cards = listOf(
                        Flashcard(
                            id = UUID.randomUUID().toString(),
                            question = question.trim(),
                            answer = answer.trim()
                        )
                    )
                )
            )
        }
    }

    private fun text(resourceId: Int): String =
        getApplication<Application>().getString(resourceId)
}
