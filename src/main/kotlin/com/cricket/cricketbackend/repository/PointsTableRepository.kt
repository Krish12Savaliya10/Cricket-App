package com.cricket.cricketbackend.repository

import com.cricket.cricketbackend.model.entity.PointsTableEntity
import com.cricket.cricketbackend.model.entity.PointsTableId
import org.springframework.data.jpa.repository.JpaRepository

interface PointsTableRepository : JpaRepository<PointsTableEntity, PointsTableId> {
    fun findAllByTournamentIdOrderByPointsDescNetRunRateDesc(tournamentId: Int): List<PointsTableEntity>
}
