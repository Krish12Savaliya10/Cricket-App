package com.cricket.cricketbackend.service

import com.cricket.cricketbackend.dto.request.LoginRequest
import com.cricket.cricketbackend.dto.request.SignupRequest
import com.cricket.cricketbackend.dto.response.AuthResponse
import com.cricket.cricketbackend.exception.BadRequestException
import com.cricket.cricketbackend.exception.ResourceNotFoundException
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
        val existing = userRepository.findAll().firstOrNull { it.email.equals(request.email!!, ignoreCase = true) }
        if (existing != null) {
            throw BadRequestException("Email already exists")
        }
        val user = userRepository.save(
            UserEntity(
                id = IdGenerator.nextLegacyStyleId(),
                fullName = request.fullName!!.trim().uppercase(),
                email = request.email!!.trim().lowercase(),
                password = request.password!!,
                role = request.role!!.trim().uppercase(),
            ),
        )
        return user.toResponse()
    }

    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findAll().firstOrNull {
            it.email.equals(request.email!!, ignoreCase = true) && it.password == request.password
        } ?: throw ResourceNotFoundException("Invalid credentials")
        return user.toResponse()
    }

    private fun UserEntity.toResponse(): AuthResponse = AuthResponse(
        userId = id,
        fullName = fullName,
        email = email,
        role = role,
    )
}
