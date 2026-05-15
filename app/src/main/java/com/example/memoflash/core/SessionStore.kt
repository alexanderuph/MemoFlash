package com.example.memoflash.core

import com.example.memoflash.onboarding.personal.model.UserProfile

object SessionStore {
    var currentUser: MemoUser? = null
    var currentProfile: UserProfile? = null
}
