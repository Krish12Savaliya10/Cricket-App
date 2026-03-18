package com.cricket.cricketbackend.service

import com.cricket.cricketbackend.dto.request.CreateTournamentRequest
import com.cricket.cricketbackend.dto.response.PointsTableEntryResponse
import com.cricket.cricketbackend.dto.response.TournamentResponse
import com.cricket.cricketbackend.exception.BadRequestException
import com.cricket.cricketbackend.model.entity.MatchType
import com.cricket.cricketbackend.model.entity.PointsTableEntity
import com.cricket.cricketbackend.model.entity.TeamEntity
import com.cricket.cricketbackend.model.entity.TournamentEntity
import com.cricket.cricketbackend.repository.MatchRepository
import com.cricket.cricketbackend.repository.PointsTableRepository
import com.cricket.cricketbackend.repository.TournamentRepository
import com.cricket.cricketbackend.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import kotlin.random.Random

@Service
class TournamentService(
    private val tournamentRepository: TournamentRepository,
    private val pointsTableRepository: PointsTableRepository,
    private val userRepository: UserRepository,
    private val teamService: TeamService,
    private val matchService: MatchService,
    private val matchRepository: MatchRepository,
) {
    @Transactional
    fun createTournament(request: CreateTournamentRequest): TournamentResponse {
        val teamIds = request.teamIds!!.distinct()
        if (teamIds.size < 6 || teamIds.size > 12 || teamIds.size % 2 != 0) {
            throw BadRequestException("Tournament requires an even number of teams between 6 and 12")
        }
        val host = userRepository.findById(request.hostUserId!!).orElseThrow()
        val teams = teamIds.map(teamService::getTeamEntity)
        val tournament = tournamentRepository.save(
            TournamentEntity(
                id = IdGenerator.nextLegacyStyleId(),
                tournamentName = request.tournamentName!!.trim().uppercase(),
                year = LocalDate.now().year,
                host = host,
                tournamentStatus = "UPCOMING",
            ),
        )
        teams.forEach { team ->
            pointsTableRepository.save(
                PointsTableEntity(
                    tournament = tournament,
                    team = team,
                ),
            )
        }
        val schedule = generateSchedule(teams, request.grouped!!, request.scheduleType ?: 1)
        val matches = schedule.mapIndexed { index, pair ->
            matchService.createScheduledMatch(
                hostUserId = host.id,
                team1Id = pair.first.id,
                team2Id = pair.second.id,
                overs = request.overs!!,
                matchType = MatchType.TOURNAMENT,
                matchTypeId = tournament.id,
                matchNumber = index + 1,
            )
        }
        return toResponse(tournament, teams, matches.map(matchService::toResponse))
    }

    fun getTournament(tournamentId: Int): TournamentResponse {
        val tournament = tournamentRepository.findById(tournamentId).orElseThrow()
        val matches = matchRepository.findAllByMatchTypeAndMatchTypeId(MatchType.TOURNAMENT, tournamentId)
            .sortedBy { it.matchNumber }
            .map(matchService::toResponse)
        val teams = pointsTableRepository.findAllByTournamentIdOrderByPointsDescNetRunRateDesc(tournamentId)
            .map { it.team!! }
        return toResponse(tournament, teams, matches)
    }

    fun listTournaments(status: String? = null): List<TournamentResponse> {
        val list = if (status != null) tournamentRepository.findAllByTournamentStatus(status.uppercase()) else tournamentRepository.findAll()
        return list.sortedBy { it.id }.map { tournament ->
            val matches = matchRepository.findAllByMatchTypeAndMatchTypeId(MatchType.TOURNAMENT, tournament.id)
                .sortedBy { it.matchNumber }
                .map(matchService::toResponse)
            val teams = pointsTableRepository.findAllByTournamentIdOrderByPointsDescNetRunRateDesc(tournament.id).map { it.team!! }
            toResponse(tournament, teams, matches)
        }
    }

    fun getPointsTable(tournamentId: Int): List<PointsTableEntryResponse> =
        pointsTableRepository.findAllByTournamentIdOrderByPointsDescNetRunRateDesc(tournamentId).map {
            PointsTableEntryResponse(
                teamId = it.team!!.id,
                teamName = it.team!!.teamName,
                points = it.points,
                matchesWon = it.matchesWon,
                matchesLost = it.matchesLost,
                matchesDrawn = it.matchesDrawn,
                netRunRate = it.netRunRate,
            )
        }

    private fun toResponse(
        tournament: TournamentEntity,
        teams: List<TeamEntity>,
        matches: List<com.cricket.cricketbackend.dto.response.MatchResponse>,
    ): TournamentResponse = TournamentResponse(
        id = tournament.id,
        tournamentName = tournament.tournamentName,
        hostUserId = tournament.host!!.id,
        year = tournament.year,
        status = tournament.tournamentStatus,
        winnerTeamId = tournament.winnerTeam?.id,
        teams = teams.map(teamService::toResponse),
        matches = matches,
    )

    private fun generateSchedule(teams: List<TeamEntity>, grouped: Boolean, scheduleType: Int): List<Pair<TeamEntity, TeamEntity>> {
        val shuffled = teams.shuffled(Random(System.currentTimeMillis()))
        val mid = shuffled.size / 2
        val group1 = shuffled.subList(0, mid).toMutableList()
        val group2 = shuffled.subList(mid, shuffled.size).toMutableList()
        return if (!grouped) {
            buildSameGroup(group1, group2) + buildCrossGroup(group1, group2)
        } else {
            when (scheduleType) {
                1 -> buildSameGroup(group1, group2)
                2 -> buildCrossGroup(group1, group2)
                3 -> buildCrossGroup(group1, group2) + buildSameGroup(group1.shuffled().toMutableList(), group2.shuffled().toMutableList()) +
                    buildCrossGroup(group2.shuffled().toMutableList(), group1.shuffled().toMutableList())
                4 -> buildSameGroup(group1, group2) + buildCrossGroup(group1.shuffled().toMutableList(), group2.shuffled().toMutableList()) +
                    buildSameGroup(group2.shuffled().toMutableList(), group1.shuffled().toMutableList())
                5 -> buildSameGroup(group1, group2) + buildCrossGroup(group2.shuffled().toMutableList(), group1.shuffled().toMutableList()) +
                    buildSameGroup(group2.shuffled().toMutableList(), group1.shuffled().toMutableList()) +
                    buildCrossGroup(group1.shuffled().toMutableList(), group2.shuffled().toMutableList())
                else -> throw BadRequestException("Unsupported tournament scheduleType $scheduleType")
            }
        }
    }

    private fun buildSameGroup(group1: MutableList<TeamEntity>, group2: MutableList<TeamEntity>): List<Pair<TeamEntity, TeamEntity>> {
        val list = mutableListOf<Pair<TeamEntity, TeamEntity>>()
        for (i in 0 until group1.size) {
            for (j in i + 1 until group1.size) {
                list += randomOrder(group1[i], group1[j])
                list += randomOrder(group2[i], group2[j])
            }
        }
        return list.shuffled()
    }

    private fun buildCrossGroup(group1: MutableList<TeamEntity>, group2: MutableList<TeamEntity>): List<Pair<TeamEntity, TeamEntity>> {
        val list = mutableListOf<Pair<TeamEntity, TeamEntity>>()
        val rotating = group2.toMutableList()
        for (i in group1.indices) {
            if (i != 0) {
                val first = rotating.removeFirst()
                rotating.add(first)
            }
            for (j in rotating.indices) {
                list += randomOrder(group1[j], rotating[j])
            }
        }
        return list
    }

    private fun randomOrder(team1: TeamEntity, team2: TeamEntity): Pair<TeamEntity, TeamEntity> =
        if (Random.nextBoolean()) team1 to team2 else team2 to team1
}
