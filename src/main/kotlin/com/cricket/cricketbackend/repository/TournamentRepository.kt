package com.cricket.cricketbackend.repository

import com.cricket.cricketbackend.model.entity.TournamentEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TournamentRepository : JpaRepository<TournamentEntity, Int> {
    fun findAllByHostId(hostId: Int): List<TournamentEntity>
    fun findAllByTournamentStatus(tournamentStatus: String): List<TournamentEntity>
}
