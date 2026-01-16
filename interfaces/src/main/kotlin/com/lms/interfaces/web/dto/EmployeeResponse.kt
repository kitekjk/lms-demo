package com.lms.interfaces.web.dto

import com.lms.domain.model.employee.EmployeeType
import java.math.BigDecimal
import java.time.Instant

/**
 * 근로자 응답 DTO
 */
data class EmployeeResponse(
    val id: String,
    val userId: String,
    val name: String,
    val employeeType: EmployeeType,
    val storeId: String?,
    val remainingLeave: BigDecimal,
    val isActive: Boolean,
    val createdAt: Instant
)

/**
 * 근로자 목록 응답 DTO
 */
data class EmployeeListResponse(val employees: List<EmployeeResponse>, val totalCount: Int)
