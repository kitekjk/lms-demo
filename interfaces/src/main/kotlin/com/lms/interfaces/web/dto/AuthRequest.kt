package com.lms.interfaces.web.dto

import com.lms.application.auth.dto.LoginCommand
import com.lms.application.auth.dto.RefreshTokenCommand
import com.lms.application.auth.dto.RegisterCommand
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 로그인 요청 DTO
 */
data class LoginRequest(
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "유효한 이메일 형식이 아닙니다")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
    val password: String
) {
    fun toCommand(): LoginCommand = LoginCommand(
        email = email,
        password = password
    )
}

/**
 * 회원가입 요청 DTO
 */
data class RegisterRequest(
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "유효한 이메일 형식이 아닙니다")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
    val password: String,

    @field:NotBlank(message = "역할은 필수입니다")
    val role: String,

    val storeId: String? = null
) {
    fun toCommand(): RegisterCommand = RegisterCommand(
        email = email,
        password = password,
        role = role,
        storeId = storeId
    )
}

/**
 * 토큰 갱신 요청 DTO
 */
data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh Token은 필수입니다")
    val refreshToken: String
) {
    fun toCommand(): RefreshTokenCommand = RefreshTokenCommand(
        refreshToken = refreshToken
    )
}
