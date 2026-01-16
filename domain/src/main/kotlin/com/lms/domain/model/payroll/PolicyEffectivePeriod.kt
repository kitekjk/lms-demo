package com.lms.domain.model.payroll

import java.time.LocalDate

/**
 * 정책 유효 기간 Value Object
 */
data class PolicyEffectivePeriod(
    val effectiveFrom: LocalDate,
    val effectiveTo: LocalDate?
) {
    init {
        if (effectiveTo != null) {
            require(!effectiveFrom.isAfter(effectiveTo)) {
                "시작일은 종료일보다 늦을 수 없습니다. 시작: $effectiveFrom, 종료: $effectiveTo"
            }
        }
    }

    /**
     * 특정 날짜가 유효 기간 내에 있는지 확인
     */
    fun isEffectiveOn(date: LocalDate): Boolean {
        val afterStart = !date.isBefore(effectiveFrom)
        val beforeEnd = effectiveTo?.let { !date.isAfter(it) } ?: true
        return afterStart && beforeEnd
    }

    /**
     * 현재 유효한지 확인
     */
    fun isCurrentlyEffective(): Boolean {
        return isEffectiveOn(LocalDate.now())
    }

    /**
     * 종료일 설정
     */
    fun terminate(endDate: LocalDate): PolicyEffectivePeriod {
        require(!endDate.isBefore(effectiveFrom)) {
            "종료일은 시작일보다 이전일 수 없습니다."
        }
        return this.copy(effectiveTo = endDate)
    }

    companion object {
        /**
         * 무기한 유효한 정책
         */
        fun indefinite(startDate: LocalDate): PolicyEffectivePeriod {
            return PolicyEffectivePeriod(startDate, null)
        }
    }
}
