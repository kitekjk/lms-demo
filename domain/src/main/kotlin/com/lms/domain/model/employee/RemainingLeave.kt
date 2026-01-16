package com.lms.domain.model.employee

import java.math.BigDecimal

@JvmInline
value class RemainingLeave(val value: BigDecimal) {
    init {
        require(value >= BigDecimal.ZERO) { "잔여 연차는 음수일 수 없습니다." }
    }

    fun deduct(days: BigDecimal): RemainingLeave {
        require(days > BigDecimal.ZERO) { "차감할 연차는 0보다 커야 합니다." }
        require(value >= days) { "잔여 연차가 부족합니다. 현재: $value, 요청: $days" }
        return RemainingLeave(value - days)
    }

    fun add(days: BigDecimal): RemainingLeave {
        require(days > BigDecimal.ZERO) { "복구할 연차는 0보다 커야 합니다." }
        return RemainingLeave(value + days)
    }
}
