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
@IdClass(PlayerMatchStatsId::class)
@Table(name = "PlayerMatchStats")
class PlayerMatchStatsEntity(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    var match: MatchEntity? = null,
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    var player: PlayerEntity? = null,
    @Column(name = "runs_scored")
    var runsScored: Int = 0,
    @Column(name = "balls_faced")
    var ballsFaced: Int = 0,
    @Column(name = "fours")
    var fours: Int = 0,
    @Column(name = "sixes")
    var sixes: Int = 0,
    @Column(name = "wickets")
    var wickets: Int = 0,
    @Column(name = "overs_bowled")
    var oversBowled: Double = 0.0,
    @Column(name = "runs_conceded")
    var runsConceded: Int = 0,
    @Column(name = "BattingStats")
    var battingStatus: String? = null,
)
