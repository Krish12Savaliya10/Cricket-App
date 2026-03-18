package com.cricket.cricketbackend.model.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@IdClass(TeamMatchStatsId::class)
@Table(name = "TeamMatchStats")
class TeamMatchStatsEntity(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    var match: MatchEntity? = null,
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    var team: TeamEntity? = null,
    @Column(name = "tournament_id")
    var tournamentId: Int? = null,
    @Column(name = "runs_scored")
    var runsScored: Int = 0,
    @Column(name = "wickets_lost")
    var wicketsLost: Int = 0,
    @Column(name = "overs_played")
    var oversPlayed: Double = 0.0,
    @Column(name = "result")
    var result: String? = null,
)
