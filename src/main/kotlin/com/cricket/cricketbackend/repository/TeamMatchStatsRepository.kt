package com.cricket.cricketbackend.repository

import com.cricket.cricketbackend.model.entity.TeamMatchStatsEntity
import com.cricket.cricketbackend.model.entity.TeamMatchStatsId
import org.springframework.data.jpa.repository.JpaRepository

interface TeamMatchStatsRepository : JpaRepository<TeamMatchStatsEntity, TeamMatchStatsId> {
    fun findAllByMatchId(matchId: Int): List<TeamMatchStatsEntity>
    fun findByMatchIdAndTeamId(matchId: Int, teamId: Int): TeamMatchStatsEntity?
}
