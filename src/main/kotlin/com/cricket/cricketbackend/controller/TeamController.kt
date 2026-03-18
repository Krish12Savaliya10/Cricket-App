package com.cricket.cricketbackend.controller

import com.cricket.cricketbackend.dto.request.CreatePlayerRequest
import com.cricket.cricketbackend.dto.request.CreateTeamRequest
import com.cricket.cricketbackend.dto.response.PlayerResponse
import com.cricket.cricketbackend.dto.response.TeamResponse
import com.cricket.cricketbackend.service.PlayerService
import com.cricket.cricketbackend.service.TeamService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/teams")
class TeamController(
    private val teamService: TeamService,
    private val playerService: PlayerService,
) {
    @GetMapping
    fun listTeams(): List<TeamResponse> = teamService.listTeams()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createTeam(@Valid @RequestBody request: CreateTeamRequest): TeamResponse = teamService.createTeam(request)

    @GetMapping("/{teamId}/players")
    fun listPlayers(@PathVariable teamId: Int): List<PlayerResponse> = playerService.listPlayers(teamId)

    @PostMapping("/{teamId}/players")
    @ResponseStatus(HttpStatus.CREATED)
    fun addPlayer(
        @PathVariable teamId: Int,
        @Valid @RequestBody request: CreatePlayerRequest,
    ): PlayerResponse = playerService.addPlayer(teamId, request)
}
