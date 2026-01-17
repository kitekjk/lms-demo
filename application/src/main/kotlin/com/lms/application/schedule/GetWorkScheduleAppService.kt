package com.lms.application.schedule

import com.lms.application.schedule.dto.WorkScheduleResult
import com.lms.domain.model.schedule.WorkScheduleId
import com.lms.domain.model.schedule.WorkScheduleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 근무 일정 단건 조회 UseCase
 */
@Service
@Transactional(readOnly = true)
class GetWorkScheduleAppService(private val workScheduleRepository: WorkScheduleRepository) {
    fun execute(scheduleId: String): WorkScheduleResult? {
        val schedule = workScheduleRepository.findById(WorkScheduleId.from(scheduleId))
            ?: return null

        return WorkScheduleResult.from(schedule)
    }
}
