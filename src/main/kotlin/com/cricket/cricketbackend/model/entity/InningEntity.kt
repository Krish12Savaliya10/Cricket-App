package com.cricket.cricketbackend.model.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "innings")
class InningEntity(
    @Id
    @Column(name = "innings_id")
    var id: Int = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    var match: MatchEntity? = null,
    @Column(name = "innings_no", nullable = false)
    var inningsNo: Int = 1,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batting_team_id", nullable = false)
    var battingTeam: TeamEntity? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bowling_team_id", nullable = false)
    var bowlingTeam: TeamEntity? = null,
    @Column(name = "striker_id", nullable = false)
    var strikerId: Int = 0,
    @Column(name = "non_striker_id", nullable = false)
    var nonStrikerId: Int = 0,
    @Column(name = "current_bowler_id", nullable = false)
    var currentBowlerId: Int = 0,
    @Column(name = "target_runs")
    var targetRuns: Int? = null,
    @Column(name = "completed", nullable = false)
    var completed: Boolean = false,
)
