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
}
