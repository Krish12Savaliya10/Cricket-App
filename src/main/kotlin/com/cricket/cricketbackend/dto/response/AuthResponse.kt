package com.cricket.cricketbackend.dto.response

data class AuthResponse(
    val userId: Int,
    val fullName: String,
    val email: String,
    val role: String,
)
