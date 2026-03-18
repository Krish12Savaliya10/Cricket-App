package com.cricket.cricketbackend.dto.response

data class PointsTableEntryResponse(
    val teamId: Int,
    val teamName: String,
    val points: Int,
    val matchesWon: Int,
    val matchesLost: Int,
    val matchesDrawn: Int,
    val netRunRate: Double,
)
