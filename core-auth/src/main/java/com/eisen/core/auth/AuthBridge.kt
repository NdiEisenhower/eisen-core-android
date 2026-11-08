package com.eisen.core.auth

import android.app.Activity
import android.webkit.JavascriptInterface
import com.eisen.core.common.bridge.JsBridge
import com.eisen.core.common.model.CoreResult
import org.json.JSONObject
import java.lang.ref.WeakReference

class AuthBridge(
    activity: Activity,
    private val authManager: IAuthManager
) : JsBridge("AndroidAuth"), AuthListener {

    private val activityRef = WeakReference(activity)

    init {
        authManager.addListener(this)
    }

    @JavascriptInterface
    fun signInAnonymously() {
        val activity = activityRef.get() ?: return
        activity.runOnUiThread {
            authManager.signInAnonymously { result ->
                when (result) {
                    is CoreResult.Success -> {
                        val json = userToJson(result.data)
                        evaluateJavascript("window.onAuthSuccess && window.onAuthSuccess($json)")
                    }
                    is CoreResult.Error -> {
                        val quoted = JSONObject.quote(result.message)
                        evaluateJavascript("window.onAuthError && window.onAuthError($quoted)")
                    }
                    is CoreResult.Loading -> {}
                }
            }
        }
    }

    @JavascriptInterface
    fun signOut() {
        val activity = activityRef.get() ?: return
        activity.runOnUiThread {
            authManager.signOut {
                evaluateJavascript("window.onSignedOut && window.onSignedOut()")
            }
        }
    }

    @JavascriptInterface
    fun getCurrentUser(): String {
        val user = authManager.getCurrentUser() ?: return "{}"
        return userToJson(user).toString()
    }

    override fun onAuthStateChanged(user: CoreUser?) {
        if (user != null) {
            val json = userToJson(user)
            evaluateJavascript("window.onAuthStateChanged && window.onAuthStateChanged($json)")
        } else {
            evaluateJavascript("window.onAuthStateChanged && window.onAuthStateChanged(null)")
        }
    }

    override fun onAuthError(message: String) {
        val quoted = JSONObject.quote(message)
        evaluateJavascript("window.onAuthError && window.onAuthError($quoted)")
    }

    private fun userToJson(user: CoreUser): JSONObject {
        return JSONObject().apply {
            put("uid", user.uid)
            put("email", user.email ?: "")
            put("displayName", user.displayName ?: "")
            put("photoUrl", user.photoUrl ?: "")
            put("isAnonymous", user.isAnonymous)
        }
    }

    override fun detach() {
        authManager.removeListener(this)
        super.detach()
    }
}
