package com.lms.domain.model.schedule

import com.lms.domain.common.DomainContext
import com.lms.domain.model.employee.EmployeeId
import com.lms.domain.model.store.StoreId
import java.time.Instant

/**
 * 근무 일정 Aggregate Root
 * 근로자의 근무 일정을 관리
 */
data class WorkSchedule private constructor(
    val id: WorkScheduleId,
    val employeeId: EmployeeId,
    val storeId: StoreId,
    val workDate: WorkDate,
    val workTime: WorkTime,
    val isConfirmed: Boolean,
    val createdAt: Instant
) {
    companion object {
        /**
         * 새로운 근무 일정 생성
         */
        fun create(
            context: DomainContext,
            employeeId: EmployeeId,
            storeId: StoreId,
            workDate: WorkDate,
            workTime: WorkTime
        ): WorkSchedule = WorkSchedule(
            id = WorkScheduleId.generate(),
            employeeId = employeeId,
            storeId = storeId,
            workDate = workDate,
            workTime = workTime,
            isConfirmed = false,
            createdAt = context.requestedAt
        )

        /**
         * 기존 근무 일정 재구성 (Repository에서 조회 시)
         */
        fun reconstruct(
            id: WorkScheduleId,
            employeeId: EmployeeId,
            storeId: StoreId,
            workDate: WorkDate,
            workTime: WorkTime,
            isConfirmed: Boolean,
            createdAt: Instant
        ): WorkSchedule = WorkSchedule(
            id,
            employeeId,
            storeId,
            workDate,
            workTime,
            isConfirmed,
            createdAt
        )
    }

    /**
     * 근무 일정 확정
     */
    fun confirm(context: DomainContext): WorkSchedule {
        require(!isConfirmed) { "이미 확정된 근무 일정입니다." }
        return this.copy(isConfirmed = true)
    }

    /**
     * 근무 일정 확정 취소
     */
    fun unconfirm(context: DomainContext): WorkSchedule {
        require(isConfirmed) { "확정되지 않은 근무 일정입니다." }
        return this.copy(isConfirmed = false)
    }

    /**
     * 근무 시간 변경
     */
    fun changeWorkTime(context: DomainContext, newWorkTime: WorkTime): WorkSchedule {
        require(!isConfirmed) { "확정된 근무 일정은 변경할 수 없습니다." }
        return this.copy(workTime = newWorkTime)
    }

    /**
     * 근무 날짜 변경
     */
    fun changeWorkDate(context: DomainContext, newWorkDate: WorkDate): WorkSchedule {
        require(!isConfirmed) { "확정된 근무 일정은 변경할 수 없습니다." }
        return this.copy(workDate = newWorkDate)
    }

    /**
     * 근무 시간 계산
     */
    fun calculateWorkHours(): Double = workTime.calculateWorkHours()

    /**
     * 주말 근무 여부 확인
     */
    fun isWeekendWork(): Boolean = workDate.isWeekend()
}
