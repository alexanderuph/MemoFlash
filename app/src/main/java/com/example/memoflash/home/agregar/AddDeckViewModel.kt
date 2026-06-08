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
    private val _cards = MutableStateFlow<List<Flashcard>>(emptyList())
    val cards: StateFlow<List<Flashcard>> = _cards.asStateFlow()
    private val _deckState = MutableStateFlow<ResponseService<StudyDeck>?>(null)
    val deckState: StateFlow<ResponseService<StudyDeck>?> = _deckState.asStateFlow()
    private val _saveState = MutableStateFlow<ResponseService<StudyDeck>?>(null)
    val saveState: StateFlow<ResponseService<StudyDeck>?> = _saveState.asStateFlow()
    private var loadedDeck: StudyDeck? = null

    fun loadDeck(deckId: String) {
        if (deckId.isBlank() || loadedDeck?.id == deckId) return
        viewModelScope.launch {
            _deckState.value = ResponseService.Loading
            val result = repository.getDeck(deckId)
            if (result is ResponseService.Success) {
                loadedDeck = result.data
                _cards.value = result.data.cards
            }
            _deckState.value = result
        }
    }

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

    fun addOrUpdateCard(cardId: String?, question: String, answer: String): Boolean {
        if (validateQuestion(question) != null || validateAnswer(answer) != null) return false
        val card = Flashcard(
            id = cardId ?: UUID.randomUUID().toString(),
            question = question.trim(),
            answer = answer.trim()
        )
        _cards.value = if (cardId == null) {
            _cards.value + card
        } else {
            _cards.value.map { if (it.id == cardId) card else it }
        }
        return true
    }

    fun removeCard(cardId: String) {
        _cards.value = _cards.value.filterNot { it.id == cardId }
    }

    fun canSave(title: String, subject: String, description: String): Boolean =
        validateTitle(title) == null &&
            validateSubject(subject) == null &&
            validateDescription(description) == null &&
            _cards.value.isNotEmpty()

    fun saveDeck(title: String, subject: String, description: String) {
        if (!canSave(title, subject, description)) return
        val previous = loadedDeck
        viewModelScope.launch {
            _saveState.value = ResponseService.Loading
            _saveState.value = repository.saveDeck(
                StudyDeck(
                    id = previous?.id.orEmpty(),
                    title = title.trim(),
                    subject = subject.trim(),
                    description = description.trim(),
                    source = "",
                    ownerId = previous?.ownerId.orEmpty(),
                    createdAt = previous?.createdAt ?: 0L,
                    cards = _cards.value
                )
            )
        }
    }

    private fun text(resourceId: Int): String =
        getApplication<Application>().getString(resourceId)
}
