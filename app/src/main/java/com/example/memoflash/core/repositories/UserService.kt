package com.example.memoflash.core.repositories

import com.example.memoflash.core.ResponseService
import com.example.memoflash.onboarding.personal.model.UserProfile

interface UserService {
    suspend fun saveUserInfo(userProfile: UserProfile): ResponseService<Unit>
}
