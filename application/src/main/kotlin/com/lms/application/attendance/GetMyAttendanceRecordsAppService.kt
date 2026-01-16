package com.lms.application.attendance

import com.lms.application.attendance.dto.AttendanceRecordResult
import com.lms.domain.model.attendance.AttendanceRecordRepository
import com.lms.domain.model.employee.EmployeeId
import java.time.LocalDate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 본인 출퇴근 기록 조회 UseCase
 */
@Service
@Transactional(readOnly = true)
class GetMyAttendanceRecordsAppService(private val attendanceRecordRepository: AttendanceRecordRepository) {
    fun execute(employeeId: String, startDate: LocalDate?, endDate: LocalDate?): List<AttendanceRecordResult> {
        val empId = EmployeeId(employeeId)

        val records = if (startDate != null && endDate != null) {
            attendanceRecordRepository.findByEmployeeIdAndDateRange(empId, startDate, endDate)
        } else {
            attendanceRecordRepository.findByEmployeeId(empId)
        }

        return records.map { AttendanceRecordResult.from(it) }
    }
}
