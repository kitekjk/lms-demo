package com.lms.application.employee.dto

import com.lms.domain.model.employee.Employee
import com.lms.domain.model.employee.EmployeeType
import java.math.BigDecimal
import java.time.Instant

/**
 * 근로자 생성 Command
 */
data class CreateEmployeeCommand(
    val userId: String,
    val name: String,
    val employeeType: EmployeeType,
    val storeId: String?
)

/**
 * 근로자 수정 Command
 */
data class UpdateEmployeeCommand(val name: String, val employeeType: EmployeeType, val storeId: String?)

/**
 * 근로자 조회 Result
 */
data class EmployeeResult(
    val id: String,
    val userId: String,
    val name: String,
    val employeeType: EmployeeType,
    val storeId: String?,
    val remainingLeave: BigDecimal,
    val isActive: Boolean,
    val createdAt: Instant
) {
    companion object {
        fun from(employee: Employee): EmployeeResult = EmployeeResult(
            id = employee.id.value,
            userId = employee.userId.value,
            name = employee.name.value,
            employeeType = employee.employeeType,
            storeId = employee.storeId?.value,
            remainingLeave = employee.remainingLeave.value,
            isActive = employee.isActive,
            createdAt = employee.createdAt
        )
    }
}
