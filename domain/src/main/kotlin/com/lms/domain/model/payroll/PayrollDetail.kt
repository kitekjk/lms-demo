package com.lms.domain.model.payroll

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

/**
 * 급여 상세 내역 Entity
 * 일별 근무 유형별 급여 계산 내역을 저장
 */
data class PayrollDetail private constructor(
    val id: PayrollDetailId,
    val payrollId: PayrollId,
    val workDate: LocalDate,
    val workType: WorkType,
    val hours: BigDecimal,
    val hourlyRate: BigDecimal,
    val multiplier: BigDecimal,
    val amount: BigDecimal
) {
    init {
        require(hours >= BigDecimal.ZERO) {
            "근무 시간은 0 이상이어야 합니다. 입력값: $hours"
        }
        require(hourlyRate > BigDecimal.ZERO) {
            "시급은 0보다 커야 합니다. 입력값: $hourlyRate"
        }
        require(multiplier >= BigDecimal.ZERO) {
            "가산율은 0 이상이어야 합니다. 입력값: $multiplier"
        }
    }

    companion object {
        /**
         * 새로운 급여 상세 내역 생성
         */
        fun create(
            payrollId: PayrollId,
            workDate: LocalDate,
            workType: WorkType,
            hours: BigDecimal,
            hourlyRate: BigDecimal,
            multiplier: BigDecimal
        ): PayrollDetail {
            val calculatedAmount = calculateAmount(hours, hourlyRate, multiplier)

            return PayrollDetail(
                id = PayrollDetailId.generate(),
                payrollId = payrollId,
                workDate = workDate,
                workType = workType,
                hours = hours,
                hourlyRate = hourlyRate,
                multiplier = multiplier,
                amount = calculatedAmount
            )
        }

        /**
         * 기존 급여 상세 내역 재구성 (Repository에서 조회 시)
         */
        fun reconstruct(
            id: PayrollDetailId,
            payrollId: PayrollId,
            workDate: LocalDate,
            workType: WorkType,
            hours: BigDecimal,
            hourlyRate: BigDecimal,
            multiplier: BigDecimal,
            amount: BigDecimal
        ): PayrollDetail = PayrollDetail(
            id,
            payrollId,
            workDate,
            workType,
            hours,
            hourlyRate,
            multiplier,
            amount
        )

        /**
         * 금액 계산 (시간 × 시급 × 가산율)
         */
        private fun calculateAmount(hours: BigDecimal, hourlyRate: BigDecimal, multiplier: BigDecimal): BigDecimal =
            (hours * hourlyRate * multiplier).setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * 금액 재계산
     */
    fun recalculate(): PayrollDetail {
        val newAmount = (hours * hourlyRate * multiplier).setScale(2, RoundingMode.HALF_UP)
        return this.copy(amount = newAmount)
    }
}
