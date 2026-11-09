package com.eisen.core.auth

import android.app.Activity
import com.eisen.core.common.model.CoreResult

interface IAuthManager {
    fun getCurrentUser(): CoreUser?
    fun signInAnonymously(onComplete: (CoreResult<CoreUser>) -> Unit)
    fun signOut(onComplete: () -> Unit)
    fun addListener(listener: AuthListener)
    fun removeListener(listener: AuthListener)
}

