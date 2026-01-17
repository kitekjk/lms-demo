package com.lms.domain.exception

/**
 * 급여를 찾을 수 없음
 */
class PayrollNotFoundException(payrollId: String, cause: Throwable? = null) :
    DomainException("PAYROLL001", "급여를 찾을 수 없습니다: $payrollId", cause)

/**
 * 급여가 이미 계산됨
 */
class PayrollAlreadyCalculatedException(employeeId: String, period: String, cause: Throwable? = null) :
    DomainException(
        "PAYROLL002",
        "해당 기간의 급여가 이미 계산되었습니다. 근로자: $employeeId, 기간: $period",
        cause
    )

/**
 * 출퇴근 기록이 없음
 */
class NoAttendanceRecordsFoundException(employeeId: String, period: String, cause: Throwable? = null) :
    DomainException(
        "PAYROLL003",
        "출퇴근 기록이 없습니다. 근로자: $employeeId, 기간: $period",
        cause
    )
