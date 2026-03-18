package com.cricket.cricketbackend.dto.response

data class BallResultResponse(
    val scoreboard: ScoreboardResponse,
    val matchStatus: String,
)
