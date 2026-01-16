package com.lms.domain.exception

/**
 * 도메인 예외 추상 클래스
 * 비즈니스 로직에서 발생하는 모든 예외의 기본 클래스
 * 각 케이스별로 구체적인 예외 클래스를 만들어 사용
 */
abstract class DomainException(val code: String, message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)
