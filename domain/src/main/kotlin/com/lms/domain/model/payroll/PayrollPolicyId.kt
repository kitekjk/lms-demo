package com.lms.domain.model.payroll

import java.util.UUID

/**
 * 급여 정책 식별자 Value Object
 */
@JvmInline
value class PayrollPolicyId(val value: String) {
    init {
        require(value.isNotBlank()) { "PayrollPolicyId는 비어있을 수 없습니다." }
    }

    companion object {
        fun generate(): PayrollPolicyId = PayrollPolicyId(UUID.randomUUID().toString())
        fun from(value: String): PayrollPolicyId = PayrollPolicyId(value)
    }
}
