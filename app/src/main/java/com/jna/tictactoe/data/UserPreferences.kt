package com.jna.tictactoe.data

data class UserPreferences(
    val name: String = "Player 1",
    val rank: String = "Novice",
    val winRate: Float = 0f,
    val profilePicturePath: String? = null
)
