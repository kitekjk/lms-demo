package com.lms.domain.model.employee

import java.util.UUID

@JvmInline
value class EmployeeId(val value: String) {
    init {
        require(value.isNotBlank()) { "EmployeeId는 비어있을 수 없습니다." }
    }

    companion object {
        fun generate(): EmployeeId = EmployeeId(UUID.randomUUID().toString())
        fun from(value: String): EmployeeId = EmployeeId(value)
    }
}
