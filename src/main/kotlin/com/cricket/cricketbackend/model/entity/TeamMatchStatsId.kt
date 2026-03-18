package com.cricket.cricketbackend.model.entity

import java.io.Serializable

data class TeamMatchStatsId(
    var match: Int = 0,
    var team: Int = 0,
) : Serializable
