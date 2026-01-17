package com.lms.domain.service

import com.lms.domain.common.DomainContext
import com.lms.domain.model.attendance.AttendanceRecord
import com.lms.domain.model.leave.LeaveRequest
import com.lms.domain.model.payroll.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * 급여 계산 엔진 (Domain Service)
 * 순수 Kotlin으로 구현되며 Spring Framework에 의존하지 않음
 *
 * 모든 계산 로직은 도메인 규칙에 따라 처리됨
 */
class PayrollCalculationEngine {
    /**
     * 급여 계산 결과
     */
    data class PayrollCalculationResult(
        val baseAmount: BigDecimal,
        val overtimeAmount: BigDecimal,
        val totalAmount: BigDecimal,
        val details: List<PayrollDetailData>
    )

    /**
     * 급여 상세 데이터
     */
    data class PayrollDetailData(
        val workDate: LocalDate,
        val workType: WorkType,
        val hours: BigDecimal,
        val hourlyRate: BigDecimal,
        val multiplier: BigDecimal,
        val amount: BigDecimal
    )

    /**
     * 출퇴근 기록과 정책을 기반으로 급여 계산
     *
     * @param attendanceRecords 출퇴근 기록 목록
     * @param approvedLeaves 승인된 휴가 목록
     * @param hourlyRate 시급
     * @param policies 가산율 정책 목록
     * @return 급여 계산 결과
     */
    fun calculate(
        context: DomainContext,
        attendanceRecords: List<AttendanceRecord>,
        approvedLeaves: List<LeaveRequest>,
        hourlyRate: BigDecimal,
        policies: List<PayrollPolicy>
    ): PayrollCalculationResult {
        // 휴가 날짜 추출 (중복 제거)
        val leaveDates = approvedLeaves.flatMap { leave ->
            val start = leave.leavePeriod.startDate
            val end = leave.leavePeriod.endDate
            generateSequence(start) { it.plusDays(1) }
                .takeWhile { !it.isAfter(end) }
                .toList()
        }.toSet()

        // 일별 급여 상세 계산
        val details = attendanceRecords
            .filter { it.attendanceTime.isCompleted() } // 퇴근 처리된 기록만
            .filterNot { leaveDates.contains(it.attendanceDate) } // 휴가 날짜 제외
            .mapNotNull { record ->
                calculateDailyPayroll(record, hourlyRate, policies)
            }

        // 기본급과 가산 금액 분리
        val baseAmount = details
            .filter { it.workType == WorkType.WEEKDAY }
            .sumOf { it.amount }

        val overtimeAmount = details
            .filter { it.workType != WorkType.WEEKDAY }
            .sumOf { it.amount }

        val totalAmount = (baseAmount + overtimeAmount).setScale(2, RoundingMode.HALF_UP)

        return PayrollCalculationResult(
            baseAmount = baseAmount,
            overtimeAmount = overtimeAmount,
            totalAmount = totalAmount,
            details = details
        )
    }

    /**
     * 일별 급여 계산
     */
    private fun calculateDailyPayroll(
        attendance: AttendanceRecord,
        hourlyRate: BigDecimal,
        policies: List<PayrollPolicy>
    ): PayrollDetailData? {
        val workHours = attendance.attendanceTime.calculateActualWorkHours() ?: return null
        val workDate = attendance.attendanceDate

        // 근무 유형 판단
        val workType = determineWorkType(attendance)

        // 가산율 조회
        val multiplier = findMultiplier(workType, workDate, policies)

        // 금액 계산
        val hours = BigDecimal.valueOf(workHours).setScale(2, RoundingMode.HALF_UP)
        val amount = (hours * hourlyRate * multiplier).setScale(2, RoundingMode.HALF_UP)

        return PayrollDetailData(
            workDate = workDate,
            workType = workType,
            hours = hours,
            hourlyRate = hourlyRate,
            multiplier = multiplier,
            amount = amount
        )
    }

    /**
     * 근무 유형 판단
     * 우선순위: 공휴일 > 주말 > 야간 > 평일
     */
    private fun determineWorkType(attendance: AttendanceRecord): WorkType {
        val date = attendance.attendanceDate
        val dayOfWeek = date.dayOfWeek

        // 공휴일 판단 (간단한 로직 - 실제로는 공휴일 테이블 또는 API 사용)
        if (isHoliday(date)) {
            return WorkType.HOLIDAY
        }

        // 주말 판단 (토요일, 일요일)
        if (dayOfWeek == java.time.DayOfWeek.SATURDAY || dayOfWeek == java.time.DayOfWeek.SUNDAY) {
            return WorkType.WEEKEND
        }

        // 야간 근무 판단 (22:00 ~ 06:00)
        if (isNightWork(attendance)) {
            return WorkType.NIGHT
        }

        // 기본 평일 근무
        return WorkType.WEEKDAY
    }

    /**
     * 야간 근무 여부 판단 (22:00 ~ 06:00)
     */
    private fun isNightWork(attendance: AttendanceRecord): Boolean {
        val checkInTime = attendance.attendanceTime.checkInTime
        val checkOutTime = attendance.attendanceTime.checkOutTime ?: return false

        val zoneId = ZoneId.systemDefault()
        val checkInLocalTime = checkInTime.atZone(zoneId).toLocalTime()
        val checkOutLocalTime = checkOutTime.atZone(zoneId).toLocalTime()

        val nightStart = LocalTime.of(22, 0)
        val nightEnd = LocalTime.of(6, 0)

        // 출근 시간이 22:00 이후이거나 퇴근 시간이 06:00 이전인 경우
        return checkInLocalTime.isAfter(nightStart) ||
            checkInLocalTime.isBefore(nightEnd) ||
            checkOutLocalTime.isAfter(nightStart) ||
            checkOutLocalTime.isBefore(nightEnd)
    }

    /**
     * 공휴일 판단
     * TODO: 실제로는 별도의 공휴일 테이블이나 외부 API를 사용해야 함
     */
    private fun isHoliday(date: LocalDate): Boolean {
        // 간단한 예시: 1월 1일(신정), 12월 25일(크리스마스)만 공휴일로 처리
        return (date.monthValue == 1 && date.dayOfMonth == 1) ||
            (date.monthValue == 12 && date.dayOfMonth == 25)
    }

    /**
     * 근무 유형과 날짜에 해당하는 가산율 조회
     */
    private fun findMultiplier(workType: WorkType, workDate: LocalDate, policies: List<PayrollPolicy>): BigDecimal {
        // WorkType을 PolicyType으로 매핑
        val policyType = when (workType) {
            WorkType.WEEKDAY -> return BigDecimal.ONE // 평일은 가산율 1.0
            WorkType.NIGHT -> PolicyType.OVERTIME_WEEKDAY // 야간 근무는 평일 연장근무로 처리
            WorkType.WEEKEND -> PolicyType.OVERTIME_WEEKEND
            WorkType.HOLIDAY -> PolicyType.OVERTIME_HOLIDAY
        }

        // 해당 날짜에 유효한 정책 조회
        val policy = policies
            .filter { it.policyType == policyType }
            .firstOrNull { it.effectivePeriod.isEffectiveOn(workDate) }

        return policy?.multiplier?.value ?: BigDecimal.ONE
    }

    /**
     * 기본급 계산 (평일 근무만)
     */
    fun calculateBaseAmount(details: List<PayrollDetailData>): BigDecimal = details
        .filter { it.workType == WorkType.WEEKDAY }
        .sumOf { it.amount }
        .setScale(2, RoundingMode.HALF_UP)

    /**
     * 가산 금액 계산 (야간/주말/공휴일)
     */
    fun calculateOvertimeAmount(details: List<PayrollDetailData>): BigDecimal = details
        .filter { it.workType != WorkType.WEEKDAY }
        .sumOf { it.amount }
        .setScale(2, RoundingMode.HALF_UP)

    /**
     * 총 급여 계산
     */
    fun calculateTotalAmount(baseAmount: BigDecimal, overtimeAmount: BigDecimal): BigDecimal =
        (baseAmount + overtimeAmount).setScale(2, RoundingMode.HALF_UP)
}
