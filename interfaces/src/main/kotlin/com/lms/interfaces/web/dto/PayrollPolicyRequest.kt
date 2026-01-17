package com.lms.interfaces.web.dto

import com.lms.domain.model.payroll.PolicyType
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDate

/**
 * 급여 정책 생성 요청
 */
data class PayrollPolicyCreateRequest(
    @field:NotNull(message = "정책 유형은 필수입니다")
    val policyType: PolicyType,

    @field:NotNull(message = "배율은 필수입니다")
    @field:DecimalMin(value = "0.0", message = "배율은 0 이상이어야 합니다")
    @field:DecimalMax(value = "10.0", message = "배율은 10.0 이하여야 합니다")
    val multiplier: BigDecimal,

    @field:NotNull(message = "시작일은 필수입니다")
    val effectiveFrom: LocalDate,

    val effectiveTo: LocalDate?,

    val description: String?
)

/**
 * 급여 정책 수정 요청
 */
data class PayrollPolicyUpdateRequest(
    @field:DecimalMin(value = "0.0", message = "배율은 0 이상이어야 합니다")
    @field:DecimalMax(value = "10.0", message = "배율은 10.0 이하여야 합니다")
    val multiplier: BigDecimal?,

    val effectiveTo: LocalDate?,

    val description: String?
)
