package com.lms.application.schedule

import com.lms.application.schedule.dto.WorkScheduleResult
import com.lms.domain.model.employee.EmployeeId
import com.lms.domain.model.schedule.WorkScheduleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 근로자별 근무 일정 조회 UseCase
 */
@Service
@Transactional(readOnly = true)
class GetWorkSchedulesByEmployeeAppService(private val workScheduleRepository: WorkScheduleRepository) {
    fun execute(employeeId: String): List<WorkScheduleResult> {
        val schedules = workScheduleRepository.findByEmployeeId(EmployeeId.from(employeeId))
        return schedules.map { WorkScheduleResult.from(it) }
    }
}
