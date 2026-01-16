package com.lms.interfaces.web.exception

import com.lms.interfaces.web.dto.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * 전역 예외 처리 핸들러
 * 모든 컨트롤러에서 발생하는 예외를 일관되게 처리
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Validation 실패 예외 처리
     * @Valid 검증 실패 시 발생
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException
    ): ResponseEntity<ApiResponse<Map<String, String>>> {
        log.warn("Validation error: {}", ex.message)

        val errors = ex.bindingResult.allErrors.associate { error ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.defaultMessage ?: "Invalid value"
            fieldName to errorMessage
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("입력값 검증에 실패했습니다.", errors))
    }

    /**
     * 인증 실패 예외 처리
     * Spring Security 인증 실패 시 발생
     */
    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(ex: AuthenticationException): ResponseEntity<ApiResponse<Unit>> {
        log.warn("Authentication failed: {}", ex.message)

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error("인증에 실패했습니다."))
    }

    /**
     * 인증 실패 예외 처리 (커스텀)
     */
    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorizedException(ex: UnauthorizedException): ResponseEntity<ApiResponse<Unit>> {
        log.warn("Unauthorized: {}", ex.message)

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error(ex.message))
    }

    /**
     * 권한 없음 예외 처리
     * Spring Security 권한 체크 실패 시 발생
     */
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(ex: AccessDeniedException): ResponseEntity<ApiResponse<Unit>> {
        log.warn("Access denied: {}", ex.message)

        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error("접근 권한이 없습니다."))
    }

    /**
     * 권한 없음 예외 처리 (커스텀)
     */
    @ExceptionHandler(ForbiddenException::class)
    fun handleForbiddenException(ex: ForbiddenException): ResponseEntity<ApiResponse<Unit>> {
        log.warn("Forbidden: {}", ex.message)

        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error(ex.message))
    }

    /**
     * 엔티티 조회 실패 예외 처리
     */
    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFoundException(ex: EntityNotFoundException): ResponseEntity<ApiResponse<Unit>> {
        log.warn("Entity not found: {}", ex.message)

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.message))
    }

    /**
     * 비즈니스 로직 예외 처리
     */
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): ResponseEntity<ApiResponse<Unit>> {
        log.warn("Business exception: {}", ex.message)

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.message))
    }

    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ApiResponse<Unit>> {
        log.warn("Illegal argument: {}", ex.message)

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.message ?: "잘못된 요청입니다."))
    }

    /**
     * IllegalStateException 처리
     */
    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(ex: IllegalStateException): ResponseEntity<ApiResponse<Unit>> {
        log.warn("Illegal state: {}", ex.message)

        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiResponse.error(ex.message ?: "처리할 수 없는 상태입니다."))
    }

    /**
     * 기타 모든 예외 처리
     */
    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ApiResponse<Unit>> {
        log.error("Unexpected error occurred", ex)

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("서버 내부 오류가 발생했습니다."))
    }
}
