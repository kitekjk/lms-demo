package com.lms.domain.service

import com.lms.domain.model.employee.EmployeeType
import com.lms.domain.model.employee.RemainingLeave
import java.math.BigDecimal

/**
 * 휴가 정책 도메인 서비스
 * 직급별 연차 정책을 관리하고 초기 연차를 설정
 *
 * Note: 이 서비스는 순수 도메인 로직이므로 Spring 의존성이 없음
 */
class LeavePolicyService {

    /**
     * 직급별 기본 연차 일수 조회
     *
     * @param employeeType 근로자 직급
     * @return 기본 연차 일수
     */
    fun getAnnualLeaveByEmployeeType(employeeType: EmployeeType): BigDecimal = when (employeeType) {
        EmployeeType.REGULAR -> BigDecimal("15") // 정규직: 15일
        EmployeeType.IRREGULAR -> BigDecimal("11") // 비정규직: 11일
        EmployeeType.PART_TIME -> BigDecimal.ZERO // 파트타임: 0일 (시간제 근로)
    }

    /**
     * 근로자 등록 시 초기 연차 설정
     *
     * @param employeeType 근로자 직급
     * @return 초기 잔여 연차
     */
    fun initializeEmployeeLeave(employeeType: EmployeeType): RemainingLeave {
        val annualLeaveDays = getAnnualLeaveByEmployeeType(employeeType)
        return RemainingLeave(annualLeaveDays)
    }

    /**
     * 연차 정책 설명 조회
     *
     * @param employeeType 근로자 직급
     * @return 정책 설명
     */
    fun getLeavePolicyDescription(employeeType: EmployeeType): String = when (employeeType) {
        EmployeeType.REGULAR -> "정규직은 연간 15일의 유급 연차를 부여받습니다."
        EmployeeType.IRREGULAR -> "비정규직은 연간 11일의 유급 연차를 부여받습니다."
        EmployeeType.PART_TIME -> "파트타임 근로자는 별도의 연차가 부여되지 않으며, 필요 시 무급 휴가를 신청할 수 있습니다."
    }

    /**
     * 근로자가 휴가를 신청할 수 있는지 검증
     *
     * @param employeeType 근로자 직급
     * @param remainingLeave 현재 잔여 연차
     * @param requestedDays 신청하려는 휴가 일수
     * @return 신청 가능 여부
     */
    fun canRequestLeave(
        employeeType: EmployeeType,
        remainingLeave: RemainingLeave,
        requestedDays: BigDecimal
    ): Boolean {
        // 파트타임 근로자는 무급 휴가만 가능하므로 연차 확인 불필요
        if (employeeType == EmployeeType.PART_TIME) {
            return true
        }

        // 정규직/비정규직은 잔여 연차 확인
        return remainingLeave.value >= requestedDays
    }

    /**
     * 휴가 신청 가능 여부를 확인하고 불가능하면 에러 메시지 반환
     *
     * @param employeeType 근로자 직급
     * @param remainingLeave 현재 잔여 연차
     * @param requestedDays 신청하려는 휴가 일수
     * @return 불가능한 경우 에러 메시지, 가능하면 null
     */
    fun validateLeaveRequest(
        employeeType: EmployeeType,
        remainingLeave: RemainingLeave,
        requestedDays: BigDecimal
    ): String? {
        if (!canRequestLeave(employeeType, remainingLeave, requestedDays)) {
            return "잔여 연차가 부족합니다. 신청: ${requestedDays}일, 잔여: ${remainingLeave.value}일"
        }
        return null
    }
}
