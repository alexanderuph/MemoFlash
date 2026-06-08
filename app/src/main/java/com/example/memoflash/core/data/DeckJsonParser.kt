package com.example.memoflash.core.data

import com.example.memoflash.core.model.StudyDeck
import com.google.gson.Gson

object DeckJsonParser {
    private val gson = Gson()

    fun parse(json: String): List<StudyDeck> =
        gson.fromJson(json, Array<StudyDeck>::class.java)?.toList().orEmpty()
}
