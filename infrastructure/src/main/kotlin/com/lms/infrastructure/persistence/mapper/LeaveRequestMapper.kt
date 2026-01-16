package com.lms.infrastructure.persistence.mapper

import com.lms.domain.model.employee.EmployeeId
import com.lms.domain.model.leave.*
import com.lms.domain.model.user.UserId
import com.lms.infrastructure.persistence.entity.LeaveRequestEntity

/**
 * LeaveRequest Domain ↔ Entity Mapper
 */
object LeaveRequestMapper {
    /**
     * Entity → Domain 변환
     */
    fun toDomain(entity: LeaveRequestEntity): LeaveRequest {
        return LeaveRequest.reconstruct(
            id = LeaveRequestId.from(entity.id),
            employeeId = EmployeeId.from(entity.employeeId),
            leaveType = LeaveType.valueOf(entity.leaveType),
            leavePeriod = LeavePeriod(
                startDate = entity.startDate,
                endDate = entity.endDate
            ),
            status = LeaveStatus.valueOf(entity.status),
            reason = entity.reason,
            approvedBy = entity.approvedBy?.let { UserId.from(it) },
            approvedAt = entity.approvedAt,
            rejectionReason = entity.rejectionReason,
            createdAt = entity.createdAt
        )
    }

    /**
     * Domain → Entity 변환
     */
    fun toEntity(domain: LeaveRequest): LeaveRequestEntity {
        return LeaveRequestEntity(
            id = domain.id.value,
            employeeId = domain.employeeId.value,
            leaveType = domain.leaveType.name,
            startDate = domain.leavePeriod.startDate,
            endDate = domain.leavePeriod.endDate,
            status = domain.status.name,
            reason = domain.reason,
            approvedBy = domain.approvedBy?.value,
            approvedAt = domain.approvedAt,
            rejectionReason = domain.rejectionReason
        )
    }

    /**
     * Domain 변경사항을 Entity에 반영
     */
    fun updateEntity(entity: LeaveRequestEntity, domain: LeaveRequest) {
        entity.employeeId = domain.employeeId.value
        entity.leaveType = domain.leaveType.name
        entity.startDate = domain.leavePeriod.startDate
        entity.endDate = domain.leavePeriod.endDate
        entity.status = domain.status.name
        entity.reason = domain.reason
        entity.approvedBy = domain.approvedBy?.value
        entity.approvedAt = domain.approvedAt
        entity.rejectionReason = domain.rejectionReason
    }
}
