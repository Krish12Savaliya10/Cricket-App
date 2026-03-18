package com.cricket.cricketbackend.dto.response

data class AudienceResponse(
    val liveMatches: List<MatchResponse>,
    val upcomingMatches: List<MatchResponse>,
    val completedMatches: List<MatchResponse>,
    val liveSeries: List<SeriesResponse>,
    val upcomingSeries: List<SeriesResponse>,
    val completedSeries: List<SeriesResponse>,
    val liveTournaments: List<TournamentResponse>,
    val upcomingTournaments: List<TournamentResponse>,
    val completedTournaments: List<TournamentResponse>,
)
