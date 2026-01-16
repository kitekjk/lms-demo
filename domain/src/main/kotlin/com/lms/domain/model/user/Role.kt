package com.lms.domain.model.user

/**
 * 사용자 역할
 * RBAC (Role Based Access Control)에 사용
 */
enum class Role(val description: String) {
    SUPER_ADMIN("슈퍼 관리자"),
    MANAGER("매니저"),
    EMPLOYEE("근로자")
}
