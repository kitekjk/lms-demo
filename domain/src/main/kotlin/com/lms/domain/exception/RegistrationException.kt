package com.lms.domain.exception

/**
 * 이메일 중복 예외
 * 이미 등록된 이메일로 회원가입 시도할 때 발생
 */
class DuplicateEmailException(email: String, cause: Throwable? = null) :
    DomainException("REG001", "이미 등록된 이메일입니다: $email", cause)

/**
 * 유효하지 않은 역할 예외
 * 잘못된 역할로 회원가입 시도할 때 발생
 */
class InvalidRoleException(role: String, cause: Throwable? = null) :
    DomainException("REG002", "유효하지 않은 역할입니다: $role", cause)
