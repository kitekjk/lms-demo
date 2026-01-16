package com.lms.domain.model.leave

/**
 * 휴가 신청 상태
 */
enum class LeaveStatus(val description: String) {
    PENDING("승인 대기"),
    APPROVED("승인됨"),
    REJECTED("거부됨"),
    CANCELLED("취소됨")
}
