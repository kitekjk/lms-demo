package com.lms.application.payroll

import com.lms.application.payroll.dto.PayrollBatchHistoryResult
import com.lms.domain.model.payroll.PayrollBatchHistoryRepository
import java.time.Instant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 급여 배치 이력 조회 UseCase
 */
@Service
@Transactional(readOnly = true)
class GetPayrollBatchHistoriesAppService(private val batchHistoryRepository: PayrollBatchHistoryRepository) {
    fun execute(startDate: Instant?, endDate: Instant?): List<PayrollBatchHistoryResult> =
        batchHistoryRepository.findAll(startDate, endDate)
            .map { PayrollBatchHistoryResult.from(it) }
}
