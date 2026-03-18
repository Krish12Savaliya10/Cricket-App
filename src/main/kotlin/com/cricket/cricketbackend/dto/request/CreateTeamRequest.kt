package com.cricket.cricketbackend.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateTeamRequest(
    @field:NotNull
    val hostUserId: Int?,
    @field:NotBlank
    val teamName: String?,
)
