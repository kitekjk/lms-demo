package com.lms.domain.model.payroll

import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * 급여 지급 기간 Value Object
 * 형식: YYYY-MM (예: "2024-01")
 */
@JvmInline
value class PayrollPeriod(val value: String) {
    init {
        require(value.matches(Regex("^\\d{4}-\\d{2}$"))) {
            "급여 기간은 YYYY-MM 형식이어야 합니다. 입력값: $value"
        }
    }

    fun toYearMonth(): YearMonth = YearMonth.parse(value, DateTimeFormatter.ofPattern("yyyy-MM"))

    companion object {
        fun from(yearMonth: YearMonth): PayrollPeriod =
            PayrollPeriod(yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")))

        fun from(value: String): PayrollPeriod = PayrollPeriod(value)

        fun of(year: Int, month: Int): PayrollPeriod = from(YearMonth.of(year, month))
    }
}
