package com.lms.application.schedule

import com.lms.domain.exception.WorkScheduleNotFoundException
import com.lms.domain.model.schedule.WorkScheduleId
import com.lms.domain.model.schedule.WorkScheduleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 근무 일정 삭제 UseCase
 */
@Service
@Transactional
class DeleteWorkScheduleAppService(private val workScheduleRepository: WorkScheduleRepository) {
    fun execute(scheduleId: String) {
        // 1. 근무 일정 존재 확인
        workScheduleRepository.findById(WorkScheduleId.from(scheduleId))
            ?: throw WorkScheduleNotFoundException(scheduleId)

        // 2. 삭제
        workScheduleRepository.delete(WorkScheduleId.from(scheduleId))
    }
}
