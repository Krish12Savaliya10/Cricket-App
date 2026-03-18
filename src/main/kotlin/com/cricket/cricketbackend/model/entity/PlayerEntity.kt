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
@Table(name = "Players")
class PlayerEntity(
    @Id
    @Column(name = "player_id")
    var id: Int = 0,
    @Column(name = "player_name", nullable = false)
    var playerName: String = "",
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    var role: PlayerRole = PlayerRole.BATSMAN,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    var team: TeamEntity? = null,
)

enum class PlayerRole {
    BATSMAN,
    BOWLER,
    ALLROUNDER,
}
