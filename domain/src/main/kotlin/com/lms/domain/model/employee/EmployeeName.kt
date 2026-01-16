package com.lms.domain.model.employee

@JvmInline
value class EmployeeName(val value: String) {
    init {
        require(value.isNotBlank()) { "이름은 비어있을 수 없습니다." }
        require(value.length <= 100) { "이름은 100자를 초과할 수 없습니다." }
    }
}
