package com.cricket.cricketbackend.service

import com.cricket.cricketbackend.model.entity.BallByBallEntity
import com.cricket.cricketbackend.model.entity.ExtraType
import com.cricket.cricketbackend.model.entity.InningEntity
import com.cricket.cricketbackend.model.entity.MatchEntity
import com.cricket.cricketbackend.model.entity.MatchStatus
import com.cricket.cricketbackend.model.entity.MatchType
import com.cricket.cricketbackend.model.entity.PlayerEntity
import com.cricket.cricketbackend.model.entity.TeamEntity
import com.cricket.cricketbackend.model.entity.UserEntity
import com.cricket.cricketbackend.model.entity.WicketType
import com.cricket.cricketbackend.repository.BallByBallRepository
import com.cricket.cricketbackend.repository.InningRepository
import com.cricket.cricketbackend.repository.MatchRepository
import com.cricket.cricketbackend.repository.PlayerRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class ScoreServiceTest {
    private val service = ScoreService(
        matchRepository = Mockito.mock(MatchRepository::class.java),
        inningRepository = Mockito.mock(InningRepository::class.java),
        ballByBallRepository = Mockito.mock(BallByBallRepository::class.java),
        playerRepository = Mockito.mock(PlayerRepository::class.java),
    )

    private val host = UserEntity(id = 7, fullName = "HOST", email = "host@test.com", password = "pw", role = "HOST")
    private val team1 = TeamEntity(id = 11, teamName = "TEAM ONE", host = host)
    private val team2 = TeamEntity(id = 22, teamName = "TEAM TWO", host = host)
    private val match = MatchEntity(
        id = 101,
        host = host,
        team1 = team1,
        team2 = team2,
        inningOvers = 20,
        matchType = MatchType.SINGLE,
        matchStatus = MatchStatus.LIVE,
    )
    private val innings = InningEntity(
        id = 201,
        match = match,
        inningsNo = 1,
        battingTeam = team1,
        bowlingTeam = team2,
        strikerId = 1,
        nonStrikerId = 2,
        currentBowlerId = 9,
    )

    @Test
    fun `consecutive wides keep over on same legal ball and add extras`() {
        val state = service.rebuildState(
            match,
            innings,
            listOf(
                ball(sequenceNo = 1, overNumber = 0, ballNumber = 1, runs = 0, extraType = ExtraType.WIDE),
                ball(sequenceNo = 2, overNumber = 0, ballNumber = 1, runs = 0, extraType = ExtraType.WIDE),
            ),
        )

        assertEquals(2, state.totalRuns)
        assertEquals(0, state.legalBalls)
        assertEquals(0, state.completedOvers)
        assertEquals(0, state.ballsInCurrentOver)
    }

    @Test
    fun `no ball plus run out adds total but does not credit bowler wicket or legal ball`() {
        val state = service.rebuildState(
            match,
            innings,
            listOf(
                ball(
                    sequenceNo = 1,
                    overNumber = 0,
                    ballNumber = 1,
                    runs = 2,
                    extraType = ExtraType.NOBALL,
                    isWicket = true,
                    wicketType = WicketType.RUN_OUT,
                    dismissedPlayerId = 2,
                    nextBatsmanId = 3,
                ),
            ),
        )

        assertEquals(3, state.totalRuns)
        assertEquals(1, state.wickets)
        assertEquals(0, state.legalBalls)
        assertEquals(0, state.bowling.getValue(9).wickets)
        assertEquals(3, state.nonStrikerId)
    }

    @Test
    fun `run out with runs rotates strike before replacement`() {
        val state = service.rebuildState(
            match,
            innings,
            listOf(
                ball(
                    sequenceNo = 1,
                    overNumber = 0,
                    ballNumber = 1,
                    runs = 1,
                    extraType = ExtraType.NONE,
                    isWicket = true,
                    wicketType = WicketType.RUN_OUT,
                    dismissedPlayerId = 2,
                    nextBatsmanId = 3,
                ),
            ),
        )

        assertEquals(1, state.totalRuns)
        assertEquals(1, state.legalBalls)
        assertEquals(3, state.strikerId)
        assertEquals(1, state.nonStrikerId)
    }

    private fun ball(
        sequenceNo: Int,
        overNumber: Int,
        ballNumber: Int,
        runs: Int,
        extraType: ExtraType,
        isWicket: Boolean = false,
        wicketType: WicketType? = null,
        dismissedPlayerId: Int? = null,
        nextBatsmanId: Int? = null,
        nextBowlerId: Int? = null,
    ) = BallByBallEntity(
        id = 500 + sequenceNo,
        match = match,
        innings = innings,
        sequenceNo = sequenceNo,
        overNumber = overNumber,
        ballNumber = ballNumber,
        strikerId = 1,
        nonStrikerId = 2,
        bowlerId = 9,
        runs = runs,
        extras = when (extraType) {
            ExtraType.NONE -> 0
            ExtraType.BYE, ExtraType.LEGBYE -> runs
            ExtraType.WIDE, ExtraType.NOBALL -> runs + 1
        },
        extraType = extraType,
        isWicket = isWicket,
        wicketType = wicketType,
        dismissedPlayerId = dismissedPlayerId,
        nextBatsmanId = nextBatsmanId,
        nextBowlerId = nextBowlerId,
    )
}
