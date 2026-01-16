package com.lms.interfaces.web.exception

/**
 * 비즈니스 로직 예외
 * 도메인 규칙 위반 시 발생
 */
open class BusinessException(
    override val message: String,
    val errorCode: String? = null
) : RuntimeException(message)
