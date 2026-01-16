package com.lms.infrastructure.persistence.mapper

import com.lms.domain.model.attendance.*
import com.lms.domain.model.employee.EmployeeId
import com.lms.domain.model.schedule.WorkScheduleId
import com.lms.infrastructure.persistence.entity.AttendanceRecordEntity

/**
 * AttendanceRecord Domain ↔ Entity Mapper
 */
object AttendanceRecordMapper {
    /**
     * Entity → Domain 변환
     */
    fun toDomain(entity: AttendanceRecordEntity): AttendanceRecord {
        return AttendanceRecord.reconstruct(
            id = AttendanceRecordId.from(entity.id),
            employeeId = EmployeeId.from(entity.employeeId),
            workScheduleId = entity.workScheduleId?.let { WorkScheduleId.from(it) },
            attendanceDate = entity.attendanceDate,
            attendanceTime = AttendanceTime(
                checkInTime = entity.checkInTime,
                checkOutTime = entity.checkOutTime
            ),
            status = AttendanceStatus.valueOf(entity.status),
            note = entity.note,
            createdAt = entity.createdAt
        )
    }

    /**
     * Domain → Entity 변환
     */
    fun toEntity(domain: AttendanceRecord): AttendanceRecordEntity {
        return AttendanceRecordEntity(
            id = domain.id.value,
            employeeId = domain.employeeId.value,
            workScheduleId = domain.workScheduleId?.value,
            attendanceDate = domain.attendanceDate,
            checkInTime = domain.attendanceTime.checkInTime,
            checkOutTime = domain.attendanceTime.checkOutTime,
            status = domain.status.name,
            note = domain.note
        )
    }

    /**
     * Domain 변경사항을 Entity에 반영
     */
    fun updateEntity(entity: AttendanceRecordEntity, domain: AttendanceRecord) {
        entity.employeeId = domain.employeeId.value
        entity.workScheduleId = domain.workScheduleId?.value
        entity.attendanceDate = domain.attendanceDate
        entity.checkInTime = domain.attendanceTime.checkInTime
        entity.checkOutTime = domain.attendanceTime.checkOutTime
        entity.status = domain.status.name
        entity.note = domain.note
    }
}
