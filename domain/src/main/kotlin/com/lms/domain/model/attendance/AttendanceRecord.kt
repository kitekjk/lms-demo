package com.lms.domain.model.attendance

import com.lms.domain.common.DomainContext
import com.lms.domain.model.employee.EmployeeId
import com.lms.domain.model.schedule.WorkSchedule
import com.lms.domain.model.schedule.WorkScheduleId
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs

/**
 * 출퇴근 기록 Aggregate Root
 * 근로자의 실제 출퇴근 기록을 관리
 */
data class AttendanceRecord private constructor(
    val id: AttendanceRecordId,
    val employeeId: EmployeeId,
    val workScheduleId: WorkScheduleId?,
    val attendanceDate: LocalDate,
    val attendanceTime: AttendanceTime,
    val status: AttendanceStatus,
    val note: String?,
    val createdAt: Instant
) {
    companion object {
        /**
         * 출근 체크
         */
        fun checkIn(
            context: DomainContext,
            employeeId: EmployeeId,
            workScheduleId: WorkScheduleId?,
            checkInTime: Instant
        ): AttendanceRecord {
            val attendanceDate = checkInTime.atZone(ZoneId.systemDefault()).toLocalDate()

            return AttendanceRecord(
                id = AttendanceRecordId.generate(),
                employeeId = employeeId,
                workScheduleId = workScheduleId,
                attendanceDate = attendanceDate,
                attendanceTime = AttendanceTime(checkInTime, null),
                status = AttendanceStatus.PENDING,
                note = null,
                createdAt = context.requestedAt
            )
        }

        /**
         * 기존 출퇴근 기록 재구성 (Repository에서 조회 시)
         */
        fun reconstruct(
            id: AttendanceRecordId,
            employeeId: EmployeeId,
            workScheduleId: WorkScheduleId?,
            attendanceDate: LocalDate,
            attendanceTime: AttendanceTime,
            status: AttendanceStatus,
            note: String?,
            createdAt: Instant
        ): AttendanceRecord = AttendanceRecord(
            id, employeeId, workScheduleId, attendanceDate,
            attendanceTime, status, note, createdAt
        )

        /**
         * 지각 허용 시간 (분)
         */
        private const val LATE_TOLERANCE_MINUTES = 10L
    }

    /**
     * 퇴근 체크
     */
    fun checkOut(context: DomainContext, checkOutTime: Instant): AttendanceRecord {
        val newAttendanceTime = attendanceTime.checkOut(checkOutTime)
        return this.copy(
            attendanceTime = newAttendanceTime,
            status = AttendanceStatus.NORMAL // 기본값, 이후 평가 필요
        )
    }

    /**
     * 근무 일정과 비교하여 상태 평가
     */
    fun evaluateStatus(context: DomainContext, workSchedule: WorkSchedule): AttendanceRecord {
        require(attendanceTime.isCompleted()) {
            "퇴근 처리되지 않은 기록은 평가할 수 없습니다."
        }

        val scheduledStartTime = workSchedule.workTime.startTime
        val scheduledEndTime = workSchedule.workTime.endTime

        val actualCheckIn = attendanceTime.checkInTime
            .atZone(ZoneId.systemDefault())
            .toLocalTime()

        val actualCheckOut = attendanceTime.checkOutTime!!
            .atZone(ZoneId.systemDefault())
            .toLocalTime()

        val newStatus = when {
            // 지각 체크 (허용 시간 고려)
            actualCheckIn.isAfter(scheduledStartTime.plusMinutes(LATE_TOLERANCE_MINUTES))
                && actualCheckOut.isBefore(scheduledEndTime) -> AttendanceStatus.LATE

            // 조퇴 체크
            !actualCheckIn.isAfter(scheduledStartTime.plusMinutes(LATE_TOLERANCE_MINUTES))
                && actualCheckOut.isBefore(scheduledEndTime) -> AttendanceStatus.EARLY_LEAVE

            // 지각 + 조퇴
            actualCheckIn.isAfter(scheduledStartTime.plusMinutes(LATE_TOLERANCE_MINUTES))
                && actualCheckOut.isBefore(scheduledEndTime) -> AttendanceStatus.LATE

            // 정상
            else -> AttendanceStatus.NORMAL
        }

        return this.copy(status = newStatus)
    }

    /**
     * 메모 추가/수정
     */
    fun updateNote(context: DomainContext, note: String): AttendanceRecord {
        return this.copy(note = note)
    }

    /**
     * 결근 처리
     */
    fun markAsAbsent(context: DomainContext): AttendanceRecord {
        return this.copy(status = AttendanceStatus.ABSENT)
    }

    /**
     * 실제 근무 시간 계산
     */
    fun calculateActualWorkHours(): Double? = attendanceTime.calculateActualWorkHours()

    /**
     * 출퇴근 완료 여부
     */
    fun isCompleted(): Boolean = attendanceTime.isCompleted()
}
