package com.lms.infrastructure.persistence.mapper

import com.lms.domain.model.employee.EmployeeId
import com.lms.domain.model.schedule.*
import com.lms.domain.model.store.StoreId
import com.lms.infrastructure.persistence.entity.WorkScheduleEntity
import java.time.LocalDate

/**
 * WorkSchedule Domain ↔ Entity Mapper
 */
object WorkScheduleMapper {
    /**
     * Entity → Domain 변환
     */
    fun toDomain(entity: WorkScheduleEntity): WorkSchedule {
        return WorkSchedule.reconstruct(
            id = WorkScheduleId.from(entity.id),
            employeeId = EmployeeId.from(entity.employeeId),
            storeId = StoreId.from(entity.storeId),
            workDate = WorkDate(entity.workDate),
            workTime = WorkTime(
                startTime = entity.startTime,
                endTime = entity.endTime
            ),
            isConfirmed = entity.isConfirmed,
            createdAt = entity.createdAt
        )
    }

    /**
     * Domain → Entity 변환
     */
    fun toEntity(domain: WorkSchedule): WorkScheduleEntity {
        return WorkScheduleEntity(
            id = domain.id.value,
            employeeId = domain.employeeId.value,
            storeId = domain.storeId.value,
            workDate = domain.workDate.value,
            startTime = domain.workTime.startTime,
            endTime = domain.workTime.endTime,
            isConfirmed = domain.isConfirmed
        )
    }

    /**
     * Domain 변경사항을 Entity에 반영
     */
    fun updateEntity(entity: WorkScheduleEntity, domain: WorkSchedule) {
        entity.employeeId = domain.employeeId.value
        entity.storeId = domain.storeId.value
        entity.workDate = domain.workDate.value
        entity.startTime = domain.workTime.startTime
        entity.endTime = domain.workTime.endTime
        entity.isConfirmed = domain.isConfirmed
    }
}
