package com.lms.domain.model.payroll

/**
 * 급여 정책 유형
 */
enum class PolicyType(val description: String) {
    OVERTIME_WEEKDAY("평일 초과근무"),
    OVERTIME_WEEKEND("주말 초과근무"),
    OVERTIME_HOLIDAY("공휴일 초과근무"),
    NIGHT_SHIFT("야간 근무"),
    HOLIDAY_WORK("휴일 근무"),
    BONUS("보너스"),
    ALLOWANCE("수당")
}
