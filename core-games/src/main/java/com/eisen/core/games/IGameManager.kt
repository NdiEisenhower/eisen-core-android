package com.eisen.core.games

import android.app.Activity
import com.eisen.core.games.model.GamePlayer

interface IGameManager {
    fun initialize(activity: Activity)
    fun signIn(activity: Activity, callback: (Boolean, GamePlayer?, String?) -> Unit)
    fun submitScore(activity: Activity, leaderboardId: String, score: Long, callback: ((Boolean) -> Unit)? = null)
    fun showLeaderboard(activity: Activity, leaderboardId: String)
    fun showAllLeaderboards(activity: Activity)
    fun unlockAchievement(activity: Activity, achievementId: String, callback: ((Boolean) -> Unit)? = null)
    fun incrementAchievement(activity: Activity, achievementId: String, steps: Int, callback: ((Boolean) -> Unit)? = null)
    fun showAchievements(activity: Activity)
    fun isUserSignedIn(): Boolean
}

