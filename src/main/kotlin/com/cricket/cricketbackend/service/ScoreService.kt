package com.cricket.cricketbackend.service

import com.cricket.cricketbackend.dto.request.BallInputDTO
import com.cricket.cricketbackend.dto.response.BatterScore
import com.cricket.cricketbackend.dto.response.BowlerStats
import com.cricket.cricketbackend.dto.response.MatchState
import com.cricket.cricketbackend.exception.BadRequestException
import com.cricket.cricketbackend.exception.ResourceNotFoundException
import com.cricket.cricketbackend.model.entity.BallByBallEntity
import com.cricket.cricketbackend.model.entity.ExtraType
import com.cricket.cricketbackend.model.entity.InningEntity
import com.cricket.cricketbackend.model.entity.MatchEntity
import com.cricket.cricketbackend.model.entity.MatchStatus
import com.cricket.cricketbackend.model.entity.PlayerEntity
import com.cricket.cricketbackend.model.entity.WicketType
import com.cricket.cricketbackend.repository.BallByBallRepository
import com.cricket.cricketbackend.repository.InningRepository
import com.cricket.cricketbackend.repository.MatchRepository
import com.cricket.cricketbackend.repository.PlayerRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ScoreService(
    private val matchRepository: MatchRepository,
    private val inningRepository: InningRepository,
    private val ballByBallRepository: BallByBallRepository,
    private val playerRepository: PlayerRepository,
) {
    @Transactional
    fun addBall(input: BallInputDTO): MatchState {
        validateBallInput(input)
        val match = getLiveMatch(input.matchId!!)
        val innings = getActiveInnings(match.id)
        val balls = ballByBallRepository.findAllByMatchIdAndInningsIdOrderBySequenceNoAsc(match.id, innings.id)
        val currentState = rebuildState(match, innings, balls)
        if (currentState.isClosed) {
            throw BadRequestException("Current innings is already completed")
        }

        val dismissedPlayerId = resolveDismissedPlayerId(input, currentState)
        val nextBall = BallByBallEntity(
            id = IdGenerator.nextLegacyStyleId(),
            match = match,
            innings = innings,
            sequenceNo = (balls.lastOrNull()?.sequenceNo ?: 0) + 1,
            overNumber = currentState.completedOvers,
            ballNumber = currentState.ballsInCurrentOver + 1,
            strikerId = currentState.strikerId ?: throw BadRequestException("Striker is not set"),
            nonStrikerId = currentState.nonStrikerId ?: throw BadRequestException("Non-striker is not set"),
            bowlerId = currentState.currentBowlerId ?: throw BadRequestException("Bowler is not set"),
            runs = input.runs,
            extras = calculateExtras(input.extraType, input.runs),
            extraType = input.extraType,
            isWicket = input.isWicket,
            wicketType = input.wicketType,
            dismissedPlayerId = dismissedPlayerId,
            nextBatsmanId = input.nextBatsmanId,
            nextBowlerId = input.nextBowlerId,
        )
        validateAppendEvent(match, innings, currentState, nextBall)
        ballByBallRepository.save(nextBall)

        val updatedState = rebuildState(match, innings, balls + nextBall)
        innings.completed = updatedState.isClosed
        inningRepository.save(innings)
        return updatedState.toResponse(match, innings, playerLookup(updatedState))
    }

    @Transactional
    fun undoLastBall(matchId: Int): MatchState {
        val match = getMatch(matchId)
        val innings = getLatestInnings(match.id)
        val lastBall = ballByBallRepository.findFirstByMatchIdAndInningsIdOrderBySequenceNoDesc(match.id, innings.id)
            ?: throw BadRequestException("No ball event found to undo for match $matchId")
        ballByBallRepository.delete(lastBall)
        val remainingBalls = ballByBallRepository.findAllByMatchIdAndInningsIdOrderBySequenceNoAsc(match.id, innings.id)
        val updatedState = rebuildState(match, innings, remainingBalls)
        innings.completed = updatedState.isClosed
        inningRepository.save(innings)
        return updatedState.toResponse(match, innings, playerLookup(updatedState))
    }

    @Transactional(readOnly = true)
    fun getMatchState(matchId: Int): MatchState {
        val match = getMatch(matchId)
        val innings = getLatestInnings(match.id)
        val balls = ballByBallRepository.findAllByMatchIdAndInningsIdOrderBySequenceNoAsc(match.id, innings.id)
        val state = rebuildState(match, innings, balls)
        return state.toResponse(match, innings, playerLookup(state))
    }

    internal fun rebuildState(
        match: MatchEntity,
        innings: InningEntity,
        balls: List<BallByBallEntity>,
    ): CalculatedState {
        val batting = linkedMapOf<Int, BatterAggregate>()
        val bowling = linkedMapOf<Int, BowlerAggregate>()
        var strikerId: Int? = innings.strikerId
        var nonStrikerId: Int? = innings.nonStrikerId
        var currentBowlerId: Int? = innings.currentBowlerId
        var totalRuns = 0
        var wickets = 0
        var legalBalls = 0
        var inningsClosed = innings.completed

        balls.sortedBy { it.sequenceNo }.forEach { ball ->
            val activeStrikerId = strikerId ?: throw BadRequestException("Missing striker while rebuilding innings ${innings.id}")
            val activeNonStrikerId = nonStrikerId ?: throw BadRequestException("Missing non-striker while rebuilding innings ${innings.id}")
            val activeBowlerId = currentBowlerId ?: throw BadRequestException("Missing bowler while rebuilding innings ${innings.id}")
            val striker = batting.getOrPut(activeStrikerId) { BatterAggregate(activeStrikerId) }
            batting.getOrPut(activeNonStrikerId) { BatterAggregate(activeNonStrikerId) }
            val bowler = bowling.getOrPut(activeBowlerId) { BowlerAggregate(activeBowlerId) }

            val legalBall = isLegalBall(ball.extraType)
            val battingRuns = battingRuns(ball.extraType, ball.runs)
            val teamRuns = teamRuns(ball.extraType, ball.runs)
            val bowlerRuns = bowlerRuns(ball.extraType, ball.runs)

            totalRuns += teamRuns
            striker.runs += battingRuns
            if (legalBall) {
                striker.balls += 1
                bowler.balls += 1
                legalBalls += 1
            }
            if (battingRuns == 4) {
                striker.fours += 1
            }
            if (battingRuns == 6) {
                striker.sixes += 1
            }
            bowler.runsConceded += bowlerRuns

            val rotatedIds = if (strikeShouldRotate(ball.extraType, ball.runs)) {
                activeNonStrikerId to activeStrikerId
            } else {
                activeStrikerId to activeNonStrikerId
            }
            var endStrikerId = rotatedIds.first
            var endNonStrikerId = rotatedIds.second

            if (ball.isWicket) {
                wickets += 1
                val dismissedId = ball.dismissedPlayerId ?: activeStrikerId
                batting.getOrPut(dismissedId) { BatterAggregate(dismissedId) }.out = true
                if (creditsBowlerWicket(ball.wicketType)) {
                    bowler.wickets += 1
                }
                if (wickets >= MAX_WICKETS) {
                    inningsClosed = true
                    if (dismissedId == endStrikerId) {
                        endStrikerId = 0
                    }
                    if (dismissedId == endNonStrikerId) {
                        endNonStrikerId = 0
                    }
                } else {
                    val incomingBatsman = ball.nextBatsmanId
                        ?: throw BadRequestException("nextBatsmanId is required after wicket before innings end")
                    batting.getOrPut(incomingBatsman) { BatterAggregate(incomingBatsman) }
                    if (dismissedId == endStrikerId) {
                        endStrikerId = incomingBatsman
                    } else if (dismissedId == endNonStrikerId) {
                        endNonStrikerId = incomingBatsman
                    } else {
                        throw BadRequestException("Dismissed player $dismissedId is not one of the active batters")
                    }
                }
            }

            strikerId = endStrikerId.takeIf { it != 0 }
            nonStrikerId = endNonStrikerId.takeIf { it != 0 }

            if (legalBall && legalBalls % BALLS_PER_OVER == 0 && !inningsClosed) {
                val temp = strikerId
                strikerId = nonStrikerId
                nonStrikerId = temp
                currentBowlerId = ball.nextBowlerId ?: currentBowlerId
            }

            if (legalBalls >= match.inningOvers * BALLS_PER_OVER) {
                inningsClosed = true
            }
        }

        return CalculatedState(
            totalRuns = totalRuns,
            wickets = wickets,
            legalBalls = legalBalls,
            strikerId = strikerId,
            nonStrikerId = nonStrikerId,
            currentBowlerId = currentBowlerId,
            batting = batting,
            bowling = bowling,
            isClosed = inningsClosed,
        )
    }

    private fun validateBallInput(input: BallInputDTO) {
        if (input.isWicket && input.wicketType == null) {
            throw BadRequestException("wicketType is required when isWicket is true")
        }
        if (!input.isWicket && input.wicketType != null) {
            throw BadRequestException("wicketType should be null when isWicket is false")
        }
        if (input.extraType == ExtraType.NOBALL && input.isWicket && input.wicketType != WicketType.RUN_OUT) {
            throw BadRequestException("Only run-out is supported as a wicket on a no-ball")
        }
    }

    private fun validateAppendEvent(
        match: MatchEntity,
        innings: InningEntity,
        currentState: CalculatedState,
        ball: BallByBallEntity,
    ) {
        validatePlayerForTeam(ball.strikerId, innings.battingTeam!!.id)
        validatePlayerForTeam(ball.nonStrikerId, innings.battingTeam!!.id)
        validatePlayerForTeam(ball.bowlerId, innings.bowlingTeam!!.id)
        ball.dismissedPlayerId?.let {
            if (it != ball.strikerId && it != ball.nonStrikerId) {
                throw BadRequestException("dismissedPlayerId must be one of the active batters")
            }
        }
        ball.nextBatsmanId?.let { validatePlayerForTeam(it, innings.battingTeam!!.id) }
        ball.nextBowlerId?.let { validatePlayerForTeam(it, innings.bowlingTeam!!.id) }
        if (ball.isWicket && currentState.wickets + 1 < MAX_WICKETS && ball.nextBatsmanId == null) {
            throw BadRequestException("nextBatsmanId is required while wickets remain")
        }
        if (isLegalBall(ball.extraType) &&
            (currentState.legalBalls + 1) % BALLS_PER_OVER == 0 &&
            currentState.legalBalls + 1 < match.inningOvers * BALLS_PER_OVER &&
            ball.nextBowlerId == null
        ) {
            throw BadRequestException("nextBowlerId is required at the end of an over")
        }
    }

    private fun resolveDismissedPlayerId(input: BallInputDTO, currentState: CalculatedState): Int? {
        if (!input.isWicket) {
            return null
        }
        val strikerId = currentState.strikerId ?: throw BadRequestException("Striker is not set")
        val nonStrikerId = currentState.nonStrikerId ?: throw BadRequestException("Non-striker is not set")
        val dismissed = input.dismissedPlayerId ?: strikerId
        if (dismissed != strikerId && dismissed != nonStrikerId) {
            throw BadRequestException("dismissedPlayerId must refer to the striker or non-striker")
        }
        return dismissed
    }

    private fun getLiveMatch(matchId: Int): MatchEntity {
        val match = getMatch(matchId)
        if (match.matchStatus != MatchStatus.LIVE) {
            throw BadRequestException("Match $matchId is not live")
        }
        return match
    }

    private fun getMatch(matchId: Int): MatchEntity = matchRepository.findById(matchId).orElseThrow {
        ResourceNotFoundException("Match $matchId not found")
    }

    private fun getActiveInnings(matchId: Int): InningEntity =
        inningRepository.findByMatchIdAndCompletedFalseOrderByInningsNoAsc(matchId).singleOrNull()
            ?: throw BadRequestException("No active innings found for match $matchId")

    private fun getLatestInnings(matchId: Int): InningEntity = inningRepository.findFirstByMatchIdOrderByInningsNoDesc(matchId)
        ?: throw BadRequestException("No innings found for match $matchId")

    private fun validatePlayerForTeam(playerId: Int, teamId: Int) {
        val player = playerRepository.findById(playerId).orElseThrow {
            ResourceNotFoundException("Player $playerId not found")
        }
        if (player.team!!.id != teamId) {
            throw BadRequestException("Player $playerId does not belong to team $teamId")
        }
    }

    private fun playerLookup(state: CalculatedState): Map<Int, PlayerEntity> {
        val ids = buildSet {
            state.strikerId?.let(::add)
            state.nonStrikerId?.let(::add)
            state.currentBowlerId?.let(::add)
            addAll(state.batting.keys)
            addAll(state.bowling.keys)
        }
        if (ids.isEmpty()) {
            return emptyMap()
        }
        return playerRepository.findAllById(ids).associateBy { it.id }
    }

    private fun CalculatedState.toResponse(
        match: MatchEntity,
        innings: InningEntity,
        players: Map<Int, PlayerEntity>,
    ): MatchState = MatchState(
        matchId = match.id,
        inningsNo = innings.inningsNo,
        battingTeamId = innings.battingTeam!!.id,
        bowlingTeamId = innings.bowlingTeam!!.id,
        totalRuns = totalRuns,
        wickets = wickets,
        overs = "$completedOvers.$ballsInCurrentOver",
        striker = strikerId?.let { id ->
            val player = players[id] ?: throw ResourceNotFoundException("Player $id not found")
            val stats = batting[id] ?: BatterAggregate(id)
            BatterScore(
                playerId = id,
                playerName = player.playerName,
                runs = stats.runs,
                balls = stats.balls,
            )
        },
        nonStriker = nonStrikerId?.let { id ->
            val player = players[id] ?: throw ResourceNotFoundException("Player $id not found")
            val stats = batting[id] ?: BatterAggregate(id)
            BatterScore(
                playerId = id,
                playerName = player.playerName,
                runs = stats.runs,
                balls = stats.balls,
            )
        },
        bowlerStats = currentBowlerId?.let { id ->
            val player = players[id] ?: throw ResourceNotFoundException("Player $id not found")
            val stats = bowling[id] ?: BowlerAggregate(id)
            BowlerStats(
                playerId = id,
                playerName = player.playerName,
                overs = "${stats.balls / BALLS_PER_OVER}.${stats.balls % BALLS_PER_OVER}",
                wickets = stats.wickets,
                runsConceded = stats.runsConceded,
            )
        },
        status = if (isClosed) MatchStatus.COMPLETED.name else match.matchStatus.name,
    )

    private fun calculateExtras(extraType: ExtraType, runs: Int): Int = when (extraType) {
        ExtraType.NONE -> 0
        ExtraType.BYE, ExtraType.LEGBYE -> runs
        ExtraType.WIDE, ExtraType.NOBALL -> runs + 1
    }

    private fun isLegalBall(extraType: ExtraType): Boolean = extraType != ExtraType.WIDE && extraType != ExtraType.NOBALL

    private fun battingRuns(extraType: ExtraType, runs: Int): Int = when (extraType) {
        ExtraType.NONE, ExtraType.NOBALL -> runs
        ExtraType.WIDE, ExtraType.BYE, ExtraType.LEGBYE -> 0
    }

    private fun teamRuns(extraType: ExtraType, runs: Int): Int = when (extraType) {
        ExtraType.NONE -> runs
        ExtraType.BYE, ExtraType.LEGBYE -> runs
        ExtraType.WIDE, ExtraType.NOBALL -> runs + 1
    }

    private fun bowlerRuns(extraType: ExtraType, runs: Int): Int = when (extraType) {
        ExtraType.BYE, ExtraType.LEGBYE -> 0
        else -> teamRuns(extraType, runs)
    }

    private fun strikeShouldRotate(extraType: ExtraType, runs: Int): Boolean = when (extraType) {
        ExtraType.WIDE -> runs % 2 == 1
        else -> runs % 2 == 1
    }

    private fun creditsBowlerWicket(wicketType: WicketType?): Boolean = wicketType != WicketType.RUN_OUT && wicketType != WicketType.RETIRED_OUT

    internal data class CalculatedState(
        val totalRuns: Int,
        val wickets: Int,
        val legalBalls: Int,
        val strikerId: Int?,
        val nonStrikerId: Int?,
        val currentBowlerId: Int?,
        val batting: Map<Int, BatterAggregate>,
        val bowling: Map<Int, BowlerAggregate>,
        val isClosed: Boolean,
    ) {
        val completedOvers: Int
            get() = legalBalls / BALLS_PER_OVER

        val ballsInCurrentOver: Int
            get() = legalBalls % BALLS_PER_OVER
    }

    internal data class BatterAggregate(
        val playerId: Int,
        var runs: Int = 0,
        var balls: Int = 0,
        var fours: Int = 0,
        var sixes: Int = 0,
        var out: Boolean = false,
    )

    internal data class BowlerAggregate(
        val playerId: Int,
        var balls: Int = 0,
        var wickets: Int = 0,
        var runsConceded: Int = 0,
    )

    companion object {
        private const val BALLS_PER_OVER = 6
        private const val MAX_WICKETS = 10
    }
}
