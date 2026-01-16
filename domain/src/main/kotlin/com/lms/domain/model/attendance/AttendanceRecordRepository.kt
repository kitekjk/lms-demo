package com.lms.domain.model.attendance

import com.lms.domain.model.employee.EmployeeId
import java.time.LocalDate

/**
 * AttendanceRecord Repository Interface
 * 구현체는 infrastructure 모듈에 위치
 */
interface AttendanceRecordRepository {
    fun save(attendanceRecord: AttendanceRecord): AttendanceRecord
    fun findById(id: AttendanceRecordId): AttendanceRecord?
    fun findByEmployeeId(employeeId: EmployeeId): List<AttendanceRecord>
    fun findByEmployeeIdAndDate(employeeId: EmployeeId, date: LocalDate): AttendanceRecord?
    fun findByEmployeeIdAndDateRange(
        employeeId: EmployeeId,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AttendanceRecord>
    fun findPendingByEmployeeId(employeeId: EmployeeId): List<AttendanceRecord>
    fun delete(id: AttendanceRecordId)
}
