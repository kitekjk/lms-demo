package com.lms.interfaces.web.dto

import com.lms.application.payroll.dto.PayrollDetailResult
import com.lms.application.payroll.dto.PayrollResult
import com.lms.application.payroll.dto.PayrollWithDetailsResult
import com.lms.domain.model.payroll.WorkType
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

/**
 * 급여 응답
 */
data class PayrollResponse(
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
        fun from(result: PayrollResult): PayrollResponse = PayrollResponse(
            id = result.id,
            employeeId = result.employeeId,
            period = result.period,
            baseAmount = result.baseAmount,
            overtimeAmount = result.overtimeAmount,
            totalAmount = result.totalAmount,
            isPaid = result.isPaid,
            paidAt = result.paidAt,
            calculatedAt = result.calculatedAt,
            createdAt = result.createdAt
        )
    }
}

/**
 * 급여 상세 응답
 */
data class PayrollDetailResponse(
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
        fun from(result: PayrollDetailResult): PayrollDetailResponse = PayrollDetailResponse(
            id = result.id,
            payrollId = result.payrollId,
            workDate = result.workDate,
            workType = result.workType,
            hours = result.hours,
            hourlyRate = result.hourlyRate,
            multiplier = result.multiplier,
            amount = result.amount
        )
    }
}

/**
 * 급여 및 상세 응답
 */
data class PayrollWithDetailsResponse(val payroll: PayrollResponse, val details: List<PayrollDetailResponse>) {
    companion object {
        fun from(result: PayrollWithDetailsResult): PayrollWithDetailsResponse = PayrollWithDetailsResponse(
            payroll = PayrollResponse.from(result.payroll),
            details = result.details.map { PayrollDetailResponse.from(it) }
        )
    }
}
