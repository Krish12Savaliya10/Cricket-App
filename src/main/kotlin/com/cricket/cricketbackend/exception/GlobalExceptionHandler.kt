package com.cricket.cricketbackend.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(ex: ResourceNotFoundException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to ex.message.orEmpty()))

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequest(ex: BadRequestException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to ex.message.orEmpty()))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, String>> {
        val message = ex.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: "Validation failed"
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to message))
    }
}
