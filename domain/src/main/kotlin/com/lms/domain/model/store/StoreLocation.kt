package com.lms.domain.model.store

@JvmInline
value class StoreLocation(val value: String) {
    init {
        require(value.isNotBlank()) { "매장 위치는 비어있을 수 없습니다." }
        require(value.length <= 200) { "매장 위치는 200자를 초과할 수 없습니다." }
    }
}
