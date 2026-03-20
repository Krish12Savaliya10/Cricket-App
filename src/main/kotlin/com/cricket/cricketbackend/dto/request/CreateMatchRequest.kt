package com.cricket.cricketbackend.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class CreateMatchRequest(
    @field:NotNull
    val hostUserId: Int?,
    @field:NotNull
    val team1Id: Int?,
    @field:NotNull
    val team2Id: Int?,
    @field:Min(1)
    @field:Max(50)
    val overs: Int?,
)
