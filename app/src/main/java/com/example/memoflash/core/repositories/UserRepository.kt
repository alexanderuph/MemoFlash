package com.example.memoflash.core.repositories

import com.example.memoflash.core.ResponseService
import com.example.memoflash.core.SessionStore
import com.example.memoflash.onboarding.personal.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserRepository : UserService {
    private val firestore = FirebaseFirestore.getInstance()
    private val userCollection = firestore.collection("users")

    override suspend fun saveUserInfo(userProfile: UserProfile): ResponseService<Unit> =
        withContext(Dispatchers.IO) {
            try {
                userCollection.document(userProfile.id).set(userProfile).await()
                SessionStore.currentProfile = userProfile
                ResponseService.Success(Unit)
            } catch (e: Exception) {
                ResponseService.Error("No se pudo guardar el perfil: ${e.localizedMessage}")
            }
        }
}
