package com.lms.interfaces.web.dto

import com.lms.domain.model.employee.EmployeeType
import jakarta.validation.constraints.NotBlank

/**
 * 근로자 생성 요청 DTO
 */
data class EmployeeCreateRequest(
    @field:NotBlank(message = "사용자 ID는 필수입니다")
    val userId: String,

    @field:NotBlank(message = "이름은 필수입니다")
    val name: String,

    val employeeType: EmployeeType,

    val storeId: String?
)

/**
 * 근로자 수정 요청 DTO
 */
data class EmployeeUpdateRequest(
    @field:NotBlank(message = "이름은 필수입니다")
    val name: String,

    val employeeType: EmployeeType,

    val storeId: String?
)
