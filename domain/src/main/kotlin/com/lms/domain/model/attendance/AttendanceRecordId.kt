package com.lms.domain.model.attendance

import java.util.UUID

/**
 * 출퇴근 기록 식별자 Value Object
 */
@JvmInline
value class AttendanceRecordId(val value: String) {
    init {
        require(value.isNotBlank()) { "AttendanceRecordId는 비어있을 수 없습니다." }
    }

    companion object {
        fun generate(): AttendanceRecordId = AttendanceRecordId(UUID.randomUUID().toString())
        fun from(value: String): AttendanceRecordId = AttendanceRecordId(value)
    }
}
