package com.cricket.cricketbackend.service

import com.cricket.cricketbackend.dto.response.BowlerScoreResponse
import com.cricket.cricketbackend.dto.response.PlayerScoreResponse
import com.cricket.cricketbackend.dto.response.ScoreboardResponse
import com.cricket.cricketbackend.exception.BadRequestException
import com.cricket.cricketbackend.model.state.InningsState
import org.springframework.stereotype.Service

@Service
class ScoreboardService(
    private val legacySimulationBridgeService: LegacySimulationBridgeService,
) {
    fun getScoreboard(matchId: Int): ScoreboardResponse {
        val state = legacySimulationBridgeService.getMatchState(matchId)
            ?: throw BadRequestException("Live scoreboard is not initialized for match $matchId")
        val inningsNo = state.currentInnings
        val innings = inningsNo?.let { state.innings[it] }
        if (innings == null) {
            return ScoreboardResponse(
                matchId = matchId,
                inningsNo = null,
                battingTeamId = null,
                bowlingTeamId = null,
                totalRuns = 0,
                wickets = 0,
                oversDisplay = "0.0",
                striker = null,
                nonStriker = null,
                currentBowler = null,
                status = state.status,
            )
        }
        return innings.toResponse(matchId, state.status)
    }

    fun fromCurrentState(matchId: Int): ScoreboardResponse = getScoreboard(matchId)

    private fun InningsState.toResponse(matchId: Int, status: String): ScoreboardResponse {
        val strikerState = strikerId?.let { batting[it] }
        val nonStrikerState = nonStrikerId?.let { batting[it] }
        val bowlerState = currentBowlerId?.let { bowling[it] }
        return ScoreboardResponse(
            matchId = matchId,
            inningsNo = inningsNo,
            battingTeamId = battingTeamId,
            bowlingTeamId = bowlingTeamId,
            totalRuns = totalRuns,
            wickets = wickets,
            oversDisplay = "$overNumber.$ballInOver",
            striker = strikerState?.let {
                PlayerScoreResponse(
                    playerId = it.playerId,
                    runs = it.runs,
                    balls = it.balls,
                    fours = it.fours,
                    sixes = it.sixes,
                    battingStatus = it.battingStatus,
                )
            },
            nonStriker = nonStrikerState?.let {
                PlayerScoreResponse(
                    playerId = it.playerId,
                    runs = it.runs,
                    balls = it.balls,
                    fours = it.fours,
                    sixes = it.sixes,
                    battingStatus = it.battingStatus,
                )
            },
            currentBowler = bowlerState?.let {
                BowlerScoreResponse(
                    playerId = it.playerId,
                    oversDisplay = "${it.ballsBowled / 6}.${it.ballsBowled % 6}",
                    wickets = it.wickets,
                    runsConceded = it.runsConceded,
                )
            },
            target = target,
            status = status,
            lastEvent = events.lastOrNull()?.summary,
        )
    }
}
