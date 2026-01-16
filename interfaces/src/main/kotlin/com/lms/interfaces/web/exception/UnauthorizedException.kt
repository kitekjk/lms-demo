package com.lms.interfaces.web.exception

/**
 * 인증 실패 예외
 * 로그인 실패, 유효하지 않은 토큰 등
 */
class UnauthorizedException(message: String = "인증에 실패했습니다.") : BusinessException(message, "UNAUTHORIZED")
