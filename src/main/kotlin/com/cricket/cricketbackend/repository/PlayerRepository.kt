package com.cricket.cricketbackend.repository

import com.cricket.cricketbackend.model.entity.PlayerEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PlayerRepository : JpaRepository<PlayerEntity, Int> {
    fun findAllByTeamId(teamId: Int): List<PlayerEntity>
}
