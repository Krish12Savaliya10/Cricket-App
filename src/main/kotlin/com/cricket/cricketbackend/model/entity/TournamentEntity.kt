package com.cricket.cricketbackend.model.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "Tournaments")
class TournamentEntity(
    @Id
    @Column(name = "tournament_id")
    var id: Int = 0,
    @Column(name = "tournament_name", nullable = false)
    var tournamentName: String = "",
    @Column(name = "year", nullable = false)
    var year: Int = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    var host: UserEntity? = null,
    @Column(name = "tournament_status")
    var tournamentStatus: String = "UPCOMING",
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_team_id")
    var winnerTeam: TeamEntity? = null,
)
