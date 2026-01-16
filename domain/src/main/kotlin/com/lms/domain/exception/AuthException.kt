package com.lms.domain.exception

/**
 * 인증 실패 예외
 * 이메일 또는 비밀번호가 일치하지 않을 때 발생
 */
class AuthenticationFailedException(message: String = "이메일 또는 비밀번호가 일치하지 않습니다.", cause: Throwable? = null) :
    DomainException("AUTH001", message, cause)

/**
 * 비활성화된 사용자 예외
 * 비활성화된 사용자가 로그인 시도할 때 발생
 */
class InactiveUserException(message: String = "비활성화된 사용자입니다.", cause: Throwable? = null) :
    DomainException("AUTH002", message, cause)

/**
 * 유효하지 않은 토큰 예외
 * Refresh Token이 유효하지 않을 때 발생
 */
class InvalidTokenException(message: String = "유효하지 않은 Refresh Token입니다.", cause: Throwable? = null) :
    DomainException("TOKEN001", message, cause)

/**
 * 사용자를 찾을 수 없음 예외
 * 토큰의 사용자 ID로 사용자를 찾을 수 없을 때 발생
 */
class UserNotFoundException(message: String = "사용자를 찾을 수 없습니다.", cause: Throwable? = null) :
    DomainException("TOKEN002", message, cause)

/**
 * 토큰 사용자 비활성화 예외
 * Refresh Token의 사용자가 비활성화되어 있을 때 발생
 */
class TokenUserInactiveException(message: String = "비활성화된 사용자입니다.", cause: Throwable? = null) :
    DomainException("TOKEN003", message, cause)
