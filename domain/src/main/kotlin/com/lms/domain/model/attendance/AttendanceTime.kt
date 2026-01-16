package com.lms.domain.model.attendance

import java.time.Duration
import java.time.Instant

/**
 * 출퇴근 시간 Value Object
 */
data class AttendanceTime(
    val checkInTime: Instant,
    val checkOutTime: Instant?
) {
    init {
        if (checkOutTime != null) {
            require(!checkInTime.isAfter(checkOutTime)) {
                "출근 시간은 퇴근 시간보다 늦을 수 없습니다. 출근: $checkInTime, 퇴근: $checkOutTime"
            }
        }
    }

    /**
     * 근무 완료 여부 (퇴근 체크 여부)
     */
    fun isCompleted(): Boolean = checkOutTime != null

    /**
     * 실제 근무 시간 계산 (시간 단위)
     */
    fun calculateActualWorkHours(): Double? {
        return checkOutTime?.let {
            val duration = Duration.between(checkInTime, it)
            duration.toMinutes() / 60.0
        }
    }

    /**
     * 퇴근 처리
     */
    fun checkOut(checkOutTime: Instant): AttendanceTime {
        require(this.checkOutTime == null) { "이미 퇴근 처리되었습니다." }
        require(!checkInTime.isAfter(checkOutTime)) {
            "퇴근 시간은 출근 시간보다 이전일 수 없습니다."
        }
        return this.copy(checkOutTime = checkOutTime)
    }
}
