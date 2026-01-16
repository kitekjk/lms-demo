package com.lms.domain.exception

/**
 * 근로자를 찾을 수 없을 때 발생하는 예외
 */
class EmployeeNotFoundException(employeeId: String, cause: Throwable? = null) :
    DomainException(ErrorCode.EMPLOYEE_NOT_FOUND, "근로자를 찾을 수 없습니다: $employeeId", cause)

/**
 * 중복된 사용자로 근로자 등록 시도 시 발생하는 예외
 */
class DuplicateEmployeeUserException(userId: String, cause: Throwable? = null) :
    DomainException(ErrorCode.DUPLICATE_EMPLOYEE_USER, "이미 근로자로 등록된 사용자입니다: $userId", cause)

/**
 * 권한이 없는 매장에 접근 시도 시 발생하는 예외
 */
class UnauthorizedStoreAccessException(message: String = "해당 매장에 대한 접근 권한이 없습니다.", cause: Throwable? = null) :
    DomainException(ErrorCode.UNAUTHORIZED_STORE_ACCESS, message, cause)
