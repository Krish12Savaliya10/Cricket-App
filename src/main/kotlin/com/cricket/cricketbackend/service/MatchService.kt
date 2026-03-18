package com.cricket.cricketbackend.service

import com.cricket.cricketbackend.dto.request.CreateSingleMatchRequest
import com.cricket.cricketbackend.dto.request.StartInningsRequest
import com.cricket.cricketbackend.dto.request.SubmitBallRequest
import com.cricket.cricketbackend.dto.response.BallResultResponse
import com.cricket.cricketbackend.dto.response.MatchResponse
import com.cricket.cricketbackend.dto.response.ScoreboardResponse
import com.cricket.cricketbackend.exception.BadRequestException
import com.cricket.cricketbackend.exception.ResourceNotFoundException
import com.cricket.cricketbackend.model.entity.MatchEntity
import com.cricket.cricketbackend.model.entity.MatchStatus
import com.cricket.cricketbackend.model.entity.MatchType
import com.cricket.cricketbackend.model.entity.PlayerMatchStatsEntity
import com.cricket.cricketbackend.model.entity.TeamMatchStatsEntity
import com.cricket.cricketbackend.repository.MatchRepository
import com.cricket.cricketbackend.repository.PlayerMatchStatsRepository
import com.cricket.cricketbackend.repository.TeamMatchStatsRepository
import com.cricket.cricketbackend.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MatchService(
    private val matchRepository: MatchRepository,
    private val userRepository: UserRepository,
    private val teamService: TeamService,
    private val playerService: PlayerService,
    private val teamMatchStatsRepository: TeamMatchStatsRepository,
    private val playerMatchStatsRepository: PlayerMatchStatsRepository,
    private val legacySimulationBridgeService: LegacySimulationBridgeService,
    private val scoreboardService: ScoreboardService,
) {

    @Transactional
    fun createSingleMatch(request: CreateSingleMatchRequest): MatchResponse {
        val host = userRepository.findById(request.hostUserId!!).orElseThrow {
            ResourceNotFoundException("Host user ${request.hostUserId} not found")
        }
        val team1 = teamService.getTeamEntity(request.team1Id!!)
        val team2 = teamService.getTeamEntity(request.team2Id!!)
        if (team1.id == team2.id) {
            throw BadRequestException("Team 1 and Team 2 must be different")
        }
        val match = MatchEntity(
            id = IdGenerator.nextLegacyStyleId(),
            host = host,
            team1 = team1,
            team2 = team2,
            matchNumber = 1,
            inningOvers = request.overs!!,
            matchType = MatchType.SINGLE,
            matchTypeId = null,
            matchStatus = MatchStatus.UPCOMING,
        )
        val saved = matchRepository.save(match)
        ensureStatsRows(saved)
        legacySimulationBridgeService.initializeMatch(saved)
        return toResponse(saved)
    }

    fun getMatch(matchId: Int): MatchResponse = toResponse(getMatchEntity(matchId))

    @Transactional
    fun startInnings(matchId: Int, inningsNo: Int, request: StartInningsRequest): ScoreboardResponse {
        if (inningsNo !in 1..2) {
            throw BadRequestException("Only innings 1 or 2 are supported")
        }
        val match = getMatchEntity(matchId)
        if (matchRepository.existsByMatchStatusAndIdNot(MatchStatus.LIVE, matchId)) {
            throw BadRequestException("Another live match is already running. Legacy scoring currently supports one live match at a time.")
        }
        if (match.matchStatus == MatchStatus.COMPLETED) {
            throw BadRequestException("Completed matches cannot start a new innings")
        }
        validatePlayerBelongsToTeam(request.opener1Id!!, request.battingTeamId!!)
        validatePlayerBelongsToTeam(request.opener2Id!!, request.battingTeamId!!)
        val bowlingTeamId = if (request.battingTeamId == match.team1!!.id) match.team2!!.id else match.team1!!.id
        validatePlayerBelongsToTeam(request.bowlerId!!, bowlingTeamId)
        val existingState = legacySimulationBridgeService.getMatchState(matchId)
        if (existingState?.innings?.containsKey(inningsNo) == true) {
            throw BadRequestException("Innings $inningsNo is already initialized for match $matchId")
        }
        if (inningsNo == 2 && existingState?.innings?.get(1)?.completed != true) {
            throw BadRequestException("Second innings cannot start before the first innings is completed")
        }

        match.matchStatus = MatchStatus.LIVE
        matchRepository.save(match)
        legacySimulationBridgeService.startInnings(match, inningsNo, request)
        syncScoreboardToStats(matchId)
        return scoreboardService.fromCurrentState(matchId)
    }

    @Transactional
    fun submitBall(matchId: Int, request: SubmitBallRequest): BallResultResponse {
        val match = getMatchEntity(matchId)
        if (match.matchStatus != MatchStatus.LIVE) {
            throw BadRequestException("Match $matchId is not live")
        }
        legacySimulationBridgeService.submitBall(match, request)
        syncScoreboardToStats(matchId)
        val scoreboard = scoreboardService.fromCurrentState(matchId)
        if (scoreboard.status == MatchStatus.COMPLETED.name && match.winnerTeam == null) {
            // TODO: derive winner/result using the full legacy engine when interactive state is bridged directly.
            match.matchStatus = MatchStatus.COMPLETED
            matchRepository.save(match)
        }
        return BallResultResponse(
            scoreboard = scoreboardService.fromCurrentState(matchId),
            matchStatus = scoreboard.status,
        )
    }

    @Transactional
    fun undoLastBall(matchId: Int): ScoreboardResponse {
        val match = getMatchEntity(matchId)
        if (match.matchStatus != MatchStatus.LIVE) {
            throw BadRequestException("Only live matches support undo")
        }
        legacySimulationBridgeService.undoLastBall(matchId)
        syncScoreboardToStats(matchId)
        return scoreboardService.fromCurrentState(matchId)
    }

    fun getScoreboard(matchId: Int): ScoreboardResponse = scoreboardService.getScoreboard(matchId)

    fun listMatches(
        hostUserId: Int? = null,
        status: MatchStatus? = null,
        type: MatchType? = null,
    ): List<MatchResponse> {
        val matches = when {
            hostUserId != null -> matchRepository.findAllByHostId(hostUserId)
            status != null -> matchRepository.findAllByMatchStatus(status)
            type != null -> matchRepository.findAllByMatchType(type)
            else -> matchRepository.findAll()
        }
        return matches.sortedBy { it.id }.map(::toResponse)
    }

    fun getMatchEntity(matchId: Int): MatchEntity = matchRepository.findById(matchId).orElseThrow {
        ResourceNotFoundException("Match $matchId not found")
    }

    @Transactional
    fun createScheduledMatch(
        hostUserId: Int,
        team1Id: Int,
        team2Id: Int,
        overs: Int,
        matchType: MatchType,
        matchTypeId: Int?,
        matchNumber: Int,
    ): MatchEntity {
        val host = userRepository.findById(hostUserId).orElseThrow {
            ResourceNotFoundException("Host user $hostUserId not found")
        }
        val team1 = teamService.getTeamEntity(team1Id)
        val team2 = teamService.getTeamEntity(team2Id)
        val saved = matchRepository.save(
            MatchEntity(
                id = IdGenerator.nextLegacyStyleId(),
                host = host,
                team1 = team1,
                team2 = team2,
                matchNumber = matchNumber,
                inningOvers = overs,
                matchType = matchType,
                matchTypeId = matchTypeId,
                matchStatus = MatchStatus.UPCOMING,
            ),
        )
        ensureStatsRows(saved)
        return saved
    }

    fun toResponse(match: MatchEntity): MatchResponse = MatchResponse(
        id = match.id,
        hostUserId = match.host!!.id,
        team1 = teamService.toResponse(match.team1!!),
        team2 = teamService.toResponse(match.team2!!),
        overs = match.inningOvers,
        matchType = match.matchType.name,
        status = match.matchStatus.name,
        winnerTeamId = match.winnerTeam?.id,
    )

    private fun validatePlayerBelongsToTeam(playerId: Int, teamId: Int) {
        val player = playerService.getPlayerEntity(playerId)
        if (player.team!!.id != teamId) {
            throw BadRequestException("Player $playerId does not belong to team $teamId")
        }
    }

    private fun ensureStatsRows(match: MatchEntity) {
        if (teamMatchStatsRepository.findAllByMatchId(match.id).isEmpty()) {
            teamMatchStatsRepository.save(
                TeamMatchStatsEntity(match = match, team = match.team1, tournamentId = null),
            )
            teamMatchStatsRepository.save(
                TeamMatchStatsEntity(match = match, team = match.team2, tournamentId = null),
            )
        }
        val teamIds = listOf(match.team1!!.id, match.team2!!.id)
        val players = teamIds.flatMap { playerService.getPlayersForTeam(it) }
        players.forEach { player ->
            if (playerMatchStatsRepository.findByMatchIdAndPlayerId(match.id, player.id) == null) {
                playerMatchStatsRepository.save(
                    PlayerMatchStatsEntity(match = match, player = player),
                )
            }
        }
    }

    private fun syncScoreboardToStats(matchId: Int) {
        val state = legacySimulationBridgeService.getMatchState(matchId) ?: return
        val currentInnings = state.currentInnings?.let { state.innings[it] } ?: return
        val match = getMatchEntity(matchId)
        val battingStats = teamMatchStatsRepository.findByMatchIdAndTeamId(matchId, currentInnings.battingTeamId)
        battingStats?.apply {
            runsScored = currentInnings.totalRuns
            wicketsLost = currentInnings.wickets
            oversPlayed = "${currentInnings.overNumber}.${currentInnings.ballInOver}".toDouble()
            teamMatchStatsRepository.save(this)
        }
        currentInnings.batting.values.forEach { batter ->
            playerMatchStatsRepository.findByMatchIdAndPlayerId(matchId, batter.playerId)?.apply {
                runsScored = batter.runs
                ballsFaced = batter.balls
                fours = batter.fours
                sixes = batter.sixes
                battingStatus = batter.battingStatus
                playerMatchStatsRepository.save(this)
            }
        }
        currentInnings.bowling.values.forEach { bowler ->
            playerMatchStatsRepository.findByMatchIdAndPlayerId(matchId, bowler.playerId)?.apply {
                wickets = bowler.wickets
                oversBowled = "${bowler.ballsBowled / 6}.${bowler.ballsBowled % 6}".toDouble()
                runsConceded = bowler.runsConceded
                playerMatchStatsRepository.save(this)
            }
        }
        if (state.status == MatchStatus.COMPLETED.name) {
            match.matchStatus = MatchStatus.COMPLETED
            matchRepository.save(match)
        }
    }
}
