package com.cricket.cricketbackend.model.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "Teams")
class TeamEntity(
    @Id
    @Column(name = "team_id")
    var id: Int = 0,
    @Column(name = "team_name", nullable = false)
    var teamName: String = "",
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Host_id", nullable = false)
    var host: UserEntity? = null,
)
