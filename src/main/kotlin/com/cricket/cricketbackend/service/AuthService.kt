package com.cricket.cricketbackend.service

import com.cricket.cricketbackend.dto.request.LoginRequest
import com.cricket.cricketbackend.dto.request.SignupRequest
import com.cricket.cricketbackend.dto.response.AuthResponse
import com.cricket.cricketbackend.exception.BadRequestException
import com.cricket.cricketbackend.model.entity.UserEntity
import com.cricket.cricketbackend.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
) {
    @Transactional
    fun signup(request: SignupRequest): AuthResponse {
        val normalizedEmail = request.email!!.trim().lowercase()
        val existing = userRepository.findByEmailIgnoreCase(normalizedEmail)
        if (existing != null) {
            throw BadRequestException("Email already exists")
        }
        val user = userRepository.save(
            UserEntity(
                id = IdGenerator.nextLegacyStyleId(),
                fullName = request.fullName!!.trim().uppercase(),
                email = normalizedEmail,
                password = request.password!!,
                role = request.role!!.trim().uppercase(),
            ),
        )
        return user.toResponse()
    }

    fun login(request: LoginRequest): AuthResponse {
        val normalizedEmail = request.email!!.trim().lowercase()
        val normalizedPassword = request.password!!
        val user = userRepository.findByEmailIgnoreCase(normalizedEmail)
            ?: throw BadRequestException("Invalid credentials")
        if (user.password != normalizedPassword) {
            throw BadRequestException("Invalid credentials")
        }
        return user.toResponse()
    }

    private fun UserEntity.toResponse(): AuthResponse = AuthResponse(
        userId = id,
        fullName = fullName,
        email = email,
        role = role,
    )
}
