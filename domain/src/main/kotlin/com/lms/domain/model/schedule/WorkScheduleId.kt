package com.lms.domain.model.schedule

import java.util.UUID

/**
 * 근무 일정 식별자 Value Object
 */
@JvmInline
value class WorkScheduleId(val value: String) {
    init {
        require(value.isNotBlank()) { "WorkScheduleId는 비어있을 수 없습니다." }
    }

    companion object {
        fun generate(): WorkScheduleId = WorkScheduleId(UUID.randomUUID().toString())
        fun from(value: String): WorkScheduleId = WorkScheduleId(value)
    }
}
