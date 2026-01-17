package com.lms.domain.exception

/**
 * 도메인 예외 에러 코드 중앙 관리
 * 모든 에러 코드는 여기에 정의하여 중복 방지 및 관리 용이성 확보
 */
object ErrorCode {
    // 인증 관련 (AUTH)
    const val AUTHENTICATION_FAILED = "AUTH001"
    const val INACTIVE_USER = "AUTH002"
    const val INVALID_TOKEN = "TOKEN001"
    const val USER_NOT_FOUND = "TOKEN002"
    const val TOKEN_USER_INACTIVE = "TOKEN003"

    // 등록 관련 (REG)
    const val DUPLICATE_EMAIL = "REG001"
    const val INVALID_ROLE = "REG002"

    // 매장 관련 (STORE)
    const val STORE_NOT_FOUND = "STORE001"
    const val DUPLICATE_STORE_NAME = "STORE002"

    // 근로자 관련 (EMPLOYEE)
    const val EMPLOYEE_NOT_FOUND = "EMP001"
    const val DUPLICATE_EMPLOYEE_USER = "EMP002"
    const val UNAUTHORIZED_STORE_ACCESS = "EMP003"

    // 출퇴근 관련 (ATTENDANCE)
    const val ATTENDANCE_NOT_FOUND = "ATT001"
    const val ALREADY_CHECKED_IN = "ATT002"
    const val NOT_CHECKED_IN = "ATT003"
    const val ALREADY_CHECKED_OUT = "ATT004"

    // 근무 일정 관련 (SCHEDULE)
    const val SCHEDULE_NOT_FOUND = "SCH001"
    const val DUPLICATE_SCHEDULE = "SCH002"
    const val CONFIRMED_SCHEDULE_CANNOT_BE_MODIFIED = "SCH003"
    const val EMPLOYEE_NOT_BELONG_TO_STORE = "SCH004"
    const val MANAGER_CAN_ONLY_MANAGE_OWN_STORE = "SCH005"
}
