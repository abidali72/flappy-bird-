package com.example.flippybird.data

/**
 * Leaderboard repository interface — stub for Firebase integration.
 */
interface LeaderboardRepository {
    suspend fun submitScore(playerName: String, score: Int): Boolean
    suspend fun getTopScores(limit: Int): List<LeaderboardEntry>
}

data class LeaderboardEntry(
    val playerName: String,
    val score: Int,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * No-op implementation. Replace with Firebase Firestore implementation when ready.
 */
class NoOpLeaderboardRepository : LeaderboardRepository {
    override suspend fun submitScore(playerName: String, score: Int) = false
    override suspend fun getTopScores(limit: Int) = emptyList<LeaderboardEntry>()
}
