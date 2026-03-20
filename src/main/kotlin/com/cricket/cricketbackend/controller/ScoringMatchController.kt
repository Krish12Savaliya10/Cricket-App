package com.cricket.cricketbackend.controller

import com.cricket.cricketbackend.dto.request.CreateMatchRequest
import com.cricket.cricketbackend.dto.request.EndMatchRequest
import com.cricket.cricketbackend.dto.request.StartMatchRequest
import com.cricket.cricketbackend.dto.response.MatchState
import com.cricket.cricketbackend.dto.response.ScoringMatchResponse
import com.cricket.cricketbackend.service.ScoringMatchService
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
@RequestMapping("/matches")
class ScoringMatchController(
    private val scoringMatchService: ScoringMatchService,
) {
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateMatchRequest): ScoringMatchResponse = scoringMatchService.create(request)

    @PostMapping("/start")
    fun start(@Valid @RequestBody request: StartMatchRequest): MatchState = scoringMatchService.start(request)

    @PostMapping("/end")
    fun end(@Valid @RequestBody request: EndMatchRequest): ScoringMatchResponse = scoringMatchService.end(request)

    @GetMapping("/{id}")
    fun get(@PathVariable id: Int): ScoringMatchResponse = scoringMatchService.get(id)
}
