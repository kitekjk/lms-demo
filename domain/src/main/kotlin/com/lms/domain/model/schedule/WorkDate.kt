package com.lms.domain.model.schedule

import java.time.LocalDate

/**
 * 근무 날짜 Value Object
 */
@JvmInline
value class WorkDate(val value: LocalDate) {
    fun isWeekend(): Boolean {
        val dayOfWeek = value.dayOfWeek
        return dayOfWeek == java.time.DayOfWeek.SATURDAY || dayOfWeek == java.time.DayOfWeek.SUNDAY
    }

    fun isBefore(other: WorkDate): Boolean = value.isBefore(other.value)
    fun isAfter(other: WorkDate): Boolean = value.isAfter(other.value)
    fun isEqual(other: WorkDate): Boolean = value.isEqual(other.value)
}
