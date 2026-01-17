package com.lms.domain.model.payroll

/**
 * 근무 유형
 * 급여 계산 시 가산율 적용 기준
 */
enum class WorkType {
    /** 평일 기본 근무 */
    WEEKDAY,

    /** 야간 근무 (22:00 ~ 06:00) */
    NIGHT,

    /** 주말 근무 (토요일, 일요일) */
    WEEKEND,

    /** 공휴일 근무 */
    HOLIDAY
}
