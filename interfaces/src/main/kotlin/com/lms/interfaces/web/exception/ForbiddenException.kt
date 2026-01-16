package com.lms.interfaces.web.exception

/**
 * 권한 없음 예외
 * 접근 권한이 없는 리소스에 접근 시도
 */
class ForbiddenException(
    message: String = "접근 권한이 없습니다."
) : BusinessException(message, "FORBIDDEN")
