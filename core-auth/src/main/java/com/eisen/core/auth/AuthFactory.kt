package com.eisen.core.auth

object AuthFactory {
    fun create(): IAuthManager {
        return FirebaseAuthManager()
    }
}

