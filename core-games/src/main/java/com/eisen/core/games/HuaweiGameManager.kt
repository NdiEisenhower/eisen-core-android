package com.eisen.core.games

import android.app.Activity
import com.eisen.core.games.model.GamePlayer
import com.huawei.hms.jos.AppParams
import com.huawei.hms.jos.JosApps
import com.huawei.hms.jos.games.Games

class HuaweiGameManager : IGameManager {

    private var isSignedIn = false
    private var currentPlayer: GamePlayer? = null

    override fun initialize(activity: Activity) {
        try {
            JosApps.getJosAppsClient(activity).init(AppParams())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun signIn(activity: Activity, callback: (Boolean, GamePlayer?, String?) -> Unit) {
        try {
            val playersClient = Games.getPlayersClient(activity)
            playersClient.currentPlayer
                .addOnSuccessListener { player ->
                    val gamePlayer = GamePlayer(
                        playerId = player.playerId ?: "",
                        displayName = player.displayName ?: "Player",
                        avatarUri = player.hiResImageUri?.toString() ?: player.iconImageUri?.toString(),
                        provider = "HUAWEI_HMS"
                    )
                    currentPlayer = gamePlayer
                    isSignedIn = true
                    try {
                        Games.getBuoyClient(activity).showFloatWindow()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    callback(true, gamePlayer, null)
                }
                .addOnFailureListener { e ->
                    // Attempt getGamePlayer fallback
                    playersClient.gamePlayer
                        .addOnSuccessListener { p ->
                            val gamePlayer = GamePlayer(
                                playerId = p.playerId ?: "",
                                displayName = p.displayName ?: "Player",
                                avatarUri = p.hiResImageUri?.toString() ?: p.iconImageUri?.toString(),
                                provider = "HUAWEI_HMS"
                            )
                            currentPlayer = gamePlayer
                            isSignedIn = true
                            callback(true, gamePlayer, null)
                        }
                        .addOnFailureListener { err ->
                            isSignedIn = false
                            callback(false, null, err.localizedMessage ?: e.localizedMessage)
                        }
                }
        } catch (e: Exception) {
            isSignedIn = false
            callback(false, null, e.localizedMessage)
        }
    }

    override fun submitScore(
        activity: Activity,
        leaderboardId: String,
        score: Long,
        callback: ((Boolean) -> Unit)?
    ) {
        try {
            val playersClient = Games.getPlayersClient(activity)
            playersClient.submitPlayerEvent(leaderboardId, score.toString())
                .addOnSuccessListener { callback?.invoke(true) }
                .addOnFailureListener { callback?.invoke(false) }
        } catch (e: Exception) {
            callback?.invoke(false)
        }
    }

    override fun showLeaderboard(activity: Activity, leaderboardId: String) {
        try {
            Games.getBuoyClient(activity).showFloatWindow()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun showAllLeaderboards(activity: Activity) {
        try {
            Games.getBuoyClient(activity).showFloatWindow()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun unlockAchievement(
        activity: Activity,
        achievementId: String,
        callback: ((Boolean) -> Unit)?
    ) {
        try {
            val playersClient = Games.getPlayersClient(activity)
            playersClient.submitPlayerEvent(achievementId, "UNLOCK")
                .addOnSuccessListener { callback?.invoke(true) }
                .addOnFailureListener { callback?.invoke(false) }
        } catch (e: Exception) {
            callback?.invoke(false)
        }
    }

    override fun incrementAchievement(
        activity: Activity,
        achievementId: String,
        steps: Int,
        callback: ((Boolean) -> Unit)?
    ) {
        try {
            val playersClient = Games.getPlayersClient(activity)
            playersClient.submitPlayerEvent(achievementId, steps.toString())
                .addOnSuccessListener { callback?.invoke(true) }
                .addOnFailureListener { callback?.invoke(false) }
        } catch (e: Exception) {
            callback?.invoke(false)
        }
    }

    override fun showAchievements(activity: Activity) {
        try {
            Games.getBuoyClient(activity).showFloatWindow()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun isUserSignedIn(): Boolean = isSignedIn
}
