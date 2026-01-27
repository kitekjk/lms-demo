package com.lms.infrastructure.config

import com.lms.infrastructure.persistence.entity.WorkScheduleEntity
import com.lms.infrastructure.persistence.repository.WorkScheduleJpaRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 시연용 데이터 초기화
 * local 프로파일에서만 실행되며, 서버 시작 시 강남점 근무 일정을 동적으로 생성합니다.
 */
@Component
@Profile("local")
class DemoDataInitializer(
    private val workScheduleJpaRepository: WorkScheduleJpaRepository
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    // 강남점 직원 ID 목록
    private val gangnamEmployees = listOf(
        "emp-manager-001", // 박수진 (매니저)
        "emp-001",         // 김민수
        "emp-002"          // 이지영
    )

    private val gangnamStoreId = "store-001"

    @Transactional
    override fun run(args: ApplicationArguments?) {
        log.info("=== 시연용 근무 일정 데이터 초기화 시작 ===")

        val today = LocalDate.now()
        val startDate = today.minusDays(7)  // 1주 전부터
        val endDate = today.plusDays(14)    // 2주 후까지

        var createdCount = 0
        var skippedCount = 0

        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            // 주말 제외 (월~금만)
            if (currentDate.dayOfWeek != DayOfWeek.SATURDAY &&
                currentDate.dayOfWeek != DayOfWeek.SUNDAY
            ) {
                for (employeeId in gangnamEmployees) {
                    // 이미 존재하는 일정은 건너뛰기
                    val existing = workScheduleJpaRepository
                        .findByEmployeeIdAndWorkDate(employeeId, currentDate)

                    if (existing == null) {
                        val schedule = WorkScheduleEntity(
                            id = UUID.randomUUID().toString(),
                            employeeId = employeeId,
                            storeId = gangnamStoreId,
                            workDate = currentDate,
                            startTime = LocalTime.of(9, 0),   // 오전 9시
                            endTime = LocalTime.of(18, 0),    // 오후 6시
                            isConfirmed = true
                        )
                        workScheduleJpaRepository.save(schedule)
                        createdCount++
                    } else {
                        skippedCount++
                    }
                }
            }
            currentDate = currentDate.plusDays(1)
        }

        log.info("=== 시연용 근무 일정 데이터 초기화 완료 ===")
        log.info("기간: {} ~ {}", startDate, endDate)
        log.info("생성된 일정: {}건, 건너뛴 일정: {}건", createdCount, skippedCount)
    }
}
