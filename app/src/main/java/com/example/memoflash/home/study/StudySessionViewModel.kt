package com.example.memoflash.home.study

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.memoflash.core.ResponseService
import com.example.memoflash.core.model.StudyDeck
import com.example.memoflash.core.repositories.DeckRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StudySessionState(
    val deck: StudyDeck,
    val cardIndex: Int = 0,
    val answerVisible: Boolean = false
)

class StudySessionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DeckRepository(application)
    private val _loadState = MutableStateFlow<ResponseService<StudyDeck>?>(null)
    val loadState: StateFlow<ResponseService<StudyDeck>?> = _loadState.asStateFlow()
    private val _session = MutableStateFlow<StudySessionState?>(null)
    val session: StateFlow<StudySessionState?> = _session.asStateFlow()

    fun load(deckId: String) {
        if (_session.value?.deck?.id == deckId) return
        viewModelScope.launch {
            _loadState.value = ResponseService.Loading
            val result = repository.getDeck(deckId)
            if (result is ResponseService.Success && result.data.cards.isNotEmpty()) {
                _session.value = StudySessionState(result.data)
            }
            _loadState.value = result
        }
    }

    fun revealAnswer() {
        _session.value = _session.value?.copy(answerVisible = true)
    }

    fun nextCard() {
        val current = _session.value ?: return
        if (current.cardIndex < current.deck.cards.lastIndex) {
            _session.value = current.copy(
                cardIndex = current.cardIndex + 1,
                answerVisible = false
            )
        }
    }

    fun previousCard() {
        val current = _session.value ?: return
        if (current.cardIndex > 0) {
            _session.value = current.copy(
                cardIndex = current.cardIndex - 1,
                answerVisible = false
            )
        }
    }
}
