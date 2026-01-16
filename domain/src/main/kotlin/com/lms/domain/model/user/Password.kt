package com.lms.domain.model.user

@JvmInline
value class Password(val encodedValue: String) {
    init {
        require(encodedValue.isNotBlank()) { "비밀번호는 비어있을 수 없습니다." }
    }
}
