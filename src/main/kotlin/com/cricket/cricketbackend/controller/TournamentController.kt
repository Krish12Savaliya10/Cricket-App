package com.cricket.cricketbackend.controller

import com.cricket.cricketbackend.dto.request.CreateTournamentRequest
import com.cricket.cricketbackend.dto.response.PointsTableEntryResponse
import com.cricket.cricketbackend.dto.response.TournamentResponse
import com.cricket.cricketbackend.service.TournamentService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/tournaments")
class TournamentController(
    private val tournamentService: TournamentService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createTournament(@Valid @RequestBody request: CreateTournamentRequest): TournamentResponse =
        tournamentService.createTournament(request)

    @GetMapping
    fun listTournaments(@RequestParam(required = false) status: String?): List<TournamentResponse> =
        tournamentService.listTournaments(status)

    @GetMapping("/{tournamentId}")
    fun getTournament(@PathVariable tournamentId: Int): TournamentResponse = tournamentService.getTournament(tournamentId)

    @GetMapping("/{tournamentId}/points-table")
    fun getPointsTable(@PathVariable tournamentId: Int): List<PointsTableEntryResponse> =
        tournamentService.getPointsTable(tournamentId)
}
