package com.lms.domain.model.payroll

import com.lms.domain.common.DomainContext
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

/**
 * 급여 정책 Aggregate Root
 * 초과근무, 야간근무, 휴일근무 등의 급여 정책을 관리
 */
data class PayrollPolicy private constructor(
    val id: PayrollPolicyId,
    val policyType: PolicyType,
    val multiplier: PolicyMultiplier,
    val effectivePeriod: PolicyEffectivePeriod,
    val description: String?,
    val createdAt: Instant
) {
    companion object {
        /**
         * 새로운 급여 정책 생성
         */
        fun create(
            context: DomainContext,
            policyType: PolicyType,
            multiplier: PolicyMultiplier,
            effectivePeriod: PolicyEffectivePeriod,
            description: String?
        ): PayrollPolicy = PayrollPolicy(
            id = PayrollPolicyId.generate(),
            policyType = policyType,
            multiplier = multiplier,
            effectivePeriod = effectivePeriod,
            description = description,
            createdAt = context.requestedAt
        )

        /**
         * 기존 급여 정책 재구성 (Repository에서 조회 시)
         */
        fun reconstruct(
            id: PayrollPolicyId,
            policyType: PolicyType,
            multiplier: PolicyMultiplier,
            effectivePeriod: PolicyEffectivePeriod,
            description: String?,
            createdAt: Instant
        ): PayrollPolicy = PayrollPolicy(
            id,
            policyType,
            multiplier,
            effectivePeriod,
            description,
            createdAt
        )
    }

    /**
     * 정책 종료
     */
    fun terminate(context: DomainContext, endDate: LocalDate): PayrollPolicy {
        val newPeriod = effectivePeriod.terminate(endDate)
        return this.copy(effectivePeriod = newPeriod)
    }

    /**
     * 배율 변경 (새로운 정책으로 생성하는 것을 권장하지만, 필요 시 사용)
     */
    fun updateMultiplier(context: DomainContext, newMultiplier: PolicyMultiplier): PayrollPolicy {
        require(effectivePeriod.isCurrentlyEffective()) {
            "유효하지 않은 정책은 수정할 수 없습니다."
        }
        return this.copy(multiplier = newMultiplier)
    }

    /**
     * 특정 날짜에 유효한지 확인
     */
    fun isEffectiveOn(date: LocalDate): Boolean = effectivePeriod.isEffectiveOn(date)

    /**
     * 현재 유효한지 확인
     */
    fun isCurrentlyEffective(): Boolean = effectivePeriod.isCurrentlyEffective()

    /**
     * 금액에 정책 배율 적용
     */
    fun applyTo(baseAmount: BigDecimal): BigDecimal = baseAmount * multiplier.value
}
