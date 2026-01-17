package com.lms.application.schedule

import com.lms.application.schedule.dto.WorkScheduleResult
import com.lms.domain.model.schedule.WorkScheduleRepository
import com.lms.domain.model.store.StoreId
import java.time.LocalDate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 날짜 범위별 근무 일정 조회 UseCase
 */
@Service
@Transactional(readOnly = true)
class GetWorkSchedulesByDateRangeAppService(private val workScheduleRepository: WorkScheduleRepository) {
    fun execute(storeId: String, startDate: LocalDate, endDate: LocalDate): List<WorkScheduleResult> {
        val schedules = workScheduleRepository.findByStoreIdAndDateRange(
            StoreId.from(storeId),
            startDate,
            endDate
        )
        return schedules.map { WorkScheduleResult.from(it) }
    }
}
