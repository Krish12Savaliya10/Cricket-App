package com.cricket.cricketbackend.dto.response

data class MatchState(
    val matchId: Int,
    val inningsNo: Int,
    val battingTeamId: Int,
    val bowlingTeamId: Int,
    val totalRuns: Int,
    val wickets: Int,
    val overs: String,
    val striker: BatterScore?,
    val nonStriker: BatterScore?,
    val bowlerStats: BowlerStats?,
    val status: String,
)

data class BatterScore(
    val playerId: Int,
    val playerName: String,
    val runs: Int,
    val balls: Int,
)

data class BowlerStats(
    val playerId: Int,
    val playerName: String,
    val overs: String,
    val wickets: Int,
    val runsConceded: Int,
)
