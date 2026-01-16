package com.lms.domain.model.employee

/**
 * 근로자 유형
 */
enum class EmployeeType(val description: String) {
    REGULAR("정규직"),
    IRREGULAR("계약직"),
    PART_TIME("아르바이트")
}
