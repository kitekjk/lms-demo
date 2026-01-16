package com.lms.application.attendance

import com.lms.application.attendance.dto.AdjustAttendanceCommand
import com.lms.application.attendance.dto.AttendanceRecordResult
import com.lms.application.auditlog.CreateAuditLogAppService
import com.lms.application.auditlog.dto.CreateAuditLogCommand
import com.lms.domain.common.DomainContext
import com.lms.domain.exception.AttendanceNotFoundException
import com.lms.domain.model.attendance.AttendanceRecordId
import com.lms.domain.model.attendance.AttendanceRecordRepository
import com.lms.domain.model.attendance.AttendanceTime
import com.lms.domain.model.auditlog.ActionType
import com.lms.domain.model.auditlog.EntityType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 출퇴근 기록 수정 UseCase (관리자용)
 */
@Service
@Transactional
class AdjustAttendanceAppService(
    private val attendanceRecordRepository: AttendanceRecordRepository,
    private val createAuditLogAppService: CreateAuditLogAppService
) {
    fun execute(context: DomainContext, recordId: String, command: AdjustAttendanceCommand): AttendanceRecordResult {
        // 1. 출퇴근 기록 조회
        val record = attendanceRecordRepository.findById(AttendanceRecordId(recordId))
            ?: throw AttendanceNotFoundException(recordId)

        // 2. 변경 전 값 저장 (AuditLog용)
        val oldValue = buildAttendanceJson(
            checkInTime = record.attendanceTime.checkInTime.toString(),
            checkOutTime = record.attendanceTime.checkOutTime?.toString(),
            note = record.note
        )

        // 3. 수정된 시간으로 AttendanceTime 생성
        val adjustedTime = AttendanceTime(
            checkInTime = command.adjustedCheckInTime,
            checkOutTime = command.adjustedCheckOutTime
        )

        // 4. 기록 업데이트 (copy 사용)
        val updatedRecord = record.copy(
            attendanceTime = adjustedTime,
            note = command.reason // 수정 사유를 note에 저장
        )

        // 5. 저장
        val savedRecord = attendanceRecordRepository.save(updatedRecord)

        // 6. 변경 후 값 저장 (AuditLog용)
        val newValue = buildAttendanceJson(
            checkInTime = savedRecord.attendanceTime.checkInTime.toString(),
            checkOutTime = savedRecord.attendanceTime.checkOutTime?.toString(),
            note = savedRecord.note
        )

        // 7. AuditLog 생성
        createAuditLogAppService.execute(
            context = context,
            command = CreateAuditLogCommand(
                entityType = EntityType.AttendanceRecord,
                entityId = recordId,
                actionType = ActionType.Update,
                oldValue = oldValue,
                newValue = newValue,
                reason = command.reason
            )
        )

        return AttendanceRecordResult.from(savedRecord)
    }

    /**
     * 출퇴근 기록을 JSON 형태의 문자열로 변환
     */
    private fun buildAttendanceJson(checkInTime: String, checkOutTime: String?, note: String?): String {
        val parts = mutableListOf<String>()
        parts.add("\"checkInTime\":\"$checkInTime\"")
        checkOutTime?.let { parts.add("\"checkOutTime\":\"$it\"") }
        note?.let { parts.add("\"note\":\"${it.replace("\"", "\\\"")}\"") }
        return "{${parts.joinToString(",")}}"
    }
}
