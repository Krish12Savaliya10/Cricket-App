package com.cricket.cricketbackend.model.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "series")
class SeriesEntity(
    @Id
    @Column(name = "series_id")
    var id: Int = 0,
    @Column(name = "series_name", nullable = false)
    var seriesName: String = "",
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    var host: UserEntity? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team1_id", nullable = false)
    var team1: TeamEntity? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team2_id", nullable = false)
    var team2: TeamEntity? = null,
    @Column(name = "total_matches", nullable = false)
    var totalMatches: Int = 0,
    @Column(name = "completed_matches")
    var completedMatches: Int = 0,
    @Column(name = "series_status")
    var seriesStatus: String = "UPCOMING",
    @Column(name = "year")
    var year: Int? = null,
)
