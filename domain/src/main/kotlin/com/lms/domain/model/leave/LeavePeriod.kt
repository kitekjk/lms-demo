package com.lms.domain.model.leave

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 휴가 기간 Value Object
 */
data class LeavePeriod(
    val startDate: LocalDate,
    val endDate: LocalDate
) {
    init {
        require(!startDate.isAfter(endDate)) {
            "시작일은 종료일보다 늦을 수 없습니다. 시작: $startDate, 종료: $endDate"
        }
    }

    /**
     * 휴가 일수 계산 (시작일과 종료일 포함)
     */
    fun calculateDays(): Long {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1
    }

    /**
     * 특정 날짜가 휴가 기간에 포함되는지 확인
     */
    fun contains(date: LocalDate): Boolean {
        return !date.isBefore(startDate) && !date.isAfter(endDate)
    }

    /**
     * 다른 휴가 기간과 겹치는지 확인
     */
    fun overlapsWith(other: LeavePeriod): Boolean {
        return !endDate.isBefore(other.startDate) && !startDate.isAfter(other.endDate)
    }
}
