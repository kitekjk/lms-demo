package com.lms.domain.model.payroll

import com.lms.domain.common.DomainContext
import com.lms.domain.model.employee.EmployeeId
import java.math.BigDecimal
import java.time.Instant

/**
 * 급여 Aggregate Root
 * 근로자의 급여 정보 및 계산을 관리
 */
data class Payroll private constructor(
    val id: PayrollId,
    val employeeId: EmployeeId,
    val period: PayrollPeriod,
    val amount: PayrollAmount,
    val calculatedAt: Instant,
    val isPaid: Boolean,
    val paidAt: Instant?,
    val createdAt: Instant
) {
    companion object {
        /**
         * 새로운 급여 생성
         */
        fun create(
            context: DomainContext,
            employeeId: EmployeeId,
            period: PayrollPeriod,
            amount: PayrollAmount
        ): Payroll = Payroll(
            id = PayrollId.generate(),
            employeeId = employeeId,
            period = period,
            amount = amount,
            calculatedAt = context.requestedAt,
            isPaid = false,
            paidAt = null,
            createdAt = context.requestedAt
        )

        /**
         * 기존 급여 재구성 (Repository에서 조회 시)
         */
        fun reconstruct(
            id: PayrollId,
            employeeId: EmployeeId,
            period: PayrollPeriod,
            amount: PayrollAmount,
            calculatedAt: Instant,
            isPaid: Boolean,
            paidAt: Instant?,
            createdAt: Instant
        ): Payroll = Payroll(
            id,
            employeeId,
            period,
            amount,
            calculatedAt,
            isPaid,
            paidAt,
            createdAt
        )
    }

    /**
     * 급여 지급 완료 처리
     */
    fun markAsPaid(context: DomainContext): Payroll {
        require(!isPaid) { "이미 지급된 급여입니다." }
        return this.copy(
            isPaid = true,
            paidAt = context.requestedAt
        )
    }

    /**
     * 초과근무수당 추가
     */
    fun addOvertime(context: DomainContext, overtimeAmount: BigDecimal): Payroll {
        require(!isPaid) { "이미 지급된 급여는 수정할 수 없습니다." }
        val newAmount = amount.addOvertime(overtimeAmount)
        return this.copy(
            amount = newAmount,
            calculatedAt = context.requestedAt
        )
    }

    /**
     * 공제액 추가
     */
    fun addDeduction(context: DomainContext, deductionAmount: BigDecimal): Payroll {
        require(!isPaid) { "이미 지급된 급여는 수정할 수 없습니다." }
        val newAmount = amount.addDeduction(deductionAmount)
        return this.copy(
            amount = newAmount,
            calculatedAt = context.requestedAt
        )
    }

    /**
     * 총 급여 계산
     */
    fun calculateTotalAmount(): BigDecimal = amount.calculateTotal()

    /**
     * 급여 재계산
     */
    fun recalculate(context: DomainContext, newAmount: PayrollAmount): Payroll {
        require(!isPaid) { "이미 지급된 급여는 재계산할 수 없습니다." }
        return this.copy(
            amount = newAmount,
            calculatedAt = context.requestedAt
        )
    }
}
