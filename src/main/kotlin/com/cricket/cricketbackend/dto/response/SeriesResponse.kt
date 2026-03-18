package com.cricket.cricketbackend.dto.response

data class SeriesResponse(
    val id: Int,
    val seriesName: String,
    val hostUserId: Int,
    val team1: TeamResponse,
    val team2: TeamResponse,
    val totalMatches: Int,
    val completedMatches: Int,
    val status: String,
    val year: Int?,
    val matches: List<MatchResponse> = emptyList(),
)
