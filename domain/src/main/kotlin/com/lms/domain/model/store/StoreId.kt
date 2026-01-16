package com.lms.domain.model.store

import java.util.UUID

@JvmInline
value class StoreId(val value: String) {
    init {
        require(value.isNotBlank()) { "StoreId는 비어있을 수 없습니다." }
    }

    companion object {
        fun generate(): StoreId = StoreId(UUID.randomUUID().toString())
        fun from(value: String): StoreId = StoreId(value)
    }
}
