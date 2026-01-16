package com.lms.domain.model.user

@JvmInline
value class Email(val value: String) {
    init {
        require(value.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) {
            "유효하지 않은 이메일 형식입니다: $value"
        }
    }
}
