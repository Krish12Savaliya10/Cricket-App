package com.cricket.cricketbackend.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class SubmitBallRequest(
    @field:NotNull
    val eventType: BallEventType?,
    @field:Min(0)
    @field:Max(6)
    val runs: Int? = 0,
    @field:Min(0)
    @field:Max(6)
    val extraRuns: Int? = 0,
    val noBallMode: NoBallMode? = null,
    val dismissalType: DismissalType? = null,
    val outPlayerId: Int? = null,
    val nextBatsmanId: Int? = null,
    @field:Min(0)
    @field:Max(2)
    val runoutRuns: Int? = null,
    val runoutEnd: RunoutEnd? = null,
    val nextBowlerId: Int? = null,
)

enum class BallEventType {
    RUN,
    WIDE,
    NO_BALL,
    WICKET,
    EXTRA,
}

enum class NoBallMode {
    STANDARD,
    EXTRA,
    RUN_OUT,
}

enum class DismissalType {
    REGULAR,
    RUN_OUT,
}

enum class RunoutEnd {
    KEEPER_END,
    BOWLER_END,
}
