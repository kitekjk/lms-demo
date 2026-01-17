package com.lms.interfaces.web.dto

import com.lms.application.payroll.dto.CalculatePayrollCommand
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.YearMonth

/**
 * 급여 계산 요청
 */
data class PayrollCalculateRequest(
    @field:NotBlank(message = "근로자 ID는 필수입니다")
    val employeeId: String,

    @field:NotNull(message = "급여 기간은 필수입니다")
    val period: YearMonth,

    @field:NotNull(message = "시급은 필수입니다")
    @field:DecimalMin(value = "0.0", inclusive = false, message = "시급은 0보다 커야 합니다")
    val hourlyRate: BigDecimal
) {
    fun toCommand(): CalculatePayrollCommand = CalculatePayrollCommand(
        employeeId = employeeId,
        period = period,
        hourlyRate = hourlyRate
    )
}
