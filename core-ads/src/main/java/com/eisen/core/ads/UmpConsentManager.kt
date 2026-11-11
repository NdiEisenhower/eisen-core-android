package com.eisen.core.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

/**
 * Google UMP (User Messaging Platform) Consent Helper.
 * Handles GDPR / European Regulations (EEA/UK) consent collection effortlessly before serving AdMob ads.
 */
object UmpConsentManager {

    private const val TAG = "UmpConsentManager"

    /**
     * Requests consent information update and automatically presents the consent form if required.
     * Invokes [onConsentGathered] with true if ads can now be requested, false otherwise.
     */
    fun requestConsent(
        activity: Activity,
        tagForUnderAgeOfConsent: Boolean = false,
        onConsentGathered: (canRequestAds: Boolean) -> Unit
    ) {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(tagForUnderAgeOfConsent)
            .build()

        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                // Consent info updated successfully
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(TAG, "Consent form error: ${formError.errorCode} - ${formError.message}")
                    }
                    onConsentGathered(consentInformation.canRequestAds())
                }
            },
            { requestError ->
                Log.w(TAG, "Consent info update failed: ${requestError.errorCode} - ${requestError.message}")
                onConsentGathered(consentInformation.canRequestAds())
            }
        )
    }

    /**
     * Checks if ads can be requested based on consent state.
     */
    fun canRequestAds(context: Context): Boolean {
        return UserMessagingPlatform.getConsentInformation(context).canRequestAds()
    }

    /**
     * Returns true if user is eligible to modify privacy/consent options (e.g., in GDPR regions).
     */
    fun isPrivacyOptionsRequired(context: Context): Boolean {
        val consentInformation = UserMessagingPlatform.getConsentInformation(context)
        return consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }

    /**
     * Displays privacy options form allowing users to change consent settings later.
     */
    fun showPrivacyOptionsForm(
        activity: Activity,
        onDismissed: () -> Unit
    ) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            if (formError != null) {
                Log.w(TAG, "Privacy options form error: ${formError.errorCode} - ${formError.message}")
            }
            onDismissed()
        }
    }

    /**
     * Resets consent state (useful during testing/QA).
     */
    fun resetConsent(context: Context) {
        UserMessagingPlatform.getConsentInformation(context).reset()
    }
}
