package com.lms.domain.model.schedule

import com.lms.domain.model.employee.EmployeeId
import com.lms.domain.model.store.StoreId
import java.time.LocalDate

/**
 * WorkSchedule Repository Interface
 * 구현체는 infrastructure 모듈에 위치
 */
interface WorkScheduleRepository {
    fun save(workSchedule: WorkSchedule): WorkSchedule
    fun findById(id: WorkScheduleId): WorkSchedule?
    fun findByEmployeeId(employeeId: EmployeeId): List<WorkSchedule>
    fun findByStoreId(storeId: StoreId): List<WorkSchedule>
    fun findByEmployeeIdAndDateRange(
        employeeId: EmployeeId,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<WorkSchedule>
    fun findByStoreIdAndDateRange(storeId: StoreId, startDate: LocalDate, endDate: LocalDate): List<WorkSchedule>
    fun delete(id: WorkScheduleId)
}
