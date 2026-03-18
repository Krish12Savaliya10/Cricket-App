package com.cricket.cricketbackend.service

import com.cricket.cricketbackend.dto.request.CreatePlayerRequest
import com.cricket.cricketbackend.dto.response.PlayerResponse
import com.cricket.cricketbackend.exception.ResourceNotFoundException
import com.cricket.cricketbackend.model.entity.PlayerEntity
import com.cricket.cricketbackend.repository.PlayerRepository
import com.cricket.cricketbackend.repository.TeamRepository
import org.springframework.stereotype.Service

@Service
class PlayerService(
    private val playerRepository: PlayerRepository,
    private val teamRepository: TeamRepository,
) {
    fun listPlayers(teamId: Int): List<PlayerResponse> = getPlayersForTeam(teamId).sortedBy { it.id }.map(::toResponse)

    fun addPlayer(teamId: Int, request: CreatePlayerRequest): PlayerResponse {
        val team = teamRepository.findById(teamId).orElseThrow {
            ResourceNotFoundException("Team $teamId not found")
        }
        val player = PlayerEntity(
            id = IdGenerator.nextLegacyStyleId(),
            playerName = request.playerName!!.trim().uppercase(),
            role = request.role!!,
            team = team,
        )
        return playerRepository.save(player).asResponse()
    }

    fun getPlayersForTeam(teamId: Int): List<PlayerEntity> = playerRepository.findAllByTeamId(teamId)

    fun getPlayerEntity(playerId: Int): PlayerEntity = playerRepository.findById(playerId).orElseThrow {
        ResourceNotFoundException("Player $playerId not found")
    }

    fun toResponse(entity: PlayerEntity): PlayerResponse = entity.asResponse()

    private fun PlayerEntity.asResponse(): PlayerResponse = PlayerResponse(
        id = id,
        playerName = playerName,
        role = role.name,
        teamId = team!!.id,
    )
}
