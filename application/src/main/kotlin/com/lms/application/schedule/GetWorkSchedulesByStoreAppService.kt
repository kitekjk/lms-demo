package com.lms.application.schedule

import com.lms.application.schedule.dto.WorkScheduleResult
import com.lms.domain.model.schedule.WorkScheduleRepository
import com.lms.domain.model.store.StoreId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 매장별 근무 일정 조회 UseCase
 */
@Service
@Transactional(readOnly = true)
class GetWorkSchedulesByStoreAppService(private val workScheduleRepository: WorkScheduleRepository) {
    fun execute(storeId: String): List<WorkScheduleResult> {
        val schedules = workScheduleRepository.findByStoreId(StoreId.from(storeId))
        return schedules.map { WorkScheduleResult.from(it) }
    }
}
