package com.lms.domain.model.payroll

import java.util.UUID

/**
 * 급여 상세 ID Value Object
 */
@JvmInline
value class PayrollDetailId(val value: String) {
    companion object {
        fun generate(): PayrollDetailId = PayrollDetailId(UUID.randomUUID().toString())

        fun from(value: String): PayrollDetailId = PayrollDetailId(value)
    }
}
