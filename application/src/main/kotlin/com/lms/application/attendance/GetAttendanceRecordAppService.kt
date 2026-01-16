package com.lms.application.attendance

import com.lms.application.attendance.dto.AttendanceRecordResult
import com.lms.domain.model.attendance.AttendanceRecordId
import com.lms.domain.model.attendance.AttendanceRecordRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 출퇴근 기록 단건 조회 UseCase
 */
@Service
@Transactional(readOnly = true)
class GetAttendanceRecordAppService(private val attendanceRecordRepository: AttendanceRecordRepository) {
    fun execute(recordId: String): AttendanceRecordResult? {
        val record = attendanceRecordRepository.findById(AttendanceRecordId(recordId))
        return record?.let { AttendanceRecordResult.from(it) }
    }
}
