package com.lms.application.auth.dto

/**
 * 인증 관련 DTO
 */

/**
 * 로그인 요청
 */
data class LoginCommand(val email: String, val password: String)

/**
 * 로그인 응답
 */
data class LoginResult(val accessToken: String, val refreshToken: String, val userInfo: UserInfo)

/**
 * 회원가입 요청
 */
data class RegisterCommand(val email: String, val password: String, val role: String, val storeId: String?)

/**
 * 토큰 갱신 요청
 */
data class RefreshTokenCommand(val refreshToken: String)

/**
 * 토큰 갱신 응답
 */
data class RefreshTokenResult(val accessToken: String)

/**
 * 사용자 정보
 */
data class UserInfo(val userId: String, val email: String, val role: String, val isActive: Boolean)
