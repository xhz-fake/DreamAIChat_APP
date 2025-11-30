package com.dreamaichat.backend.controller

import com.dreamaichat.backend.dto.ApiResponse
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Unit>> {
        val msg = ex.bindingResult.fieldErrors.joinToString(", ") { "${it.field}:${it.defaultMessage}" }
            .ifBlank { "Invalid request parameters" }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.fail(msg, HttpStatus.BAD_REQUEST.value()))
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraint(ex: ConstraintViolationException): ResponseEntity<ApiResponse<Unit>> {
        val msg = ex.constraintViolations.joinToString(", ") { it.message }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.fail(msg, HttpStatus.BAD_REQUEST.value()))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ApiResponse<Unit>> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.fail(ex.message ?: "Invalid request", HttpStatus.BAD_REQUEST.value()))

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ApiResponse<Unit>> =
        ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.fail(ex.message ?: "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR.value()))
}
