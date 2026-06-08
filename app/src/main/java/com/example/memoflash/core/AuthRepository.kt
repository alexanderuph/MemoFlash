package com.example.memoflash.core

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepository : Authentication {
    private val auth = FirebaseAuth.getInstance()

    override suspend fun requestLogin(
        email: String,
        password: String
    ): ResponseService<MemoUser> = withContext(Dispatchers.IO) {
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
                ?: return@withContext ResponseService.Error("Usuario no encontrado")
            val user = MemoUser(
                id = firebaseUser.uid,
                email = firebaseUser.email.orEmpty(),
                name = firebaseUser.displayName.orEmpty()
            )
            SessionStore.currentUser = user
            ResponseService.Success(user)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            ResponseService.Error("Correo o contraseña incorrectos")
        } catch (e: FirebaseAuthException) {
            ResponseService.Error(e.localizedMessage ?: "Error de autenticación")
        } catch (e: Exception) {
            ResponseService.Error("Error inesperado. Intenta de nuevo")
        }
    }

    override suspend fun requestSignUp(
        name: String,
        email: String,
        password: String
    ): ResponseService<MemoUser> = withContext(Dispatchers.IO) {
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
                ?: return@withContext ResponseService.Error("No se pudo crear el usuario")
            val user = MemoUser(
                id = firebaseUser.uid,
                email = firebaseUser.email.orEmpty(),
                name = name
            )
            SessionStore.currentUser = user
            ResponseService.Success(user)
        } catch (e: FirebaseAuthUserCollisionException) {
            ResponseService.Error("Este correo ya está registrado")
        } catch (e: FirebaseAuthWeakPasswordException) {
            ResponseService.Error("La contraseña es muy débil")
        } catch (e: FirebaseAuthException) {
            ResponseService.Error(e.localizedMessage ?: "Error de autenticación")
        } catch (e: Exception) {
            ResponseService.Error("Error inesperado. Intenta de nuevo")
        }
    }

    override suspend fun requestPasswordReset(email: String): ResponseService<Unit> =
        withContext(Dispatchers.IO) {
            try {
                auth.sendPasswordResetEmail(email).await()
                ResponseService.Success(Unit)
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                ResponseService.Error("El correo no tiene un formato válido")
            } catch (e: FirebaseAuthException) {
                ResponseService.Error(e.localizedMessage ?: "No se pudo enviar el correo")
            } catch (e: Exception) {
                ResponseService.Error("No se pudo enviar el correo. Intenta de nuevo")
            }
        }
}
