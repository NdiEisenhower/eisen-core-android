package com.eisen.core.auth

data class CoreUser(
    val uid: String,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val isAnonymous: Boolean = false,
    val idToken: String? = null
)

interface AuthListener {
    fun onAuthStateChanged(user: CoreUser?)
    fun onAuthError(message: String)
}
