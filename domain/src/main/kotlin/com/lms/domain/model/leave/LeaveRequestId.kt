package com.lms.domain.model.leave

import java.util.UUID

/**
 * 휴가 신청 식별자 Value Object
 */
@JvmInline
value class LeaveRequestId(val value: String) {
    init {
        require(value.isNotBlank()) { "LeaveRequestId는 비어있을 수 없습니다." }
    }

    companion object {
        fun generate(): LeaveRequestId = LeaveRequestId(UUID.randomUUID().toString())
        fun from(value: String): LeaveRequestId = LeaveRequestId(value)
    }
}
