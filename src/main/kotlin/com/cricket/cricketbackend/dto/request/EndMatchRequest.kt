package com.cricket.cricketbackend.dto.request

import jakarta.validation.constraints.NotNull

data class EndMatchRequest(
    @field:NotNull
    val matchId: Int?,
)
