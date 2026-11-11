package com.eisen.core.games

import android.app.Activity
import com.eisen.core.games.model.GamePlayer
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk

class GoogleGameManager : IGameManager {

    private var isSignedIn = false
    private var currentPlayer: GamePlayer? = null

    companion object {
        private const val RC_UNUSED = 9002
    }

    override fun initialize(activity: Activity) {
        try {
            PlayGamesSdk.initialize(activity)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun signIn(activity: Activity, callback: (Boolean, GamePlayer?, String?) -> Unit) {
        val gamesSignInClient = PlayGames.getGamesSignInClient(activity)
        gamesSignInClient.isAuthenticated.addOnCompleteListener { task ->
            val isAuthenticated = task.isSuccessful && task.result.isAuthenticated
            if (isAuthenticated) {
                PlayGames.getPlayersClient(activity).currentPlayer.addOnCompleteListener { playerTask ->
                    if (playerTask.isSuccessful) {
                        val player = playerTask.result
                        val gamePlayer = GamePlayer(
                            playerId = player.playerId,
                            displayName = player.displayName,
                            avatarUri = player.hiResImageUri?.toString() ?: player.iconImageUri?.toString(),
                            provider = "GOOGLE_PLAY"
                        )
                        currentPlayer = gamePlayer
                        isSignedIn = true
                        callback(true, gamePlayer, null)
                    } else {
                        isSignedIn = true
                        callback(true, null, null)
                    }
                }
            } else {
                // Request Sign-In
                gamesSignInClient.signIn().addOnCompleteListener { signInTask ->
                    if (signInTask.isSuccessful && signInTask.result.isAuthenticated) {
                        PlayGames.getPlayersClient(activity).currentPlayer.addOnCompleteListener { playerTask ->
                            if (playerTask.isSuccessful) {
                                val player = playerTask.result
                                val gamePlayer = GamePlayer(
                                    playerId = player.playerId,
                                    displayName = player.displayName,
                                    avatarUri = player.hiResImageUri?.toString() ?: player.iconImageUri?.toString(),
                                    provider = "GOOGLE_PLAY"
                                )
                                currentPlayer = gamePlayer
                                isSignedIn = true
                                callback(true, gamePlayer, null)
                            } else {
                                isSignedIn = true
                                callback(true, null, null)
                            }
                        }
                    } else {
                        isSignedIn = false
                        callback(false, null, signInTask.exception?.localizedMessage ?: "Sign-in failed or cancelled")
                    }
                }
            }
        }
    }

    override fun submitScore(
        activity: Activity,
        leaderboardId: String,
        score: Long,
        callback: ((Boolean) -> Unit)?
    ) {
        try {
            PlayGames.getLeaderboardsClient(activity).submitScore(leaderboardId, score)
            callback?.invoke(true)
        } catch (e: Exception) {
            callback?.invoke(false)
        }
    }

    override fun showLeaderboard(activity: Activity, leaderboardId: String) {
        PlayGames.getLeaderboardsClient(activity).getLeaderboardIntent(leaderboardId)
            .addOnSuccessListener { intent ->
                activity.startActivityForResult(intent, RC_UNUSED)
            }
    }

    override fun showAllLeaderboards(activity: Activity) {
        PlayGames.getLeaderboardsClient(activity).allLeaderboardsIntent
            .addOnSuccessListener { intent ->
                activity.startActivityForResult(intent, RC_UNUSED)
            }
    }

    override fun unlockAchievement(
        activity: Activity,
        achievementId: String,
        callback: ((Boolean) -> Unit)?
    ) {
        try {
            PlayGames.getAchievementsClient(activity).unlock(achievementId)
            callback?.invoke(true)
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
            PlayGames.getAchievementsClient(activity).increment(achievementId, steps)
            callback?.invoke(true)
        } catch (e: Exception) {
            callback?.invoke(false)
        }
    }

    override fun showAchievements(activity: Activity) {
        PlayGames.getAchievementsClient(activity).achievementsIntent
            .addOnSuccessListener { intent ->
                activity.startActivityForResult(intent, RC_UNUSED)
            }
    }

    override fun isUserSignedIn(): Boolean = isSignedIn
}

