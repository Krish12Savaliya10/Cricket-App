package com.cricket.cricketbackend.model.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "Matches")
class MatchEntity(
    @Id
    @Column(name = "match_id")
    var id: Int = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Host_Id", nullable = false)
    var host: UserEntity? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team1_id", nullable = false)
    var team1: TeamEntity? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team2_id", nullable = false)
    var team2: TeamEntity? = null,
    @Column(name = "Match_Number")
    var matchNumber: Int = 1,
    @Column(name = "inning_overs", nullable = false)
    var inningOvers: Int = 0,
    @Enumerated(EnumType.STRING)
    @Column(name = "match_type", nullable = false)
    var matchType: MatchType = MatchType.SINGLE,
    @Column(name = "match_type_id")
    var matchTypeId: Int? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "match_status")
    var matchStatus: MatchStatus = MatchStatus.UPCOMING,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_team_id")
    var winnerTeam: TeamEntity? = null,
)

enum class MatchType {
    SINGLE,
    SERIES,
    TOURNAMENT,
    SEMI1,
    SEMI2,
    FINAL,
}

enum class MatchStatus {
    UPCOMING,
    LIVE,
    COMPLETED,
}
