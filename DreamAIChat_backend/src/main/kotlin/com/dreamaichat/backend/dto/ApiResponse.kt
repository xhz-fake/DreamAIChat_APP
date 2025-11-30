package com.dreamaichat.backend.dto

data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val success: Boolean,
    val data: T?
) {
    companion object {
        fun <T> ok(data: T, message: String = "ok"): ApiResponse<T> =
            ApiResponse(code = 0, message = message, success = true, data = data)

        fun <T> fail(message: String, code: Int = -1): ApiResponse<T> =
            ApiResponse(code = code, message = message, success = false, data = null)
    }
}
