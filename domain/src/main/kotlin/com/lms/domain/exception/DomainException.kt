package com.lms.domain.exception

/**
 * 도메인 예외
 * 비즈니스 로직에서 발생하는 모든 예외의 기본 클래스
 */
open class DomainException(val code: String, message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)
