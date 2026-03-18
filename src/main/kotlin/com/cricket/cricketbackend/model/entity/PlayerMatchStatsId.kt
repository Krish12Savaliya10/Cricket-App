package com.cricket.cricketbackend.model.entity

import java.io.Serializable

data class PlayerMatchStatsId(
    var match: Int = 0,
    var player: Int = 0,
) : Serializable
