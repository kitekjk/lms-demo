package com.lms.domain.model.attendance

/**
 * 출퇴근 상태
 */
enum class AttendanceStatus(val description: String) {
    NORMAL("정상 출근"),
    LATE("지각"),
    EARLY_LEAVE("조퇴"),
    ABSENT("결근"),
    PENDING("퇴근 대기 중")
}
