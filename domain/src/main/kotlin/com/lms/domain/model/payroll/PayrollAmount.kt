package com.lms.domain.model.payroll

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 급여 금액 Value Object
 */
data class PayrollAmount(
    val baseAmount: BigDecimal,
    val overtimeAmount: BigDecimal,
    val deductions: BigDecimal = BigDecimal.ZERO
) {
    init {
        require(baseAmount >= BigDecimal.ZERO) {
            "기본 급여는 0 이상이어야 합니다. 입력값: $baseAmount"
        }
        require(overtimeAmount >= BigDecimal.ZERO) {
            "초과 근무 수당은 0 이상이어야 합니다. 입력값: $overtimeAmount"
        }
        require(deductions >= BigDecimal.ZERO) {
            "공제액은 0 이상이어야 합니다. 입력값: $deductions"
        }
    }

    /**
     * 총 급여 계산 (기본급 + 초과근무수당 - 공제액)
     */
    fun calculateTotal(): BigDecimal = (baseAmount + overtimeAmount - deductions)
        .setScale(2, RoundingMode.HALF_UP)

    /**
     * 초과근무수당 추가
     */
    fun addOvertime(amount: BigDecimal): PayrollAmount {
        require(amount >= BigDecimal.ZERO) {
            "추가할 초과근무수당은 0 이상이어야 합니다."
        }
        return this.copy(overtimeAmount = overtimeAmount + amount)
    }

    /**
     * 공제액 추가
     */
    fun addDeduction(amount: BigDecimal): PayrollAmount {
        require(amount >= BigDecimal.ZERO) {
            "추가할 공제액은 0 이상이어야 합니다."
        }
        return this.copy(deductions = deductions + amount)
    }

    companion object {
        /**
         * 기본 급여만으로 생성
         */
        fun fromBase(baseAmount: BigDecimal): PayrollAmount = PayrollAmount(
            baseAmount = baseAmount,
            overtimeAmount = BigDecimal.ZERO,
            deductions = BigDecimal.ZERO
        )
    }
}
