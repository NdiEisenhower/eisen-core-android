package com.eisen.core.updater

import android.app.Activity
import android.util.Log
import com.google.firebase.appdistribution.FirebaseAppDistribution
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DistributionStatus(
    val isTesterSignedIn: Boolean = false,
    val isCheckingForUpdate: Boolean = false,
    val lastError: String? = null
)

/**
 * Google Firebase App Distribution Manager.
 * Facilitates distributing pre-release builds to trusted testers,
 * checking for new tester releases, in-app alerts, and tester feedback.
 */
object AppDistributionManager {

    private const val TAG = "AppDistributionManager"

    private val _status = MutableStateFlow(DistributionStatus())
    val status: StateFlow<DistributionStatus> = _status.asStateFlow()

    private val appDistribution: FirebaseAppDistribution? by lazy {
        try {
            FirebaseAppDistribution.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Firebase App Distribution is not initialized or not available: ${e.message}")
            null
        }
    }

    /**
     * Checks if the tester is currently authenticated with Google for App Distribution.
     */
    fun isTesterSignedIn(): Boolean {
        return try {
            appDistribution?.isTesterSignedIn == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Prompts the tester to sign in with their Google account.
     */
    fun signInTester(onComplete: ((Boolean) -> Unit)? = null) {
        val dist = appDistribution ?: run {
            onComplete?.invoke(false)
            return
        }

        dist.signInTester()
            .addOnSuccessListener {
                _status.value = _status.value.copy(isTesterSignedIn = true)
                onComplete?.invoke(true)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Tester sign-in failed: ${e.message}")
                _status.value = _status.value.copy(lastError = e.message)
                onComplete?.invoke(false)
            }
    }

    /**
     * Checks if a new release is available on Firebase App Distribution and prompts the tester.
     */
    fun updateIfNewReleaseAvailable(onComplete: ((Boolean) -> Unit)? = null) {
        val dist = appDistribution ?: run {
            onComplete?.invoke(false)
            return
        }

        _status.value = _status.value.copy(isCheckingForUpdate = true)
        dist.updateIfNewReleaseAvailable()
            .addOnSuccessListener {
                _status.value = _status.value.copy(isCheckingForUpdate = false)
                onComplete?.invoke(true)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "App Distribution update check failed: ${e.message}")
                _status.value = _status.value.copy(
                    isCheckingForUpdate = false,
                    lastError = e.message
                )
                onComplete?.invoke(false)
            }
    }

    /**
     * Displays a notification allowing testers to submit in-app feedback with screenshots.
     */
    fun startFeedback(additionalFormText: String? = null) {
        try {
            if (additionalFormText != null) {
                appDistribution?.startFeedback(additionalFormText)
            } else {
                appDistribution?.startFeedback("Feedback from app tester")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start feedback: ${e.message}")
        }
    }
}
