package com.lms.interfaces.web.dto

import java.time.LocalDateTime

/**
 * 공통 API 응답 DTO
 * 모든 REST API 응답의 일관된 형식을 보장합니다.
 *
 * @param T 응답 데이터 타입
 */
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        /**
         * 성공 응답 (데이터 포함)
         */
        fun <T> success(data: T, message: String = "Success"): ApiResponse<T> {
            return ApiResponse(
                success = true,
                message = message,
                data = data
            )
        }

        /**
         * 성공 응답 (데이터 없음)
         */
        fun success(message: String = "Success"): ApiResponse<Unit> {
            return ApiResponse(
                success = true,
                message = message,
                data = null
            )
        }

        /**
         * 실패 응답 (에러 메시지만)
         */
        fun <T> error(message: String): ApiResponse<T> {
            return ApiResponse(
                success = false,
                message = message,
                data = null
            )
        }

        /**
         * 실패 응답 (데이터 포함)
         */
        fun <T> error(message: String, data: T): ApiResponse<T> {
            return ApiResponse(
                success = false,
                message = message,
                data = data
            )
        }
    }
}
