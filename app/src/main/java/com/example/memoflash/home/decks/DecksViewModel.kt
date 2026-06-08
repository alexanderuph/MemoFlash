package com.example.memoflash.home.decks

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

class DecksViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DeckRepository(application)
    private val _deckState = MutableStateFlow<ResponseService<List<StudyDeck>>?>(null)
    val deckState: StateFlow<ResponseService<List<StudyDeck>>?> = _deckState.asStateFlow()

    fun loadDecks() {
        viewModelScope.launch {
            _deckState.value = ResponseService.Loading
            _deckState.value = repository.getDecks()
        }
    }
}
