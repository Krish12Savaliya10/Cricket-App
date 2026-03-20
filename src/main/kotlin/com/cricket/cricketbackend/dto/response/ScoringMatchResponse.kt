package com.cricket.cricketbackend.dto.response

data class ScoringMatchResponse(
    val id: Int,
    val hostUserId: Int,
    val team1Id: Int,
    val team2Id: Int,
    val overs: Int,
    val status: String,
    val currentState: MatchState? = null,
)
