package com.lms.domain.model.user

import java.util.UUID

@JvmInline
value class UserId(val value: String) {
    init {
        require(value.isNotBlank()) { "UserId는 비어있을 수 없습니다." }
    }

    companion object {
        fun generate(): UserId = UserId(UUID.randomUUID().toString())
        fun from(value: String): UserId = UserId(value)
    }
}
