package com.lms.application.payroll

import com.lms.application.payroll.dto.ExecutePayrollBatchCommand
import com.lms.application.payroll.dto.PayrollBatchHistoryResult
import com.lms.domain.common.DomainContext
import com.lms.domain.exception.NoEmployeesFoundException
import com.lms.domain.model.employee.EmployeeRepository
import com.lms.domain.model.payroll.PayrollBatchHistory
import com.lms.domain.model.payroll.PayrollBatchHistoryRepository
import com.lms.domain.model.payroll.PayrollPeriod
import com.lms.domain.model.store.StoreId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 급여 배치 실행 UseCase
 * 특정 기간과 매장(선택적)에 대해 급여를 일괄 산정
 */
@Service
@Transactional
class ExecutePayrollBatchAppService(
    private val employeeRepository: EmployeeRepository,
    private val batchHistoryRepository: PayrollBatchHistoryRepository,
    private val calculatePayrollAppService: CalculatePayrollAppService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun execute(context: DomainContext, command: ExecutePayrollBatchCommand): PayrollBatchHistoryResult {
        val period = PayrollPeriod.from(command.period)
        val storeId = command.storeId?.let { StoreId.from(it) }

        // 1. 대상 직원 조회
        val employees = if (storeId != null) {
            employeeRepository.findByStoreIdAndActive(storeId, true)
        } else {
            employeeRepository.findByActive(true)
        }

        if (employees.isEmpty()) {
            throw NoEmployeesFoundException(storeId?.value ?: "전체")
        }

        // 2. 배치 이력 시작
        var batchHistory = PayrollBatchHistory.start(
            context = context,
            period = period,
            storeId = storeId,
            totalCount = employees.size
        )
        batchHistory = batchHistoryRepository.save(batchHistory)

        // 3. 각 직원별 급여 계산
        var successCount = 0
        var failureCount = 0
        val errors = mutableListOf<String>()

        employees.forEach { employee ->
            try {
                // FIXME: hourlyRate는 Employee 또는 별도 테이블에서 가져와야 함
                // 현재는 임시로 10,000원 고정
                val calculateCommand = com.lms.application.payroll.dto.CalculatePayrollCommand(
                    employeeId = employee.id.value,
                    period = command.period,
                    hourlyRate = java.math.BigDecimal("10000")
                )

                calculatePayrollAppService.execute(context, calculateCommand)
                successCount++

                logger.info("급여 산정 성공: employeeId=${employee.id.value}, period=${period.value}")
            } catch (e: Exception) {
                failureCount++
                val errorMsg = "employeeId=${employee.id.value}: ${e.message}"
                errors.add(errorMsg)

                logger.error("급여 산정 실패: $errorMsg", e)
            }
        }

        // 4. 배치 이력 완료 처리
        batchHistory = if (errors.isEmpty()) {
            batchHistory.complete(context, successCount, failureCount)
        } else {
            batchHistory.complete(context, successCount, failureCount)
        }

        val finalHistory = batchHistoryRepository.save(batchHistory)

        logger.info(
            "급여 배치 완료: period=${period.value}, total=${employees.size}, " +
                "success=$successCount, failure=$failureCount"
        )

        return PayrollBatchHistoryResult.from(finalHistory)
    }
}
