package com.eisen.core.auth

import com.eisen.core.common.model.CoreResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.concurrent.CopyOnWriteArrayList

class FirebaseAuthManager : IAuthManager {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val listeners = CopyOnWriteArrayList<AuthListener>()

    init {
        auth.addAuthStateListener { fbAuth ->
            val user = fbAuth.currentUser?.toCoreUser()
            listeners.forEach { it.onAuthStateChanged(user) }
        }
    }

    override fun getCurrentUser(): CoreUser? {
        return auth.currentUser?.toCoreUser()
    }

    override fun signInAnonymously(onComplete: (CoreResult<CoreUser>) -> Unit) {
        auth.signInAnonymously()
            .addOnSuccessListener { result ->
                val user = result.user?.toCoreUser()
                if (user != null) {
                    onComplete(CoreResult.Success(user))
                } else {
                    onComplete(CoreResult.Error("User was null after sign-in"))
                }
            }
            .addOnFailureListener { exception ->
                val errorMsg = exception.message ?: "Authentication failed"
                listeners.forEach { it.onAuthError(errorMsg) }
                onComplete(CoreResult.Error(errorMsg, exception))
            }
    }

    override fun signOut(onComplete: () -> Unit) {
        auth.signOut()
        onComplete()
    }

    override fun addListener(listener: AuthListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: AuthListener) {
        listeners.remove(listener)
    }

    private fun FirebaseUser.toCoreUser(): CoreUser {
        return CoreUser(
            uid = this.uid,
            email = this.email,
            displayName = this.displayName,
            photoUrl = this.photoUrl?.toString(),
            isAnonymous = this.isAnonymous
        )
    }
}
