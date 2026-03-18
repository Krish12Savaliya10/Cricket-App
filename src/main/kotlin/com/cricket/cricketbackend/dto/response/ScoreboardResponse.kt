package com.cricket.cricketbackend.dto.response

data class ScoreboardResponse(
    val matchId: Int,
    val inningsNo: Int?,
    val battingTeamId: Int?,
    val bowlingTeamId: Int?,
    val totalRuns: Int,
    val wickets: Int,
    val oversDisplay: String,
    val striker: PlayerScoreResponse?,
    val nonStriker: PlayerScoreResponse?,
    val currentBowler: BowlerScoreResponse?,
    val target: Int? = null,
    val status: String,
    val lastEvent: String? = null,
)

data class PlayerScoreResponse(
    val playerId: Int,
    val runs: Int,
    val balls: Int,
    val fours: Int,
    val sixes: Int,
    val battingStatus: String,
)

data class BowlerScoreResponse(
    val playerId: Int,
    val oversDisplay: String,
    val wickets: Int,
    val runsConceded: Int,
)
