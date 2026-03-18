package com.cricket.cricketbackend.dto.response

data class MatchResponse(
    val id: Int,
    val hostUserId: Int,
    val team1: TeamResponse,
    val team2: TeamResponse,
    val overs: Int,
    val matchType: String,
    val status: String,
    val winnerTeamId: Int? = null,
)
