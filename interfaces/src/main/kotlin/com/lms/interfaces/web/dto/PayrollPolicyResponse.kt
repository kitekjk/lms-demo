package com.lms.interfaces.web.dto

import com.lms.application.payroll.dto.PayrollPolicyResult
import com.lms.domain.model.payroll.PolicyType
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

/**
 * 급여 정책 응답
 */
data class PayrollPolicyResponse(
    val id: String,
    val policyType: PolicyType,
    val policyTypeDescription: String,
    val multiplier: BigDecimal,
    val effectiveFrom: LocalDate,
    val effectiveTo: LocalDate?,
    val description: String?,
    val isCurrentlyEffective: Boolean,
    val createdAt: Instant
) {
    companion object {
        fun from(result: PayrollPolicyResult): PayrollPolicyResponse = PayrollPolicyResponse(
            id = result.id,
            policyType = result.policyType,
            policyTypeDescription = result.policyType.description,
            multiplier = result.multiplier,
            effectiveFrom = result.effectiveFrom,
            effectiveTo = result.effectiveTo,
            description = result.description,
            isCurrentlyEffective = result.isCurrentlyEffective,
            createdAt = result.createdAt
        )
    }
}

/**
 * 급여 정책 목록 응답
 */
data class PayrollPolicyListResponse(val policies: List<PayrollPolicyResponse>, val totalCount: Int)
