package com.lms.domain.model.auditlog

/**
 * 감사로그 대상 엔티티 타입
 */
sealed class EntityType(val value: String) {
    data object AttendanceRecord : EntityType("ATTENDANCE_RECORD")
    data object Employee : EntityType("EMPLOYEE")
    data object Store : EntityType("STORE")
    data object User : EntityType("USER")
    data object WorkSchedule : EntityType("WORK_SCHEDULE")
    data object LeaveRequest : EntityType("LEAVE_REQUEST")
    data object Payroll : EntityType("PAYROLL")

    companion object {
        fun from(value: String): EntityType = when (value) {
            "ATTENDANCE_RECORD" -> AttendanceRecord
            "EMPLOYEE" -> Employee
            "STORE" -> Store
            "USER" -> User
            "WORK_SCHEDULE" -> WorkSchedule
            "LEAVE_REQUEST" -> LeaveRequest
            "PAYROLL" -> Payroll
            else -> throw IllegalArgumentException("Unknown EntityType: $value")
        }
    }
}
