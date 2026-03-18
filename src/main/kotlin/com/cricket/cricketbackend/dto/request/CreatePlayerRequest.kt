package com.cricket.cricketbackend.dto.request

import com.cricket.cricketbackend.model.entity.PlayerRole
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreatePlayerRequest(
    @field:NotBlank
    val playerName: String?,
    @field:NotNull
    val role: PlayerRole?,
)
