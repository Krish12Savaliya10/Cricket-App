package com.cricket.cricketbackend.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class CreateTournamentRequest(
    @field:NotNull
    val hostUserId: Int?,
    @field:NotBlank
    val tournamentName: String?,
    @field:NotEmpty
    val teamIds: List<Int>?,
    @field:Min(2)
    @field:Max(50)
    val overs: Int?,
    @field:NotNull
    val grouped: Boolean?,
    @field:Min(1)
    @field:Max(5)
    val scheduleType: Int? = 1,
)
