package com.cricket.cricketbackend.repository

import com.cricket.cricketbackend.model.entity.PlayerMatchStatsEntity
import com.cricket.cricketbackend.model.entity.PlayerMatchStatsId
import org.springframework.data.jpa.repository.JpaRepository

interface PlayerMatchStatsRepository : JpaRepository<PlayerMatchStatsEntity, PlayerMatchStatsId> {
    fun findAllByMatchId(matchId: Int): List<PlayerMatchStatsEntity>
    fun findByMatchIdAndPlayerId(matchId: Int, playerId: Int): PlayerMatchStatsEntity?
}
