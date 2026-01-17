package com.lms.application.payroll.dto

import com.lms.domain.model.payroll.Payroll
import com.lms.domain.model.payroll.PayrollDetail
import com.lms.domain.model.payroll.WorkType
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

/**
 * 급여 계산 Command
 */
data class CalculatePayrollCommand(val employeeId: String, val period: YearMonth, val hourlyRate: BigDecimal)

/**
 * 배치 급여 계산 Command
 */
data class CalculateBatchPayrollCommand(val storeId: String, val period: YearMonth)

/**
 * 급여 Result
 */
data class PayrollResult(
    val id: String,
    val employeeId: String,
    val period: YearMonth,
    val baseAmount: BigDecimal,
    val overtimeAmount: BigDecimal,
    val totalAmount: BigDecimal,
    val isPaid: Boolean,
    val paidAt: Instant?,
    val calculatedAt: Instant,
    val createdAt: Instant
) {
    companion object {
        fun from(payroll: Payroll): PayrollResult = PayrollResult(
            id = payroll.id.value,
            employeeId = payroll.employeeId.value,
            period = payroll.period.toYearMonth(),
            baseAmount = payroll.amount.baseAmount,
            overtimeAmount = payroll.amount.overtimeAmount,
            totalAmount = payroll.calculateTotalAmount(),
            isPaid = payroll.isPaid,
            paidAt = payroll.paidAt,
            calculatedAt = payroll.calculatedAt,
            createdAt = payroll.createdAt
        )
    }
}

/**
 * 급여 상세 Result
 */
data class PayrollDetailResult(
    val id: String,
    val payrollId: String,
    val workDate: LocalDate,
    val workType: WorkType,
    val hours: BigDecimal,
    val hourlyRate: BigDecimal,
    val multiplier: BigDecimal,
    val amount: BigDecimal
) {
    companion object {
        fun from(detail: PayrollDetail): PayrollDetailResult = PayrollDetailResult(
            id = detail.id.value,
            payrollId = detail.payrollId.value,
            workDate = detail.workDate,
            workType = detail.workType,
            hours = detail.hours,
            hourlyRate = detail.hourlyRate,
            multiplier = detail.multiplier,
            amount = detail.amount
        )
    }
}

/**
 * 급여 상세 조회 Result (급여 + 상세 목록)
 */
data class PayrollWithDetailsResult(val payroll: PayrollResult, val details: List<PayrollDetailResult>)
