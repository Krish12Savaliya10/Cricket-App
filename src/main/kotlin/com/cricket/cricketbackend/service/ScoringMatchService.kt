package com.cricket.cricketbackend.service

import com.cricket.cricketbackend.dto.request.CreateMatchRequest
import com.cricket.cricketbackend.dto.request.EndMatchRequest
import com.cricket.cricketbackend.dto.request.StartMatchRequest
import com.cricket.cricketbackend.dto.response.MatchState
import com.cricket.cricketbackend.dto.response.ScoringMatchResponse
import com.cricket.cricketbackend.exception.BadRequestException
import com.cricket.cricketbackend.exception.ResourceNotFoundException
import com.cricket.cricketbackend.model.entity.InningEntity
import com.cricket.cricketbackend.model.entity.MatchEntity
import com.cricket.cricketbackend.model.entity.MatchStatus
import com.cricket.cricketbackend.model.entity.MatchType
import com.cricket.cricketbackend.repository.InningRepository
import com.cricket.cricketbackend.repository.MatchRepository
import com.cricket.cricketbackend.repository.PlayerRepository
import com.cricket.cricketbackend.repository.TeamRepository
import com.cricket.cricketbackend.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ScoringMatchService(
    private val matchRepository: MatchRepository,
    private val inningRepository: InningRepository,
    private val userRepository: UserRepository,
    private val teamRepository: TeamRepository,
    private val playerRepository: PlayerRepository,
    private val scoreService: ScoreService,
) {
    @Transactional
    fun create(request: CreateMatchRequest): ScoringMatchResponse {
        val host = userRepository.findById(request.hostUserId!!).orElseThrow {
            ResourceNotFoundException("Host user ${request.hostUserId} not found")
        }
        val team1 = teamRepository.findById(request.team1Id!!).orElseThrow {
            ResourceNotFoundException("Team ${request.team1Id} not found")
        }
        val team2 = teamRepository.findById(request.team2Id!!).orElseThrow {
            ResourceNotFoundException("Team ${request.team2Id} not found")
        }
        if (team1.id == team2.id) {
            throw BadRequestException("Teams must be different")
        }
        val match = matchRepository.save(
            MatchEntity(
                id = IdGenerator.nextLegacyStyleId(),
                host = host,
                team1 = team1,
                team2 = team2,
                matchNumber = 1,
                inningOvers = request.overs!!,
                matchType = MatchType.SINGLE,
                matchStatus = MatchStatus.UPCOMING,
            ),
        )
        return match.toResponse()
    }

    @Transactional
    fun start(request: StartMatchRequest): MatchState {
        val match = getMatch(request.matchId!!)
        if (match.matchStatus == MatchStatus.COMPLETED) {
            throw BadRequestException("Completed match cannot be started again")
        }
        if (inningRepository.findByMatchIdAndInningsNo(match.id, request.inningsNo!! ) != null) {
            throw BadRequestException("Innings ${request.inningsNo} already exists for match ${match.id}")
        }
        if (inningRepository.findByMatchIdAndCompletedFalseOrderByInningsNoAsc(match.id).isNotEmpty()) {
            throw BadRequestException("Another innings is already active for match ${match.id}")
        }
        val battingTeam = when (request.battingTeamId) {
            match.team1!!.id -> match.team1!!
            match.team2!!.id -> match.team2!!
            else -> throw BadRequestException("Batting team ${request.battingTeamId} does not belong to match ${match.id}")
        }
        val bowlingTeam = if (battingTeam.id == match.team1!!.id) match.team2!! else match.team1!!
        validatePlayerTeam(request.strikerId!!, battingTeam.id)
        validatePlayerTeam(request.nonStrikerId!!, battingTeam.id)
        validatePlayerTeam(request.bowlerId!!, bowlingTeam.id)
        if (request.strikerId == request.nonStrikerId) {
            throw BadRequestException("Striker and non-striker must be different players")
        }

        val innings = inningRepository.save(
            InningEntity(
                id = IdGenerator.nextLegacyStyleId(),
                match = match,
                inningsNo = request.inningsNo,
                battingTeam = battingTeam,
                bowlingTeam = bowlingTeam,
                strikerId = request.strikerId,
                nonStrikerId = request.nonStrikerId,
                currentBowlerId = request.bowlerId,
            ),
        )
        match.matchStatus = MatchStatus.LIVE
        matchRepository.save(match)
        return scoreService.getMatchState(match.id)
    }

    @Transactional
    fun end(request: EndMatchRequest): ScoringMatchResponse {
        val match = getMatch(request.matchId!!)
        inningRepository.findByMatchIdAndCompletedFalseOrderByInningsNoAsc(match.id).forEach {
            it.completed = true
            inningRepository.save(it)
        }
        match.matchStatus = MatchStatus.COMPLETED
        return matchRepository.save(match).toResponse(currentState = inningRepository.findFirstByMatchIdOrderByInningsNoDesc(match.id)?.let {
            scoreService.getMatchState(match.id)
        })
    }

    @Transactional(readOnly = true)
    fun get(matchId: Int): ScoringMatchResponse {
        val match = getMatch(matchId)
        val state = inningRepository.findFirstByMatchIdOrderByInningsNoDesc(match.id)?.let { scoreService.getMatchState(match.id) }
        return match.toResponse(state)
    }

    private fun getMatch(matchId: Int): MatchEntity = matchRepository.findById(matchId).orElseThrow {
        ResourceNotFoundException("Match $matchId not found")
    }

    private fun validatePlayerTeam(playerId: Int, teamId: Int) {
        val player = playerRepository.findById(playerId).orElseThrow {
            ResourceNotFoundException("Player $playerId not found")
        }
        if (player.team!!.id != teamId) {
            throw BadRequestException("Player $playerId does not belong to team $teamId")
        }
    }
    private fun MatchEntity.toResponse(currentState: MatchState? = null): ScoringMatchResponse = ScoringMatchResponse(
        id = id,
        hostUserId = host!!.id,
        team1Id = team1!!.id,
        team2Id = team2!!.id,
        overs = inningOvers,
        status = matchStatus.name,
        currentState = currentState,
    )
}
