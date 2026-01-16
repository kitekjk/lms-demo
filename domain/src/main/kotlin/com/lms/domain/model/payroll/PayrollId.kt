package com.lms.domain.model.payroll

import java.util.UUID

/**
 * 급여 식별자 Value Object
 */
@JvmInline
value class PayrollId(val value: String) {
    init {
        require(value.isNotBlank()) { "PayrollId는 비어있을 수 없습니다." }
    }

    companion object {
        fun generate(): PayrollId = PayrollId(UUID.randomUUID().toString())
        fun from(value: String): PayrollId = PayrollId(value)
    }
}
