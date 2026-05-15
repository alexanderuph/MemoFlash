package com.example.memoflash.core

interface Authentication {
    suspend fun requestLogin(email: String, password: String): ResponseService<MemoUser>
    suspend fun requestSignUp(name: String, email: String, password: String): ResponseService<MemoUser>
}
