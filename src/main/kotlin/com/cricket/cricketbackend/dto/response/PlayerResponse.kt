package com.cricket.cricketbackend.dto.response

data class PlayerResponse(
    val id: Int,
    val playerName: String,
    val role: String,
    val teamId: Int,
)
