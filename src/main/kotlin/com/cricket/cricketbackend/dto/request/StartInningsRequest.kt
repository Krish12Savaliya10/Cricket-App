package com.cricket.cricketbackend.dto.request

import jakarta.validation.constraints.NotNull

data class StartInningsRequest(
    @field:NotNull
    val battingTeamId: Int?,
    @field:NotNull
    val opener1Id: Int?,
    @field:NotNull
    val opener2Id: Int?,
    @field:NotNull
    val bowlerId: Int?,
)
