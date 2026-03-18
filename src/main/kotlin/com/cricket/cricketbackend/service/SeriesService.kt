package com.cricket.cricketbackend.service

import com.cricket.cricketbackend.dto.request.CreateSeriesRequest
import com.cricket.cricketbackend.dto.response.SeriesResponse
import com.cricket.cricketbackend.model.entity.MatchType
import com.cricket.cricketbackend.model.entity.SeriesEntity
import com.cricket.cricketbackend.repository.MatchRepository
import com.cricket.cricketbackend.repository.SeriesRepository
import com.cricket.cricketbackend.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class SeriesService(
    private val seriesRepository: SeriesRepository,
    private val userRepository: UserRepository,
    private val teamService: TeamService,
    private val matchService: MatchService,
    private val matchRepository: MatchRepository,
) {
    @Transactional
    fun createSeries(request: CreateSeriesRequest): SeriesResponse {
        val host = userRepository.findById(request.hostUserId!!).orElseThrow()
        val team1 = teamService.getTeamEntity(request.team1Id!!)
        val team2 = teamService.getTeamEntity(request.team2Id!!)
        val series = seriesRepository.save(
            SeriesEntity(
                id = IdGenerator.nextLegacyStyleId(),
                seriesName = request.seriesName!!.trim().uppercase(),
                host = host,
                team1 = team1,
                team2 = team2,
                totalMatches = request.totalMatches!!,
                completedMatches = 0,
                seriesStatus = "UPCOMING",
                year = LocalDate.now().year,
            ),
        )
        val matches = (1..request.totalMatches).map { matchNumber ->
            matchService.createScheduledMatch(
                hostUserId = host.id,
                team1Id = team1.id,
                team2Id = team2.id,
                overs = request.overs!!,
                matchType = MatchType.SERIES,
                matchTypeId = series.id,
                matchNumber = matchNumber,
            )
        }
        return toResponse(series, matches.map(matchService::toResponse))
    }

    fun getSeries(seriesId: Int): SeriesResponse {
        val series = seriesRepository.findById(seriesId).orElseThrow()
        val matches = matchRepository.findAllByMatchTypeAndMatchTypeId(MatchType.SERIES, seriesId)
            .sortedBy { it.matchNumber }
            .map(matchService::toResponse)
        return toResponse(series, matches)
    }

    fun listSeries(status: String? = null): List<SeriesResponse> {
        val list = if (status != null) seriesRepository.findAllBySeriesStatus(status.uppercase()) else seriesRepository.findAll()
        return list.sortedBy { it.id }.map { toResponse(it, matchRepository.findAllByMatchTypeAndMatchTypeId(MatchType.SERIES, it.id).sortedBy { m -> m.matchNumber }.map(matchService::toResponse)) }
    }

    private fun toResponse(series: SeriesEntity, matches: List<com.cricket.cricketbackend.dto.response.MatchResponse>): SeriesResponse =
        SeriesResponse(
            id = series.id,
            seriesName = series.seriesName,
            hostUserId = series.host!!.id,
            team1 = teamService.toResponse(series.team1!!),
            team2 = teamService.toResponse(series.team2!!),
            totalMatches = series.totalMatches,
            completedMatches = series.completedMatches,
            status = series.seriesStatus,
            year = series.year,
            matches = matches,
        )
}
