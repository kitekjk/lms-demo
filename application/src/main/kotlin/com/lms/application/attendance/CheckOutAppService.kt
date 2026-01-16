package com.lms.application.attendance

import com.lms.application.attendance.dto.AttendanceRecordResult
import com.lms.application.attendance.dto.CheckOutCommand
import com.lms.domain.common.DomainContext
import com.lms.domain.exception.AlreadyCheckedOutException
import com.lms.domain.exception.NotCheckedInException
import com.lms.domain.model.attendance.AttendanceRecordRepository
import com.lms.domain.model.employee.EmployeeId
import java.time.LocalDate
import java.time.ZoneId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 퇴근 체크 UseCase
 */
@Service
@Transactional
class CheckOutAppService(private val attendanceRecordRepository: AttendanceRecordRepository) {
    fun execute(context: DomainContext, command: CheckOutCommand): AttendanceRecordResult {
        val employeeId = EmployeeId(command.employeeId)
        val today = LocalDate.now(ZoneId.systemDefault())

        // 출근 기록 조회
        val attendanceRecord = attendanceRecordRepository.findByEmployeeIdAndDate(employeeId, today)
            ?: throw NotCheckedInException(command.employeeId, today.toString())

        // 이미 퇴근했는지 확인
        if (attendanceRecord.isCompleted()) {
            throw AlreadyCheckedOutException(command.employeeId, today.toString())
        }

        // 퇴근 처리
        val updatedRecord = attendanceRecord.checkOut(context, context.requestedAt)

        // 저장
        val savedRecord = attendanceRecordRepository.save(updatedRecord)

        return AttendanceRecordResult.from(savedRecord)
    }
}
