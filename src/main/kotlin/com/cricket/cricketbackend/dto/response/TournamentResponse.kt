package com.cricket.cricketbackend.dto.response

data class TournamentResponse(
    val id: Int,
    val tournamentName: String,
    val hostUserId: Int,
    val year: Int,
    val status: String,
    val winnerTeamId: Int? = null,
    val teams: List<TeamResponse> = emptyList(),
    val matches: List<MatchResponse> = emptyList(),
)
