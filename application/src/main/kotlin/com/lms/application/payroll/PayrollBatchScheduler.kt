package com.lms.application.payroll

import com.lms.application.payroll.dto.ExecutePayrollBatchCommand
import com.lms.domain.common.DomainContext
import java.time.Instant
import java.time.YearMonth
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * 급여 자동 산정 배치 스케줄러
 * 매월 말일 01:00에 전체 직원 급여 자동 산정
 */
@Component
class PayrollBatchScheduler(private val executePayrollBatchAppService: ExecutePayrollBatchAppService) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 매월 말일 01:00 자동 실행
     * Cron: "초 분 시 일 월 요일"
     * L = Last day of month
     */
    @Scheduled(cron = "0 0 1 L * ?")
    fun executeMonthlyPayrollCalculation() {
        logger.info("==================================================")
        logger.info("급여 자동 산정 배치 시작")
        logger.info("==================================================")

        try {
            // 이전 달 급여 계산 (매월 말일에 이전 달 급여 산정)
            val previousMonth = YearMonth.now().minusMonths(1)

            val context = createSystemContext()
            val command = ExecutePayrollBatchCommand(
                period = previousMonth,
                storeId = null // 전체 매장
            )

            val result = executePayrollBatchAppService.execute(context, command)

            logger.info("==================================================")
            logger.info("급여 자동 산정 배치 완료")
            logger.info("기간: ${previousMonth.year}-${previousMonth.monthValue}")
            logger.info("총 대상: ${result.totalCount}명")
            logger.info("성공: ${result.successCount}명")
            logger.info("실패: ${result.failureCount}명")
            logger.info("==================================================")
        } catch (e: Exception) {
            logger.error("급여 자동 산정 배치 실행 중 오류 발생", e)
        }
    }

    /**
     * 시스템 컨텍스트 생성
     */
    private fun createSystemContext(): DomainContext = object : DomainContext {
        override val serviceName: String = "payroll-batch-scheduler"
        override val userId: String = "SYSTEM"
        override val userName: String = "시스템"
        override val roleId: String = "SYSTEM"
        override val requestId: UUID = UUID.randomUUID()
        override val requestedAt: Instant = Instant.now()
        override val clientIp: String = "127.0.0.1"
    }
}
