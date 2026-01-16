package com.lms.application.attendance

import com.lms.application.attendance.dto.AttendanceRecordResult
import com.lms.domain.model.attendance.AttendanceRecordRepository
import com.lms.domain.model.employee.EmployeeRepository
import com.lms.domain.model.store.StoreId
import java.time.LocalDate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 매장별 출퇴근 기록 조회 UseCase (관리자용)
 */
@Service
@Transactional(readOnly = true)
class GetAttendanceRecordsByStoreAppService(
    private val employeeRepository: EmployeeRepository,
    private val attendanceRecordRepository: AttendanceRecordRepository
) {
    fun execute(storeId: String, startDate: LocalDate?, endDate: LocalDate?): List<AttendanceRecordResult> {
        // 해당 매장의 근로자 목록 조회
        val employees = employeeRepository.findByStoreId(StoreId(storeId))

        // 각 근로자의 출퇴근 기록 조회
        val allRecords = employees.flatMap { employee ->
            if (startDate != null && endDate != null) {
                attendanceRecordRepository.findByEmployeeIdAndDateRange(
                    employee.id,
                    startDate,
                    endDate
                )
            } else {
                attendanceRecordRepository.findByEmployeeId(employee.id)
            }
        }

        return allRecords.map { AttendanceRecordResult.from(it) }
            .sortedByDescending { it.attendanceDate }
    }
}
