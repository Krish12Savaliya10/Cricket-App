package com.cricket.cricketbackend.model.entity

import java.io.Serializable

data class PointsTableId(
    var tournament: Int = 0,
    var team: Int = 0,
) : Serializable
