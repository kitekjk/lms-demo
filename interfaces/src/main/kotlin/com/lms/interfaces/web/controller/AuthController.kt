package com.lms.interfaces.web.controller

import com.lms.application.auth.LoginAppService
import com.lms.application.auth.RefreshTokenAppService
import com.lms.application.auth.RegisterAppService
import com.lms.domain.exception.DomainException
import com.lms.infrastructure.context.HttpDomainContext
import com.lms.interfaces.web.dto.ErrorResponse
import com.lms.interfaces.web.dto.LoginRequest
import com.lms.interfaces.web.dto.LoginResponse
import com.lms.interfaces.web.dto.RefreshTokenRequest
import com.lms.interfaces.web.dto.RefreshTokenResponse
import com.lms.interfaces.web.dto.RegisterRequest
import com.lms.interfaces.web.dto.RegisterResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 인증 관련 API Controller
 */
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val loginAppService: LoginAppService,
    private val registerAppService: RegisterAppService,
    private val refreshTokenAppService: RefreshTokenAppService
) {

    /**
     * 로그인
     * POST /api/auth/login
     */
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<LoginResponse> {
        val context = HttpDomainContext.from(httpRequest)
        val result = loginAppService.execute(context, request.toCommand())
        return ResponseEntity.ok(LoginResponse.from(result))
    }

    /**
     * 회원가입
     * POST /api/auth/register
     * SUPER_ADMIN만 접근 가능
     */
    @PostMapping("/register")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    fun register(
        @Valid @RequestBody request: RegisterRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<RegisterResponse> {
        val context = HttpDomainContext.from(httpRequest)
        val result = registerAppService.execute(context, request.toCommand())
        return ResponseEntity.status(HttpStatus.CREATED).body(RegisterResponse.from(result))
    }

    /**
     * 토큰 갱신
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    fun refresh(
        @Valid @RequestBody request: RefreshTokenRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<RefreshTokenResponse> {
        val context = HttpDomainContext.from(httpRequest)
        val result = refreshTokenAppService.execute(context, request.toCommand())
        return ResponseEntity.ok(RefreshTokenResponse.from(result))
    }

    /**
     * 로그아웃
     * POST /api/auth/logout
     * TODO: Refresh Token 무효화 처리 (블랙리스트 또는 DB 저장 필요)
     */
    @PostMapping("/logout")
    fun logout(httpRequest: HttpServletRequest): ResponseEntity<Map<String, String>> {
        // 현재는 클라이언트에서 토큰 삭제만 수행
        // 추후 Refresh Token 블랙리스트 구현 필요
        return ResponseEntity.ok(mapOf("message" to "로그아웃 되었습니다"))
    }

    /**
     * DomainException 처리
     */
    @ExceptionHandler(DomainException::class)
    fun handleDomainException(e: DomainException): ResponseEntity<ErrorResponse> {
        val status = when (e.code) {
            "AUTH001", "TOKEN001" -> HttpStatus.UNAUTHORIZED
            "AUTH002", "TOKEN003" -> HttpStatus.FORBIDDEN
            "REG001" -> HttpStatus.CONFLICT
            else -> HttpStatus.BAD_REQUEST
        }

        return ResponseEntity.status(status).body(
            ErrorResponse(
                code = e.code,
                message = e.message ?: "알 수 없는 오류가 발생했습니다"
            )
        )
    }

    /**
     * Validation 에러 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = e.bindingResult.allErrors.joinToString(", ") { error ->
            when (error) {
                is FieldError -> "${error.field}: ${error.defaultMessage}"
                else -> error.defaultMessage ?: "유효하지 않은 입력입니다"
            }
        }

        return ResponseEntity.badRequest().body(
            ErrorResponse(
                code = "VALIDATION_ERROR",
                message = errors
            )
        )
    }

    /**
     * 일반 예외 처리
     */
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> = ResponseEntity.internalServerError().body(
        ErrorResponse(
            code = "INTERNAL_ERROR",
            message = "서버 오류가 발생했습니다"
        )
    )
}
