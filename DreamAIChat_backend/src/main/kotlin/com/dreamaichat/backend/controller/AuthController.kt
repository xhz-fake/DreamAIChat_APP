package com.dreamaichat.backend.controller

import com.dreamaichat.backend.dto.ApiResponse
import com.dreamaichat.backend.dto.LoginRequest
import com.dreamaichat.backend.dto.LoginResponse
import com.dreamaichat.backend.dto.RegisterRequest
import com.dreamaichat.backend.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<ApiResponse<LoginResponse>> {
        val result = authService.register(request)
        return ResponseEntity.ok(ApiResponse.ok(result, "娉ㄥ唽鎴愬姛"))
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<ApiResponse<LoginResponse>> {
        val result = authService.login(request)
        return ResponseEntity.ok(ApiResponse.ok(result, "鐧诲綍鎴愬姛"))
    }
}
