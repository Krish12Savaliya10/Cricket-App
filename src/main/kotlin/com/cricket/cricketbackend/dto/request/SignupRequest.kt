package com.cricket.cricketbackend.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class SignupRequest(
    @field:NotBlank
    val fullName: String?,
    @field:Email
    @field:NotBlank
    val email: String?,
    @field:NotBlank
    val password: String?,
    @field:NotBlank
    val role: String?,
)
