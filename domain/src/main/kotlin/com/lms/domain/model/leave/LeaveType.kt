package com.lms.domain.model.leave

/**
 * 휴가 유형
 */
enum class LeaveType(val description: String, val requiresApproval: Boolean) {
    ANNUAL("연차", true),
    SICK("병가", true),
    PERSONAL("개인 사유", true),
    MATERNITY("출산 휴가", true),
    PATERNITY("육아 휴가", true),
    BEREAVEMENT("경조사", true),
    UNPAID("무급 휴가", true)
}
