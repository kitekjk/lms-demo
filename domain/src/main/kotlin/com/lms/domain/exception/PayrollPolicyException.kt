package com.lms.domain.exception

/**
 * 급여 정책을 찾을 수 없음
 */
class PayrollPolicyNotFoundException(policyId: String, cause: Throwable? = null) :
    DomainException("PAYROLL_POLICY001", "급여 정책을 찾을 수 없습니다: $policyId", cause)

/**
 * 급여 정책 기간 중복
 */
class PayrollPolicyPeriodOverlapException(
    policyType: String,
    existingPeriod: String,
    newPeriod: String,
    cause: Throwable? = null
) : DomainException(
    "PAYROLL_POLICY002",
    "정책 기간이 기존 정책과 겹칩니다. 유형: $policyType, 기존: $existingPeriod, 신규: $newPeriod",
    cause
)

/**
 * 유효하지 않은 정책 기간
 */
class InvalidPolicyPeriodException(effectiveFrom: String, effectiveTo: String?, cause: Throwable? = null) :
    DomainException(
        "PAYROLL_POLICY003",
        "유효하지 않은 정책 기간입니다. 시작일: $effectiveFrom, 종료일: ${effectiveTo ?: "없음"}",
        cause
    )

/**
 * 정책 수정 불가 (종료된 정책)
 */
class InactivePolicyCannotBeModifiedException(policyId: String, cause: Throwable? = null) :
    DomainException(
        "PAYROLL_POLICY004",
        "종료되거나 유효하지 않은 정책은 수정할 수 없습니다: $policyId",
        cause
    )
