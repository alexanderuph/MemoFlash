package com.example.memoflash.home.detail

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

class DeckDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DeckRepository(application)
    private val _deckState = MutableStateFlow<ResponseService<StudyDeck>?>(null)
    val deckState: StateFlow<ResponseService<StudyDeck>?> = _deckState.asStateFlow()

    fun loadDeck(deckId: String) {
        viewModelScope.launch {
            _deckState.value = ResponseService.Loading
            _deckState.value = repository.getDeck(deckId)
        }
    }
}
