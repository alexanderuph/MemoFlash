package com.example.memoflash.core.repositories

import android.content.Context
import com.example.memoflash.R
import com.example.memoflash.core.ResponseService
import com.example.memoflash.core.data.DeckJsonParser
import com.example.memoflash.core.model.StudyDeck
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class DeckRepository(
    private val context: Context,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : DeckService {
    private val deckCollection = firestore.collection("decks")

    override suspend fun getDecks(): ResponseService<List<StudyDeck>> =
        withContext(Dispatchers.IO) {
            try {
                val localDecks = loadLocalDecks()
                val userId = auth.currentUser?.uid
                if (userId == null) {
                    return@withContext ResponseService.Success(localDecks)
                }

                val remoteDecks = deckCollection
                    .whereEqualTo("ownerId", userId)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { it.toObject(StudyDeck::class.java) }

                ResponseService.Success((remoteDecks + localDecks).distinctBy(StudyDeck::id))
            } catch (e: Exception) {
                ResponseService.Error(
                    e.localizedMessage ?: context.getString(R.string.deck_load_error)
                )
            }
        }

    override suspend fun getDeck(deckId: String): ResponseService<StudyDeck> =
        withContext(Dispatchers.IO) {
            try {
                loadLocalDecks().firstOrNull { it.id == deckId }?.let {
                    return@withContext ResponseService.Success(it)
                }
                val document = deckCollection.document(deckId).get().await()
                val deck = document.toObject(StudyDeck::class.java)
                    ?: return@withContext ResponseService.Error(
                        context.getString(R.string.deck_not_found)
                    )
                ResponseService.Success(deck)
            } catch (e: Exception) {
                ResponseService.Error(
                    e.localizedMessage ?: context.getString(R.string.deck_load_error)
                )
            }
        }

    override suspend fun saveDeck(deck: StudyDeck): ResponseService<StudyDeck> =
        withContext(Dispatchers.IO) {
            val userId = auth.currentUser?.uid
                ?: return@withContext ResponseService.Error(
                    context.getString(R.string.session_required)
                )
            try {
                val savedDeck = deck.copy(
                    id = deck.id.ifBlank { UUID.randomUUID().toString() },
                    ownerId = userId,
                    createdAt = deck.createdAt.takeIf { it > 0 } ?: System.currentTimeMillis()
                )
                deckCollection.document(savedDeck.id).set(savedDeck).await()
                ResponseService.Success(savedDeck)
            } catch (e: Exception) {
                ResponseService.Error(
                    e.localizedMessage ?: context.getString(R.string.deck_save_error)
                )
            }
        }

    override suspend fun deleteDeck(deckId: String): ResponseService<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val document = deckCollection.document(deckId).get().await()
                val deck = document.toObject(StudyDeck::class.java)
                if (deck?.ownerId != auth.currentUser?.uid) {
                    return@withContext ResponseService.Error(
                        context.getString(R.string.deck_delete_not_allowed)
                    )
                }
                document.reference.delete().await()
                ResponseService.Success(Unit)
            } catch (e: Exception) {
                ResponseService.Error(
                    e.localizedMessage ?: context.getString(R.string.deck_delete_error)
                )
            }
        }

    private fun loadLocalDecks(): List<StudyDeck> {
        val json = context.resources.openRawResource(R.raw.study_decks)
            .bufferedReader()
            .use { it.readText() }
        return DeckJsonParser.parse(json)
    }
}
