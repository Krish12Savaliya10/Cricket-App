package com.cricket.cricketbackend.dto.request

import com.cricket.cricketbackend.model.entity.ExtraType
import com.cricket.cricketbackend.model.entity.WicketType
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class BallInputDTO(
    @field:NotNull
    val matchId: Int?,
    @field:Min(0)
    @field:Max(6)
    val runs: Int = 0,
    @field:NotNull
    val extraType: ExtraType = ExtraType.NONE,
    val isWicket: Boolean = false,
    val wicketType: WicketType? = null,
    val dismissedPlayerId: Int? = null,
    val nextBatsmanId: Int? = null,
    val nextBowlerId: Int? = null,
)
