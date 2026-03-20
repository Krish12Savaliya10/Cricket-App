package com.cricket.cricketbackend.dto.request

import jakarta.validation.constraints.NotNull

data class UndoScoreRequest(
    @field:NotNull
    val matchId: Int?,
)
