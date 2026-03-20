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
@Table(name = "ball_by_ball")
class BallByBallEntity(
    @Id
    @Column(name = "id")
    var id: Int = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    var match: MatchEntity? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "innings_id", nullable = false)
    var innings: InningEntity? = null,
    @Column(name = "sequence_no", nullable = false)
    var sequenceNo: Int = 0,
    @Column(name = "over_number", nullable = false)
    var overNumber: Int = 0,
    @Column(name = "ball_number", nullable = false)
    var ballNumber: Int = 0,
    @Column(name = "striker_id", nullable = false)
    var strikerId: Int = 0,
    @Column(name = "non_striker_id", nullable = false)
    var nonStrikerId: Int = 0,
    @Column(name = "bowler_id", nullable = false)
    var bowlerId: Int = 0,
    @Column(name = "runs", nullable = false)
    var runs: Int = 0,
    @Column(name = "extras", nullable = false)
    var extras: Int = 0,
    @Enumerated(EnumType.STRING)
    @Column(name = "extra_type", nullable = false)
    var extraType: ExtraType = ExtraType.NONE,
    @Column(name = "is_wicket", nullable = false)
    var isWicket: Boolean = false,
    @Enumerated(EnumType.STRING)
    @Column(name = "wicket_type")
    var wicketType: WicketType? = null,
    @Column(name = "dismissed_player_id")
    var dismissedPlayerId: Int? = null,
    @Column(name = "next_batsman_id")
    var nextBatsmanId: Int? = null,
    @Column(name = "next_bowler_id")
    var nextBowlerId: Int? = null,
)

enum class ExtraType {
    NONE,
    WIDE,
    NOBALL,
    BYE,
    LEGBYE,
}

enum class WicketType {
    BOWLED,
    CAUGHT,
    LBW,
    STUMPED,
    RUN_OUT,
    HIT_WICKET,
    RETIRED_OUT,
}
