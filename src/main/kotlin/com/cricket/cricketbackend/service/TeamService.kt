package com.cricket.cricketbackend.service

import com.cricket.cricketbackend.dto.request.CreateTeamRequest
import com.cricket.cricketbackend.dto.response.TeamResponse
import com.cricket.cricketbackend.exception.ResourceNotFoundException
import com.cricket.cricketbackend.model.entity.TeamEntity
import com.cricket.cricketbackend.repository.TeamRepository
import com.cricket.cricketbackend.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class TeamService(
    private val teamRepository: TeamRepository,
    private val userRepository: UserRepository,
) {
    fun listTeams(): List<TeamResponse> = teamRepository.findAll().sortedBy { it.id }.map(::toResponse)

    fun createTeam(request: CreateTeamRequest): TeamResponse {
        val host = userRepository.findById(request.hostUserId!!).orElseThrow {
            ResourceNotFoundException("Host user ${request.hostUserId} not found")
        }
        val team = TeamEntity(
            id = IdGenerator.nextLegacyStyleId(),
            teamName = request.teamName!!.trim().uppercase(),
            host = host,
        )
        return teamRepository.save(team).asResponse()
    }

    fun getTeamEntity(teamId: Int): TeamEntity = teamRepository.findById(teamId).orElseThrow {
        ResourceNotFoundException("Team $teamId not found")
    }

    fun toResponse(entity: TeamEntity): TeamResponse = entity.asResponse()

    private fun TeamEntity.asResponse(): TeamResponse = TeamResponse(
        id = id,
        teamName = teamName,
        hostUserId = host!!.id,
    )
}
