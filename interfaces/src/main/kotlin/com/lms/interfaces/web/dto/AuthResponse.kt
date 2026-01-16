package com.lms.interfaces.web.dto

import com.lms.application.auth.dto.LoginResult
import com.lms.application.auth.dto.RefreshTokenResult
import com.lms.application.auth.dto.UserInfo

/**
 * 로그인 응답 DTO
 */
data class LoginResponse(val accessToken: String, val refreshToken: String, val userInfo: UserInfoResponse) {
    companion object {
        fun from(result: LoginResult): LoginResponse = LoginResponse(
            accessToken = result.accessToken,
            refreshToken = result.refreshToken,
            userInfo = UserInfoResponse.from(result.userInfo)
        )
    }
}

/**
 * 사용자 정보 응답 DTO
 */
data class UserInfoResponse(val userId: String, val email: String, val role: String, val isActive: Boolean) {
    companion object {
        fun from(userInfo: UserInfo): UserInfoResponse = UserInfoResponse(
            userId = userInfo.userId,
            email = userInfo.email,
            role = userInfo.role,
            isActive = userInfo.isActive
        )
    }
}

/**
 * 회원가입 응답 DTO
 */
data class RegisterResponse(val userId: String, val email: String, val role: String, val isActive: Boolean) {
    companion object {
        fun from(userInfo: UserInfo): RegisterResponse = RegisterResponse(
            userId = userInfo.userId,
            email = userInfo.email,
            role = userInfo.role,
            isActive = userInfo.isActive
        )
    }
}

/**
 * 토큰 갱신 응답 DTO
 */
data class RefreshTokenResponse(val accessToken: String) {
    companion object {
        fun from(result: RefreshTokenResult): RefreshTokenResponse = RefreshTokenResponse(
            accessToken = result.accessToken
        )
    }
}

/**
 * 에러 응답 DTO
 */
data class ErrorResponse(
    val code: String,
    val message: String,
    val timestamp: String = java.time.Instant.now().toString()
)
