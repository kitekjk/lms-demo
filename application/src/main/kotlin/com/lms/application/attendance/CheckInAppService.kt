package com.lms.application.attendance

import com.lms.application.attendance.dto.AttendanceRecordResult
import com.lms.application.attendance.dto.CheckInCommand
import com.lms.domain.common.DomainContext
import com.lms.domain.exception.AlreadyCheckedInException
import com.lms.domain.model.attendance.AttendanceRecord
import com.lms.domain.model.attendance.AttendanceRecordRepository
import com.lms.domain.model.employee.EmployeeId
import com.lms.domain.model.schedule.WorkScheduleId
import java.time.LocalDate
import java.time.ZoneId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 출근 체크 UseCase
 */
@Service
@Transactional
class CheckInAppService(private val attendanceRecordRepository: AttendanceRecordRepository) {
    fun execute(context: DomainContext, command: CheckInCommand): AttendanceRecordResult {
        val employeeId = EmployeeId(command.employeeId)
        val today = LocalDate.now(ZoneId.systemDefault())

        // 중복 출근 체크
        attendanceRecordRepository.findByEmployeeIdAndDate(employeeId, today)?.let {
            throw AlreadyCheckedInException(command.employeeId, today.toString())
        }

        // 출근 기록 생성
        val attendanceRecord = AttendanceRecord.checkIn(
            context = context,
            employeeId = employeeId,
            workScheduleId = command.workScheduleId?.let { WorkScheduleId(it) },
            checkInTime = context.requestedAt
        )

        // 저장
        val savedRecord = attendanceRecordRepository.save(attendanceRecord)

        return AttendanceRecordResult.from(savedRecord)
    }
}
