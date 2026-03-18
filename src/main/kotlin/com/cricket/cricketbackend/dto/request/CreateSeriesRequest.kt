package com.cricket.cricketbackend.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateSeriesRequest(
    @field:NotNull
    val hostUserId: Int?,
    @field:NotBlank
    val seriesName: String?,
    @field:NotNull
    val team1Id: Int?,
    @field:NotNull
    val team2Id: Int?,
    @field:Min(2)
    @field:Max(5)
    val totalMatches: Int?,
    @field:Min(2)
    @field:Max(50)
    val overs: Int?,
)
