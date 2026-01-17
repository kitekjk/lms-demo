package com.lms.application.payroll.dto

import com.lms.domain.model.payroll.PayrollPolicy
import com.lms.domain.model.payroll.PolicyType
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

/**
 * 급여 정책 생성 Command
 */
data class CreatePayrollPolicyCommand(
    val policyType: PolicyType,
    val multiplier: BigDecimal,
    val effectiveFrom: LocalDate,
    val effectiveTo: LocalDate?,
    val description: String?
)

/**
 * 급여 정책 수정 Command
 */
data class UpdatePayrollPolicyCommand(
    val multiplier: BigDecimal?,
    val effectiveTo: LocalDate?,
    val description: String?
)

/**
 * 급여 정책 Result
 */
data class PayrollPolicyResult(
    val id: String,
    val policyType: PolicyType,
    val multiplier: BigDecimal,
    val effectiveFrom: LocalDate,
    val effectiveTo: LocalDate?,
    val description: String?,
    val isCurrentlyEffective: Boolean,
    val createdAt: Instant
) {
    companion object {
        fun from(policy: PayrollPolicy): PayrollPolicyResult = PayrollPolicyResult(
            id = policy.id.value,
            policyType = policy.policyType,
            multiplier = policy.multiplier.value,
            effectiveFrom = policy.effectivePeriod.effectiveFrom,
            effectiveTo = policy.effectivePeriod.effectiveTo,
            description = policy.description,
            isCurrentlyEffective = policy.isCurrentlyEffective(),
            createdAt = policy.createdAt
        )
    }
}
