package com.cricket.cricketbackend.controller

import com.cricket.cricketbackend.dto.request.CreateSingleMatchRequest
import com.cricket.cricketbackend.dto.request.StartInningsRequest
import com.cricket.cricketbackend.dto.request.SubmitBallRequest
import com.cricket.cricketbackend.dto.response.BallResultResponse
import com.cricket.cricketbackend.dto.response.MatchResponse
import com.cricket.cricketbackend.dto.response.ScoreboardResponse
import com.cricket.cricketbackend.model.entity.MatchStatus
import com.cricket.cricketbackend.model.entity.MatchType
import com.cricket.cricketbackend.service.MatchService
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
@RequestMapping("/api/v1/matches")
class MatchController(
    private val matchService: MatchService,
) {

    @PostMapping("/single")
    @ResponseStatus(HttpStatus.CREATED)
    fun createSingleMatch(@Valid @RequestBody request: CreateSingleMatchRequest): MatchResponse =
        matchService.createSingleMatch(request)

    @GetMapping
    fun listMatches(
        @RequestParam(required = false) hostUserId: Int?,
        @RequestParam(required = false) status: MatchStatus?,
        @RequestParam(required = false) type: MatchType?,
    ): List<MatchResponse> = matchService.listMatches(hostUserId, status, type)

    @GetMapping("/{matchId}")
    fun getMatch(@PathVariable matchId: Int): MatchResponse = matchService.getMatch(matchId)

    @PostMapping("/{matchId}/innings/{inningsNo}/start")
    fun startInnings(
        @PathVariable matchId: Int,
        @PathVariable inningsNo: Int,
        @Valid @RequestBody request: StartInningsRequest,
    ): ScoreboardResponse = matchService.startInnings(matchId, inningsNo, request)

    @PostMapping("/{matchId}/ball")
    fun submitBall(
        @PathVariable matchId: Int,
        @Valid @RequestBody request: SubmitBallRequest,
    ): BallResultResponse = matchService.submitBall(matchId, request)

    @GetMapping("/{matchId}/scoreboard")
    fun getScoreboard(@PathVariable matchId: Int): ScoreboardResponse = matchService.getScoreboard(matchId)

    @PostMapping("/{matchId}/undo-last-ball")
    fun undoLastBall(@PathVariable matchId: Int): ScoreboardResponse = matchService.undoLastBall(matchId)
}
