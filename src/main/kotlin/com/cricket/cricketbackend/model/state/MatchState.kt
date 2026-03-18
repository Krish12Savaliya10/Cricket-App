package com.cricket.cricketbackend.model.state

data class MatchState(
    val matchId: Int,
    val team1Id: Int,
    val team2Id: Int,
    val oversLimit: Int,
    var currentInnings: Int? = null,
    var status: String = "UPCOMING",
    val innings: MutableMap<Int, InningsState> = mutableMapOf(),
    val history: ArrayDeque<MatchStateSnapshot> = ArrayDeque(),
)

data class InningsState(
    val inningsNo: Int,
    val battingTeamId: Int,
    val bowlingTeamId: Int,
    val target: Int? = null,
    var totalRuns: Int = 0,
    var wickets: Int = 0,
    var overNumber: Int = 0,
    var ballInOver: Int = 0,
    var strikerId: Int? = null,
    var nonStrikerId: Int? = null,
    var currentBowlerId: Int? = null,
    var freeHitActive: Boolean = false,
    var completed: Boolean = false,
    var resultSummary: String? = null,
    val batting: MutableMap<Int, BatterState> = mutableMapOf(),
    val bowling: MutableMap<Int, BowlerState> = mutableMapOf(),
    val events: MutableList<BallEventState> = mutableListOf(),
)

data class BatterState(
    val playerId: Int,
    var runs: Int = 0,
    var balls: Int = 0,
    var fours: Int = 0,
    var sixes: Int = 0,
    var out: Boolean = false,
    var battingStatus: String = "PENDING",
)

data class BowlerState(
    val playerId: Int,
    var wickets: Int = 0,
    var ballsBowled: Int = 0,
    var runsConceded: Int = 0,
)

data class BallEventState(
    val inningsNo: Int,
    val overNumber: Int,
    val ballNumber: Int,
    val eventType: String,
    val summary: String,
)

data class MatchStateSnapshot(
    val inningsCopies: Map<Int, InningsState>,
    val currentInnings: Int?,
    val status: String,
)
