package com.cricket.cricketbackend.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class StartMatchRequest(
    @field:NotNull
    val matchId: Int?,
    @field:Min(1)
    val inningsNo: Int? = 1,
    @field:NotNull
    val battingTeamId: Int?,
    @field:NotNull
    val strikerId: Int?,
    @field:NotNull
    val nonStrikerId: Int?,
    @field:NotNull
    val bowlerId: Int?,
)
