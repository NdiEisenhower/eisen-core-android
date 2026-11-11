package com.eisen.core.games.bridge

import android.app.Activity
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.eisen.core.common.bridge.JsBridge
import com.eisen.core.games.IGameManager
import org.json.JSONObject

class GameJsBridge(
    private val activity: Activity,
    private val gameManager: IGameManager,
    private val webView: WebView? = null
) : JsBridge("AndroidGames") {

    @JavascriptInterface
    fun signIn() {
        activity.runOnUiThread {
            gameManager.signIn(activity) { success, player, error ->
                val playerJson = if (player != null) {
                    JSONObject().apply {
                        put("playerId", player.playerId)
                        put("displayName", player.displayName)
                        put("avatarUri", player.avatarUri ?: "")
                        put("provider", player.provider)
                    }.toString()
                } else {
                    "null"
                }

                val errSafe = (error ?: "").replace("'", "\\'")
                val js = """
                    if (typeof window.onGamesSignedIn === 'function') {
                        window.onGamesSignedIn($success, $playerJson, '$errSafe');
                    }
                """.trimIndent()
                webView?.evaluateJavascript(js, null)
            }
        }
    }

    @JavascriptInterface
    fun submitScore(leaderboardId: String, score: Long) {
        activity.runOnUiThread {
            gameManager.submitScore(activity, leaderboardId, score) { success ->
                val js = """
                    if (typeof window.onScoreSubmitted === 'function') {
                        window.onScoreSubmitted('$leaderboardId', $score, $success);
                    }
                """.trimIndent()
                webView?.evaluateJavascript(js, null)
            }
        }
    }

    @JavascriptInterface
    fun showLeaderboard(leaderboardId: String) {
        activity.runOnUiThread {
            gameManager.showLeaderboard(activity, leaderboardId)
        }
    }

    @JavascriptInterface
    fun showAllLeaderboards() {
        activity.runOnUiThread {
            gameManager.showAllLeaderboards(activity)
        }
    }

    @JavascriptInterface
    fun unlockAchievement(achievementId: String) {
        activity.runOnUiThread {
            gameManager.unlockAchievement(activity, achievementId) { success ->
                val js = """
                    if (typeof window.onAchievementUnlocked === 'function') {
                        window.onAchievementUnlocked('$achievementId', $success);
                    }
                """.trimIndent()
                webView?.evaluateJavascript(js, null)
            }
        }
    }

    @JavascriptInterface
    fun incrementAchievement(achievementId: String, steps: Int) {
        activity.runOnUiThread {
            gameManager.incrementAchievement(activity, achievementId, steps)
        }
    }

    @JavascriptInterface
    fun showAchievements() {
        activity.runOnUiThread {
            gameManager.showAchievements(activity)
        }
    }

    @JavascriptInterface
    fun isSignedIn(): Boolean = gameManager.isUserSignedIn()
}

