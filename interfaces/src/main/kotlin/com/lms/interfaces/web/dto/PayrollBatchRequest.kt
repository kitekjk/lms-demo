package com.lms.interfaces.web.dto

import com.lms.application.payroll.dto.ExecutePayrollBatchCommand
import jakarta.validation.constraints.NotNull
import java.time.YearMonth

/**
 * 급여 배치 실행 요청
 */
data class PayrollBatchExecuteRequest(
    @field:NotNull(message = "급여 기간은 필수입니다")
    val period: YearMonth,

    val storeId: String?
) {
    fun toCommand(): ExecutePayrollBatchCommand = ExecutePayrollBatchCommand(
        period = period,
        storeId = storeId
    )
}
