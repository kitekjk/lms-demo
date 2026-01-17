package com.lms.application.schedule

import com.lms.application.schedule.dto.UpdateWorkScheduleCommand
import com.lms.application.schedule.dto.WorkScheduleResult
import com.lms.domain.common.DomainContext
import com.lms.domain.exception.WorkScheduleNotFoundException
import com.lms.domain.model.schedule.WorkDate
import com.lms.domain.model.schedule.WorkScheduleId
import com.lms.domain.model.schedule.WorkScheduleRepository
import com.lms.domain.model.schedule.WorkTime
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 근무 일정 수정 UseCase
 */
@Service
@Transactional
class UpdateWorkScheduleAppService(private val workScheduleRepository: WorkScheduleRepository) {
    fun execute(context: DomainContext, scheduleId: String, command: UpdateWorkScheduleCommand): WorkScheduleResult {
        // 1. 근무 일정 조회
        var schedule = workScheduleRepository.findById(WorkScheduleId.from(scheduleId))
            ?: throw WorkScheduleNotFoundException(scheduleId)

        // 2. 날짜 변경 (제공된 경우)
        if (command.workDate != null) {
            schedule = schedule.changeWorkDate(context, WorkDate(command.workDate))
        }

        // 3. 시간 변경 (제공된 경우)
        if (command.startTime != null && command.endTime != null) {
            schedule = schedule.changeWorkTime(
                context,
                WorkTime(
                    startTime = command.startTime,
                    endTime = command.endTime
                )
            )
        }

        // 4. 저장
        val savedSchedule = workScheduleRepository.save(schedule)

        return WorkScheduleResult.from(savedSchedule)
    }
}
