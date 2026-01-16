package com.lms.infrastructure.persistence.mapper

import com.lms.domain.model.employee.*
import com.lms.domain.model.store.StoreId
import com.lms.domain.model.user.UserId
import com.lms.infrastructure.persistence.entity.EmployeeEntity

/**
 * Employee Domain ↔ Entity Mapper
 */
object EmployeeMapper {
    /**
     * Entity → Domain 변환
     */
    fun toDomain(entity: EmployeeEntity): Employee = Employee.reconstruct(
        id = EmployeeId(entity.id),
        userId = UserId(entity.userId),
        name = EmployeeName(entity.name),
        employeeType = entity.employeeType,
        storeId = entity.storeId?.let { StoreId(it) },
        remainingLeave = RemainingLeave(entity.remainingLeave),
        isActive = entity.isActive,
        createdAt = entity.createdAt
    )

    /**
     * Domain → Entity 변환
     * Note: createdAt은 JPA Auditing에 의해 자동 설정됨
     */
    fun toEntity(domain: Employee): EmployeeEntity = EmployeeEntity(
        id = domain.id.value,
        userId = domain.userId.value,
        name = domain.name.value,
        employeeType = domain.employeeType,
        storeId = domain.storeId?.value,
        remainingLeave = domain.remainingLeave.value,
        isActive = domain.isActive
    )

    /**
     * Domain 변경사항을 Entity에 반영
     */
    fun updateEntity(entity: EmployeeEntity, domain: Employee) {
        entity.userId = domain.userId.value
        entity.name = domain.name.value
        entity.employeeType = domain.employeeType
        entity.storeId = domain.storeId?.value
        entity.remainingLeave = domain.remainingLeave.value
        entity.isActive = domain.isActive
    }
}
