package com.lms.domain.model.payroll

import java.math.BigDecimal

/**
 * 급여 정책 배율 Value Object
 */
@JvmInline
value class PolicyMultiplier(val value: BigDecimal) {
    init {
        require(value >= BigDecimal.ZERO) {
            "정책 배율은 0 이상이어야 합니다. 입력값: $value"
        }
        require(value <= BigDecimal("10.0")) {
            "정책 배율은 10.0 이하여야 합니다. 입력값: $value"
        }
    }

    companion object {
        /**
         * 일반 초과근무 배율 (1.5배)
         */
        fun standard(): PolicyMultiplier = PolicyMultiplier(BigDecimal("1.5"))

        /**
         * 주말/야간 배율 (2.0배)
         */
        fun weekend(): PolicyMultiplier = PolicyMultiplier(BigDecimal("2.0"))

        /**
         * 공휴일 배율 (2.5배)
         */
        fun holiday(): PolicyMultiplier = PolicyMultiplier(BigDecimal("2.5"))

        fun from(value: BigDecimal): PolicyMultiplier = PolicyMultiplier(value)
        fun from(value: Double): PolicyMultiplier = PolicyMultiplier(BigDecimal.valueOf(value))
    }
}
