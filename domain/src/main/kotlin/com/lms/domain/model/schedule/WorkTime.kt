package com.lms.domain.model.schedule

import java.time.Duration
import java.time.LocalTime

/**
 * 근무 시간 Value Object
 */
data class WorkTime(val startTime: LocalTime, val endTime: LocalTime) {
    init {
        require(!startTime.isAfter(endTime)) {
            "시작 시간은 종료 시간보다 늦을 수 없습니다. 시작: $startTime, 종료: $endTime"
        }
    }

    /**
     * 근무 시간 계산 (시간 단위)
     */
    fun calculateWorkHours(): Double {
        val duration = Duration.between(startTime, endTime)
        return duration.toMinutes() / 60.0
    }

    /**
     * 특정 시간이 근무 시간 내에 포함되는지 확인
     */
    fun contains(time: LocalTime): Boolean = !time.isBefore(startTime) && !time.isAfter(endTime)

    companion object {
        /**
         * 표준 근무 시간 (09:00 ~ 18:00)
         */
        fun standard(): WorkTime = WorkTime(
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(18, 0)
        )
    }
}
