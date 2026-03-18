package com.cricket.cricketbackend.service

import DataStructure.LinkedListOfPlayer
import DataStructure.StackOfBatsman
import DataStructure.StackOfBowler
import DataStructure.StackOfTeam
import Simulation.Match as LegacyMatch
import Simulation.Team as LegacyTeam
import com.cricket.cricketbackend.dto.request.BallEventType
import com.cricket.cricketbackend.dto.request.DismissalType
import com.cricket.cricketbackend.dto.request.NoBallMode
import com.cricket.cricketbackend.dto.request.RunoutEnd
import com.cricket.cricketbackend.dto.request.StartInningsRequest
import com.cricket.cricketbackend.dto.request.SubmitBallRequest
import com.cricket.cricketbackend.exception.BadRequestException
import com.cricket.cricketbackend.model.entity.MatchEntity
import com.cricket.cricketbackend.model.entity.PlayerRole
import com.cricket.cricketbackend.model.state.BallEventState
import com.cricket.cricketbackend.model.state.BatterState
import com.cricket.cricketbackend.model.state.BowlerState
import com.cricket.cricketbackend.model.state.InningsState
import com.cricket.cricketbackend.model.state.MatchState
import com.cricket.cricketbackend.repository.PlayerRepository
import org.springframework.stereotype.Service
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintWriter
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.Scanner
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

@Service
class LegacySimulationBridgeService(
    private val playerRepository: PlayerRepository,
) {

    // TODO: `MatchSimulation` uses static mutable fields, so only one live legacy innings is truly safe at a time.
    // TODO: The legacy engine still writes to stdout; this adapter isolates input but not per-session output streams.
    private val states = ConcurrentHashMap<Int, MatchState>()
    private val sessions = ConcurrentHashMap<Int, LegacyMatchSession>()

    private val matchSimulationClass = Class.forName("Simulation.MatchSimulation")
    private val playInningsMethod: Method = matchSimulationClass.getDeclaredMethod(
        "playInnings",
        Scanner::class.java,
        LegacyTeam::class.java,
        Boolean::class.javaPrimitiveType,
        Int::class.javaPrimitiveType,
        Int::class.javaPrimitiveType,
        Int::class.javaPrimitiveType,
        Int::class.javaPrimitiveType,
        Int::class.javaPrimitiveType,
    ).apply { isAccessible = true }
    private val displayMatchResultMethod: Method = matchSimulationClass.getDeclaredMethod(
        "displayMatchResult",
        LegacyTeam::class.java,
        Int::class.javaPrimitiveType,
        LegacyMatch::class.java,
        Int::class.javaPrimitiveType,
    ).apply { isAccessible = true }

    private val team1Field = staticField("team1")
    private val team2Field = staticField("team2")
    private val team1PlayersField = staticField("team1Players")
    private val team2PlayersField = staticField("team2Players")
    private val batsman1Field = staticField("Batsman1")
    private val batsman2Field = staticField("Batsman2")
    private val bat1StackField = staticField("Bat1")
    private val bat2StackField = staticField("Bat2")
    private val bowlerStackField = staticField("Ball")
    private val teamStackField = staticField("BatTeam")

    fun initializeMatch(match: MatchEntity): MatchState {
        val state = MatchState(
            matchId = match.id,
            team1Id = match.team1!!.id,
            team2Id = match.team2!!.id,
            oversLimit = match.inningOvers,
            status = match.matchStatus.name,
        )
        states[match.id] = state
        sessions.computeIfAbsent(match.id) { createSession(match, state) }
        return state
    }

    fun getMatchState(matchId: Int): MatchState? = sessions[matchId]?.let(::snapshotState) ?: states[matchId]

    fun startInnings(match: MatchEntity, inningsNo: Int, request: StartInningsRequest): MatchState {
        val state = states[match.id] ?: initializeMatch(match)
        val session = sessions[match.id] ?: createSession(match, state)
        val running = session.activeInnings
        if (running?.thread?.isAlive == true) {
            throw BadRequestException("A legacy innings thread is already active for match ${match.id}")
        }

        val battingTeam = session.teamById(request.battingTeamId!!)
        val bowlingTeam = session.opponentTeam(request.battingTeamId)
        val battingPlayers = session.playersByTeamId(request.battingTeamId)
        val opener1 = battingPlayers.getPlayerById(request.opener1Id!!)
        val opener2 = battingPlayers.getPlayerById(request.opener2Id!!)
        val startingBowler = session.findPlayer(request.bowlerId!!)
        validateStartingBowler(session, bowlingTeam, startingBowler.playerId)

        session.prepareForInningsStart(inningsNo, opener1, opener2)
        setLegacyStaticContext(session, opener1, opener2)

        val target = if (inningsNo == 2) session.firstInningsTarget() else 0
        val inningsState = InningsState(
            inningsNo = inningsNo,
            battingTeamId = battingTeam.getTeamId(),
            bowlingTeamId = bowlingTeam.getTeamId(),
            target = if (inningsNo == 2) target else null,
            strikerId = opener1.playerId,
            nonStrikerId = opener2.playerId,
            currentBowlerId = startingBowler.playerId,
        )
        state.innings[inningsNo] = inningsState
        state.currentInnings = inningsNo
        state.status = "LIVE"

        val runner = LegacyInningsRunner(
            session = session,
            inningsNo = inningsNo,
            battingTeam = battingTeam,
            isTeam1Batting = battingTeam.getTeamId() == session.team1.getTeamId(),
            target = target,
            currentBowlerId = startingBowler.playerId,
            meta = LegacyMeta(currentBowlerId = startingBowler.playerId),
        )
        session.activeInnings = runner
        runner.thread = thread(start = true, isDaemon = true, name = "legacy-match-${match.id}-innings-$inningsNo") {
            try {
                playInningsMethod.invoke(
                    null,
                    session.scanner,
                    battingTeam,
                    runner.isTeam1Batting,
                    match.inningOvers,
                    inningsNo,
                    session.playerCountForTeam(battingTeam.getTeamId()),
                    target,
                    match.id,
                )
                if (inningsNo == 2) {
                    displayMatchResultMethod.invoke(
                        null,
                        battingTeam,
                        session.playerCountForTeam(battingTeam.getTeamId()),
                        session.legacyMatch,
                        0,
                    )
                }
            } catch (ex: Exception) {
                runner.failure.set(ex)
            } finally {
                runner.completed = true
            }
        }

        session.writeLine(startingBowler.getPlayerName())
        waitForThreadToBlock()
        return snapshotState(session)
    }

    fun submitBall(match: MatchEntity, request: SubmitBallRequest): MatchState {
        val session = sessions[match.id] ?: throw BadRequestException("Legacy session not initialized for match ${match.id}")
        val runner = session.activeInnings ?: throw BadRequestException("Innings has not started for match ${match.id}")
        ensureRunnerHealthy(runner)

        validateBallRequest(session, runner, request)
        val beforeSignature = snapshotSignature(session)
        runner.history.addLast(LegacyStepSnapshot(runner.meta.copy(), runner.events.size))

        val mapped = mapBallRequestToLegacyInput(session, runner, request)
        mapped.inputs.forEach(session::writeLine)

        waitForLegacyProgress(session, beforeSignature)
        ensureRunnerHealthy(runner)

        runner.events.add(
            BallEventState(
                inningsNo = runner.inningsNo,
                overNumber = runner.meta.overNumber,
                ballNumber = runner.meta.ballInOver,
                eventType = request.eventType!!.name,
                summary = mapped.summary,
            ),
        )
        return snapshotState(session)
    }

    fun undoLastBall(matchId: Int): MatchState {
        val session = sessions[matchId] ?: throw BadRequestException("Legacy session not initialized for match $matchId")
        val runner = session.activeInnings ?: throw BadRequestException("Innings has not started for match $matchId")
        ensureRunnerHealthy(runner)

        val snapshot = runner.history.removeLastOrNull()
            ?: throw BadRequestException("No ball available to undo in the current legacy innings")
        val beforeSignature = snapshotSignature(session)
        session.writeLine("U")
        waitForLegacyProgress(session, beforeSignature)
        ensureRunnerHealthy(runner)

        runner.meta = snapshot.meta
        runner.currentBowlerId = snapshot.meta.currentBowlerId
        while (runner.events.size > snapshot.eventCount) {
            runner.events.removeLast()
        }
        return snapshotState(session)
    }

    private fun createSession(match: MatchEntity, state: MatchState): LegacyMatchSession {
        val team1Players = buildLegacyPlayers(match.team1!!.id)
        val team2Players = buildLegacyPlayers(match.team2!!.id)
        val legacyTeam1 = LegacyTeam(match.team1!!.teamName, match.team1!!.id)
        val legacyTeam2 = LegacyTeam(match.team2!!.teamName, match.team2!!.id)
        val session = LegacyMatchSession(
            matchId = match.id,
            legacyMatch = LegacyMatch(legacyTeam1, legacyTeam2, match.id, "SINGLE"),
            team1 = legacyTeam1,
            team2 = legacyTeam2,
            team1Players = team1Players,
            team2Players = team2Players,
            scannerInput = PipedInputStream(),
            state = state,
        )
        session.connect()
        sessions[match.id] = session
        return session
    }

    private fun buildLegacyPlayers(teamId: Int): LinkedListOfPlayer {
        val legacyPlayers = LinkedListOfPlayer()
        val players = playerRepository.findAllByTeamId(teamId).sortedWith(compareBy({ roleOrder(it.role) }, { it.playerName }))
        players.forEach { player ->
            when (player.role) {
                PlayerRole.BATSMAN -> legacyPlayers.addBatsman(player.playerName, player.id)
                PlayerRole.ALLROUNDER -> legacyPlayers.addAllrounder(player.playerName, player.id)
                PlayerRole.BOWLER -> legacyPlayers.addBowler(player.playerName, player.id)
            }
        }
        return legacyPlayers
    }

    private fun roleOrder(role: PlayerRole): Int = when (role) {
        PlayerRole.BATSMAN -> 0
        PlayerRole.ALLROUNDER -> 1
        PlayerRole.BOWLER -> 2
    }

    private fun setLegacyStaticContext(
        session: LegacyMatchSession,
        opener1: LinkedListOfPlayer.Player,
        opener2: LinkedListOfPlayer.Player,
    ) {
        team1Field.set(null, session.team1)
        team2Field.set(null, session.team2)
        team1PlayersField.set(null, session.team1Players)
        team2PlayersField.set(null, session.team2Players)
        batsman1Field.set(null, opener1)
        batsman2Field.set(null, opener2)
        bat1StackField.set(null, StackOfBatsman())
        bat2StackField.set(null, StackOfBatsman())
        bowlerStackField.set(null, StackOfBowler())
        teamStackField.set(null, StackOfTeam())
    }

    private fun validateStartingBowler(session: LegacyMatchSession, bowlingTeam: LegacyTeam, bowlerId: Int) {
        val player = session.findPlayer(bowlerId)
        val playerEntity = playerRepository.findById(bowlerId).orElseThrow()
        if (playerEntity.team!!.id != bowlingTeam.getTeamId()) {
            throw BadRequestException("Starting bowler must belong to the bowling team")
        }
        if (playerEntity.role != PlayerRole.BOWLER && playerEntity.role != PlayerRole.ALLROUNDER) {
            throw BadRequestException("Starting bowler must be a bowler or all-rounder")
        }
        player.getPlayerName()
    }

    private fun validateBallRequest(session: LegacyMatchSession, runner: LegacyInningsRunner, request: SubmitBallRequest) {
        when (request.eventType ?: throw BadRequestException("eventType is required")) {
            BallEventType.RUN -> {
                val runs = request.runs ?: 0
                if (runs !in setOf(0, 1, 2, 3, 4, 6)) {
                    throw BadRequestException("RUN deliveries must use 0, 1, 2, 3, 4 or 6")
                }
            }

            BallEventType.EXTRA -> {
                if ((request.extraRuns ?: -1) !in 0..4) {
                    throw BadRequestException("EXTRA deliveries require extraRuns between 0 and 4")
                }
            }

            BallEventType.WIDE -> {
                if ((request.extraRuns ?: 0) < 1) {
                    throw BadRequestException("WIDE deliveries require extraRuns >= 1 including the mandatory wide run")
                }
            }

            BallEventType.NO_BALL -> {
                when (request.noBallMode ?: NoBallMode.STANDARD) {
                    NoBallMode.STANDARD -> {
                        val runs = request.runs ?: 0
                        if (runs !in setOf(0, 1, 2, 3, 4, 6)) {
                            throw BadRequestException("STANDARD no-balls require batsman runs 0, 1, 2, 3, 4 or 6")
                        }
                    }

                    NoBallMode.EXTRA -> {
                        if ((request.extraRuns ?: -1) !in 0..4) {
                            throw BadRequestException("EXTRA no-balls require extraRuns between 0 and 4")
                        }
                    }

                    NoBallMode.RUN_OUT -> validateRunoutFields(session, request, runner.isAllOutAfterNextWicket(session))
                }
            }

            BallEventType.WICKET -> {
                when (request.dismissalType ?: DismissalType.REGULAR) {
                    DismissalType.REGULAR -> {
                        if (!runner.isAllOutAfterNextWicket(session) && request.nextBatsmanId == null) {
                            throw BadRequestException("nextBatsmanId is required for a non-terminal wicket")
                        }
                    }

                    DismissalType.RUN_OUT -> validateRunoutFields(session, request, runner.isAllOutAfterNextWicket(session))
                }
            }
        }

        if (requiresExplicitNextBowler(runner, request)) {
            val nextBowlerId = request.nextBowlerId
                ?: throw BadRequestException("nextBowlerId is required when a legal ball completes the over")
            validateNextBowler(session, runner, nextBowlerId)
        }
    }

    private fun validateRunoutFields(session: LegacyMatchSession, request: SubmitBallRequest, allOutAfterWicket: Boolean) {
        val outPlayerId = request.outPlayerId ?: throw BadRequestException("outPlayerId is required for runout flows")
        session.findPlayer(outPlayerId)
        if ((request.runoutRuns ?: -1) !in 0..2) {
            throw BadRequestException("runoutRuns must be between 0 and 2")
        }
        if (request.runoutEnd == null) {
            throw BadRequestException("runoutEnd is required for runout flows")
        }
        if (!allOutAfterWicket && request.nextBatsmanId == null) {
            throw BadRequestException("nextBatsmanId is required when runout does not end the innings")
        }
    }

    private fun validateNextBowler(session: LegacyMatchSession, runner: LegacyInningsRunner, nextBowlerId: Int) {
        val playerEntity = playerRepository.findById(nextBowlerId).orElseThrow()
        if (playerEntity.team!!.id != runner.bowlingTeamId()) {
            throw BadRequestException("nextBowlerId must belong to the bowling team")
        }
        if (playerEntity.role != PlayerRole.BOWLER && playerEntity.role != PlayerRole.ALLROUNDER) {
            throw BadRequestException("nextBowlerId must be a bowler or all-rounder")
        }
        if (runner.currentBowlerId == nextBowlerId) {
            throw BadRequestException("nextBowlerId cannot repeat the previous over's bowler")
        }
        session.findPlayer(nextBowlerId).getPlayerName()
    }

    private fun mapBallRequestToLegacyInput(
        session: LegacyMatchSession,
        runner: LegacyInningsRunner,
        request: SubmitBallRequest,
    ): LegacyMappedInput {
        val inputs = mutableListOf<String>()
        var summary = request.eventType!!.name

        when (request.eventType) {
            BallEventType.RUN -> {
                val runs = request.runs ?: 0
                inputs += runs.toString()
                summary = "Runs $runs"
            }

            BallEventType.EXTRA -> {
                val extras = request.extraRuns ?: 0
                inputs += "E"
                inputs += extras.toString()
                summary = "Extras $extras"
            }

            BallEventType.WIDE -> {
                val wideRuns = request.extraRuns ?: 1
                inputs += "W"
                if (wideRuns > 1) {
                    inputs += "y"
                    inputs += (wideRuns - 1).toString()
                } else {
                    inputs += ""
                }
                summary = "Wide $wideRuns"
            }

            BallEventType.NO_BALL -> {
                when (request.noBallMode ?: NoBallMode.STANDARD) {
                    NoBallMode.STANDARD -> {
                        val runs = request.runs ?: 0
                        inputs += "N"
                        inputs += ""
                        inputs += runs.toString()
                        summary = "No-ball bat $runs"
                    }

                    NoBallMode.EXTRA -> {
                        val extras = request.extraRuns ?: 0
                        inputs += "N"
                        inputs += "E"
                        inputs += extras.toString()
                        summary = "No-ball extra $extras"
                    }

                    NoBallMode.RUN_OUT -> {
                        inputs += "N"
                        inputs += "O"
                        inputs += mapRunoutSequence(session, runner, request)
                        summary = "No-ball runout"
                    }
                }
            }

            BallEventType.WICKET -> {
                when (request.dismissalType ?: DismissalType.REGULAR) {
                    DismissalType.REGULAR -> {
                        inputs += "O"
                        inputs += ""
                        if (!runner.isAllOutAfterNextWicket(session)) {
                            inputs += playerName(session, request.nextBatsmanId)
                        }
                        summary = "Wicket"
                    }

                    DismissalType.RUN_OUT -> {
                        inputs += "O"
                        inputs += "y"
                        inputs += mapRunoutSequence(session, runner, request)
                        summary = "Runout"
                    }
                }
            }
        }

        advanceTrackedBallState(session, runner, request, inputs)
        return LegacyMappedInput(inputs, summary)
    }

    private fun mapRunoutSequence(
        session: LegacyMatchSession,
        runner: LegacyInningsRunner,
        request: SubmitBallRequest,
    ): List<String> {
        val strikerId = currentStrikerId(session, runner)
        val nonStrikerId = currentNonStrikerId(session, runner)
        val outPlayerId = request.outPlayerId ?: throw BadRequestException("outPlayerId is required for runout")
        val whoRunOut = when (outPlayerId) {
            strikerId -> "1"
            nonStrikerId -> "2"
            else -> throw BadRequestException("Runout outPlayerId must match the current striker or non-striker")
        }

        val inputs = mutableListOf<String>()
        inputs += (request.runoutRuns ?: 0).toString()
        inputs += whoRunOut
        inputs += when (request.runoutEnd!!) {
            RunoutEnd.KEEPER_END -> "1"
            RunoutEnd.BOWLER_END -> "2"
        }
        if (!runner.isAllOutAfterNextWicket(session)) {
            inputs += playerName(session, request.nextBatsmanId)
        }
        return inputs
    }

    private fun requiresExplicitNextBowler(runner: LegacyInningsRunner, request: SubmitBallRequest): Boolean {
        val legalBall = request.eventType in setOf(BallEventType.RUN, BallEventType.EXTRA, BallEventType.WICKET)
        return legalBall && runner.meta.ballInOver == 5 && !runner.isTerminalBall(request)
    }

    private fun advanceTrackedBallState(
        session: LegacyMatchSession,
        runner: LegacyInningsRunner,
        request: SubmitBallRequest,
        inputs: MutableList<String>,
    ) {
        val legalBall = request.eventType in setOf(BallEventType.RUN, BallEventType.EXTRA, BallEventType.WICKET)
        if (!legalBall) {
            return
        }
        val overEnds = runner.meta.ballInOver == 5
        if (!overEnds) {
            runner.meta = runner.meta.copy(ballInOver = runner.meta.ballInOver + 1)
            return
        }
        if (runner.isTerminalBall(request)) {
            runner.meta = runner.meta.copy(overNumber = runner.meta.overNumber + 1, ballInOver = 0)
            return
        }
        val nextBowlerId = request.nextBowlerId ?: throw BadRequestException("nextBowlerId is required at over end")
        validateNextBowler(session, runner, nextBowlerId)
        runner.currentBowlerId = nextBowlerId
        runner.meta = LegacyMeta(
            overNumber = runner.meta.overNumber + 1,
            ballInOver = 0,
            currentBowlerId = nextBowlerId,
        )
        inputs += playerName(session, nextBowlerId)
    }

    private fun waitForLegacyProgress(session: LegacyMatchSession, beforeSignature: LegacySignature) {
        repeat(80) {
            Thread.sleep(25)
            val now = snapshotSignature(session)
            if (now != beforeSignature || session.activeInnings?.completed == true) {
                return
            }
        }
        // TODO: legacy Scanner prompts make exact progress detection heuristic-based for HTTP usage.
        throw BadRequestException("Legacy scoring engine did not advance; likely waiting for an unsupported prompt path")
    }

    private fun waitForThreadToBlock() {
        Thread.sleep(100)
    }

    private fun snapshotState(session: LegacyMatchSession): MatchState {
        val state = session.state
        val runner = session.activeInnings
        if (runner != null) {
            val battingTeam = session.teamById(runner.battingTeamId())
            val inningsState = state.innings.getOrPut(runner.inningsNo) {
                InningsState(
                    inningsNo = runner.inningsNo,
                    battingTeamId = battingTeam.getTeamId(),
                    bowlingTeamId = runner.bowlingTeamId(),
                    target = if (runner.inningsNo == 2) runner.target else null,
                )
            }

            val battingPlayers = session.playersByTeamId(runner.battingTeamId()).getALlPlayer()
            inningsState.batting.clear()
            battingPlayers.forEach { player ->
                inningsState.batting[player.playerId] = BatterState(
                    playerId = player.playerId,
                    runs = player.runsScored,
                    balls = player.ballsFaced,
                    fours = player.fours,
                    sixes = player.sixes,
                    out = player.isOut,
                    battingStatus = if (player.isOut) "OUT" else if (playerIsPlaying(player)) "PLAYING" else "PENDING",
                )
            }

            val bowlingPlayers = session.playersByTeamId(runner.bowlingTeamId()).getALlPlayer()
                .filter { player ->
                    val role = playerRepository.findById(player.playerId).orElse(null)?.role
                    role == PlayerRole.BOWLER || role == PlayerRole.ALLROUNDER
                }
            inningsState.bowling.clear()
            bowlingPlayers.forEach { player ->
                inningsState.bowling[player.playerId] = BowlerState(
                    playerId = player.playerId,
                    wickets = player.wickets,
                    ballsBowled = player.oversBowled * 6 + if (runner.currentBowlerId == player.playerId) runner.meta.ballInOver else 0,
                    runsConceded = player.runsGiven,
                )
            }

            val striker = battingPlayers.firstOrNull { it.isOnStrike } ?: battingPlayers.firstOrNull { playerIsPlaying(it) }
            val nonStriker = battingPlayers.firstOrNull { !it.isOnStrike && playerIsPlaying(it) }

            inningsState.totalRuns = teamTotalRuns(battingTeam)
            inningsState.wickets = teamWickets(battingTeam)
            inningsState.overNumber = runner.meta.overNumber
            inningsState.ballInOver = runner.meta.ballInOver
            inningsState.strikerId = striker?.playerId
            inningsState.nonStrikerId = nonStriker?.playerId
            inningsState.currentBowlerId = runner.currentBowlerId
            inningsState.freeHitActive = teamFreeHitActive(battingTeam)
            inningsState.completed = runner.completed
            inningsState.events.clear()
            inningsState.events.addAll(runner.events)
            state.currentInnings = runner.inningsNo
        }

        state.status = when {
            session.activeInnings?.completed == true && session.activeInnings?.inningsNo == 2 -> "COMPLETED"
            session.activeInnings != null -> "LIVE"
            else -> state.status
        }
        states[state.matchId] = state
        return state
    }

    private fun snapshotSignature(session: LegacyMatchSession): LegacySignature {
        val runner = session.activeInnings
        val battingTeam = runner?.let { session.teamById(it.battingTeamId()) }
        val battingPlayers = battingTeam?.let { session.playersByTeamId(it.getTeamId()).getALlPlayer() }.orEmpty()
        return LegacySignature(
            runs = battingTeam?.let(::teamTotalRuns) ?: 0,
            wickets = battingTeam?.let(::teamWickets) ?: 0,
            batterBalls = battingPlayers.sumOf { it.ballsFaced },
            batterRuns = battingPlayers.sumOf { it.runsScored },
        )
    }

    private fun ensureRunnerHealthy(runner: LegacyInningsRunner) {
        val failure = runner.failure.get() ?: return
        throw BadRequestException("Legacy scoring engine failed: ${failure.cause?.message ?: failure.message}")
    }

    private fun currentStrikerId(session: LegacyMatchSession, runner: LegacyInningsRunner): Int =
        session.playersByTeamId(runner.battingTeamId()).getALlPlayer().firstOrNull { it.isOnStrike }?.playerId
            ?: throw BadRequestException("Unable to resolve current striker from legacy state")

    private fun currentNonStrikerId(session: LegacyMatchSession, runner: LegacyInningsRunner): Int =
        session.playersByTeamId(runner.battingTeamId()).getALlPlayer().firstOrNull { !it.isOnStrike && playerIsPlaying(it) }?.playerId
            ?: throw BadRequestException("Unable to resolve current non-striker from legacy state")

    private fun playerName(session: LegacyMatchSession, playerId: Int?): String {
        val id = playerId ?: throw BadRequestException("Player id is required")
        return session.findPlayer(id).getPlayerName()
    }

    private fun playerIsPlaying(player: LinkedListOfPlayer.Player): Boolean =
        player.javaClass.superclass.getDeclaredField("isPlaying").apply { isAccessible = true }.getBoolean(player)

    private fun teamTotalRuns(team: LegacyTeam): Int =
        team.javaClass.getDeclaredField("totalRun").apply { isAccessible = true }.getInt(team)

    private fun teamWickets(team: LegacyTeam): Int =
        team.javaClass.getDeclaredField("wicketDown").apply { isAccessible = true }.getInt(team)

    private fun teamFreeHitActive(team: LegacyTeam): Boolean =
        team.javaClass.getDeclaredField("freeHitActive").apply { isAccessible = true }.getBoolean(team)

    private fun staticField(name: String): Field = matchSimulationClass.getDeclaredField(name).apply { isAccessible = true }

    private fun LinkedListOfPlayer.getPlayerById(playerId: Int): LinkedListOfPlayer.Player =
        getALlPlayer().firstOrNull { it.playerId == playerId }
            ?: throw BadRequestException("Legacy player $playerId not found in roster")

    private data class LegacySignature(
        val runs: Int,
        val wickets: Int,
        val batterBalls: Int,
        val batterRuns: Int,
    )

    private data class LegacyMeta(
        val overNumber: Int = 0,
        val ballInOver: Int = 0,
        val currentBowlerId: Int? = null,
    )

    private data class LegacyStepSnapshot(
        val meta: LegacyMeta,
        val eventCount: Int,
    )

    private data class LegacyMappedInput(
        val inputs: List<String>,
        val summary: String,
    )

    private data class LegacyInningsRunner(
        val session: LegacyMatchSession,
        val inningsNo: Int,
        val battingTeam: LegacyTeam,
        val isTeam1Batting: Boolean,
        val target: Int,
        val failure: AtomicReference<Exception?> = AtomicReference(null),
        val history: ArrayDeque<LegacyStepSnapshot> = ArrayDeque(),
        val events: MutableList<BallEventState> = mutableListOf(),
        @Volatile var currentBowlerId: Int? = null,
        @Volatile var meta: LegacyMeta = LegacyMeta(),
    ) {
        @Volatile
        var completed: Boolean = false

        @Volatile
        var thread: Thread? = null

        fun battingTeamId(): Int = battingTeam.getTeamId()

        fun bowlingTeamId(): Int = if (isTeam1Batting) session.team2.getTeamId() else session.team1.getTeamId()

        fun isAllOutAfterNextWicket(session: LegacyMatchSession): Boolean =
            session.teamWickets(battingTeam) + 1 >= session.playerCountForTeam(battingTeam.getTeamId()) - 1

        fun isTerminalBall(request: SubmitBallRequest): Boolean {
            if (request.eventType == BallEventType.WICKET && isAllOutAfterNextWicket(session)) {
                return true
            }
            val nextOver = if (meta.ballInOver == 5 && request.eventType in setOf(BallEventType.RUN, BallEventType.EXTRA, BallEventType.WICKET)) {
                meta.overNumber + 1
            } else {
                meta.overNumber
            }
            if (nextOver >= session.state.oversLimit) {
                return true
            }
            if (inningsNo == 2) {
                val projected = session.teamTotalRuns(battingTeam) + when (request.eventType) {
                    BallEventType.RUN -> request.runs ?: 0
                    BallEventType.EXTRA -> request.extraRuns ?: 0
                    BallEventType.WIDE -> request.extraRuns ?: 1
                    BallEventType.NO_BALL -> when (request.noBallMode ?: NoBallMode.STANDARD) {
                        NoBallMode.STANDARD -> 1 + (request.runs ?: 0)
                        NoBallMode.EXTRA -> 1 + (request.extraRuns ?: 0)
                        NoBallMode.RUN_OUT -> 1 + (request.runoutRuns ?: 0)
                    }

                    BallEventType.WICKET -> if ((request.dismissalType ?: DismissalType.REGULAR) == DismissalType.RUN_OUT) {
                        request.runoutRuns ?: 0
                    } else {
                        0
                    }

                    null -> 0
                }
                if (projected >= target) {
                    return true
                }
            }
            return false
        }
    }

    private data class LegacyMatchSession(
        val matchId: Int,
        val legacyMatch: LegacyMatch,
        val team1: LegacyTeam,
        val team2: LegacyTeam,
        val team1Players: LinkedListOfPlayer,
        val team2Players: LinkedListOfPlayer,
        val scannerInput: PipedInputStream,
        val state: MatchState,
    ) {
        lateinit var scanner: Scanner
        private lateinit var scannerOutput: PipedOutputStream
        private lateinit var writer: PrintWriter

        @Volatile
        var activeInnings: LegacyInningsRunner? = null

        fun connect() {
            scannerOutput = PipedOutputStream(scannerInput)
            writer = PrintWriter(scannerOutput, true)
            scanner = Scanner(scannerInput)
        }

        fun writeLine(value: String) {
            writer.println(value)
            writer.flush()
        }

        fun teamById(teamId: Int): LegacyTeam = when (teamId) {
            team1.getTeamId() -> team1
            team2.getTeamId() -> team2
            else -> throw BadRequestException("Legacy team $teamId not part of match $matchId")
        }

        fun opponentTeam(teamId: Int): LegacyTeam = if (team1.getTeamId() == teamId) team2 else team1

        fun playersByTeamId(teamId: Int): LinkedListOfPlayer = when (teamId) {
            team1.getTeamId() -> team1Players
            team2.getTeamId() -> team2Players
            else -> throw BadRequestException("Legacy roster for team $teamId not found")
        }

        fun playerCountForTeam(teamId: Int): Int = playersByTeamId(teamId).getALlPlayer().size

        fun firstInningsTarget(): Int {
            val first = state.innings[1] ?: throw BadRequestException("First innings has not been started")
            return first.totalRuns + 1
        }

        fun prepareForInningsStart(
            inningsNo: Int,
            opener1: LinkedListOfPlayer.Player,
            opener2: LinkedListOfPlayer.Player,
        ) {
            if (inningsNo == 1) {
                team1.setDefault()
                team2.setDefault()
                team1Players.setDefault()
                team2Players.setDefault()
            }
            opener1.setPlaying(true)
            opener1.setOnStrike(true)
            opener2.setPlaying(true)
            opener2.setOnStrike(false)
        }

        fun findPlayer(playerId: Int): LinkedListOfPlayer.Player =
            (team1Players.getALlPlayer() + team2Players.getALlPlayer()).firstOrNull { it.playerId == playerId }
                ?: throw BadRequestException("Legacy player $playerId not found")

        fun teamTotalRuns(team: LegacyTeam): Int =
            team.javaClass.getDeclaredField("totalRun").apply { isAccessible = true }.getInt(team)

        fun teamWickets(team: LegacyTeam): Int =
            team.javaClass.getDeclaredField("wicketDown").apply { isAccessible = true }.getInt(team)
    }
}
