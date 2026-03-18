package com.cricket.cricketbackend.service

import com.cricket.cricketbackend.dto.response.AudienceResponse
import com.cricket.cricketbackend.model.entity.MatchStatus
import org.springframework.stereotype.Service

@Service
class AudienceService(
    private val matchService: MatchService,
    private val seriesService: SeriesService,
    private val tournamentService: TournamentService,
) {
    fun getDashboard(): AudienceResponse = AudienceResponse(
        liveMatches = matchService.listMatches(status = MatchStatus.LIVE),
        upcomingMatches = matchService.listMatches(status = MatchStatus.UPCOMING),
        completedMatches = matchService.listMatches(status = MatchStatus.COMPLETED),
        liveSeries = seriesService.listSeries("LIVE"),
        upcomingSeries = seriesService.listSeries("UPCOMING"),
        completedSeries = seriesService.listSeries("COMPLETED"),
        liveTournaments = tournamentService.listTournaments("LIVE"),
        upcomingTournaments = tournamentService.listTournaments("UPCOMING"),
        completedTournaments = tournamentService.listTournaments("COMPLETED"),
    )
}
