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
@IdClass(PointsTableId::class)
@Table(name = "PointsTable")
class PointsTableEntity(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    var tournament: TournamentEntity? = null,
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    var team: TeamEntity? = null,
    @Column(name = "points")
    var points: Int = 0,
    @Column(name = "matches_won")
    var matchesWon: Int = 0,
    @Column(name = "matches_lost")
    var matchesLost: Int = 0,
    @Column(name = "matches_drawn")
    var matchesDrawn: Int = 0,
    @Column(name = "net_run_rate")
    var netRunRate: Double = 0.0,
)
