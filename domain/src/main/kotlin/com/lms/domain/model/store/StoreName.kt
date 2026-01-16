package com.lms.domain.model.store

@JvmInline
value class StoreName(val value: String) {
    init {
        require(value.isNotBlank()) { "매장 이름은 비어있을 수 없습니다." }
        require(value.length <= 100) { "매장 이름은 100자를 초과할 수 없습니다." }
    }
}
