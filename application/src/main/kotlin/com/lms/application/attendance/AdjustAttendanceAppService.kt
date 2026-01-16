package com.lms.application.attendance

import com.lms.application.attendance.dto.AdjustAttendanceCommand
import com.lms.application.attendance.dto.AttendanceRecordResult
import com.lms.domain.common.DomainContext
import com.lms.domain.exception.AttendanceNotFoundException
import com.lms.domain.model.attendance.AttendanceRecordId
import com.lms.domain.model.attendance.AttendanceRecordRepository
import com.lms.domain.model.attendance.AttendanceTime
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 출퇴근 기록 수정 UseCase (관리자용)
 */
@Service
@Transactional
class AdjustAttendanceAppService(private val attendanceRecordRepository: AttendanceRecordRepository) {
    fun execute(context: DomainContext, recordId: String, command: AdjustAttendanceCommand): AttendanceRecordResult {
        // 출퇴근 기록 조회
        val record = attendanceRecordRepository.findById(AttendanceRecordId(recordId))
            ?: throw AttendanceNotFoundException(recordId)

        // 수정된 시간으로 AttendanceTime 생성
        val adjustedTime = AttendanceTime(
            checkInTime = command.adjustedCheckInTime,
            checkOutTime = command.adjustedCheckOutTime
        )

        // 기록 업데이트 (copy 사용)
        val updatedRecord = record.copy(
            attendanceTime = adjustedTime,
            note = command.reason // 수정 사유를 note에 저장
        )

        // 저장
        val savedRecord = attendanceRecordRepository.save(updatedRecord)

        // TODO: Task 5.5에서 AuditLog 생성 추가 예정

        return AttendanceRecordResult.from(savedRecord)
    }
}
