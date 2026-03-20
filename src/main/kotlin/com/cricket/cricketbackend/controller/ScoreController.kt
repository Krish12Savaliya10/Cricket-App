package com.cricket.cricketbackend.controller

import com.cricket.cricketbackend.dto.request.BallInputDTO
import com.cricket.cricketbackend.dto.request.UndoScoreRequest
import com.cricket.cricketbackend.dto.response.MatchState
import com.cricket.cricketbackend.service.ScoreService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/score")
class ScoreController(
    private val scoreService: ScoreService,
) {
    @PostMapping("/add-ball")
    fun addBall(@Valid @RequestBody request: BallInputDTO): MatchState = scoreService.addBall(request)

    @PostMapping("/undo")
    fun undo(@Valid @RequestBody request: UndoScoreRequest): MatchState = scoreService.undoLastBall(request.matchId!!)

    @GetMapping("/{matchId}")
    fun getScore(@PathVariable matchId: Int): MatchState = scoreService.getMatchState(matchId)
}
