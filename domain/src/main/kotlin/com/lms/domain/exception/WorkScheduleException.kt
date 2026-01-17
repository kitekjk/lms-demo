package com.lms.domain.exception

/**
 * 근무 일정을 찾을 수 없을 때 발생하는 예외
 */
class WorkScheduleNotFoundException(scheduleId: String, cause: Throwable? = null) :
    DomainException(ErrorCode.SCHEDULE_NOT_FOUND, "근무 일정을 찾을 수 없습니다: $scheduleId", cause)

/**
 * 동일한 근로자와 날짜에 대해 중복된 근무 일정이 존재할 때 발생하는 예외
 */
class DuplicateWorkScheduleException(employeeId: String, date: String, cause: Throwable? = null) :
    DomainException(
        ErrorCode.DUPLICATE_SCHEDULE,
        "이미 해당 날짜에 근무 일정이 존재합니다. 근로자: $employeeId, 날짜: $date",
        cause
    )

/**
 * 확정된 근무 일정을 수정하려고 시도할 때 발생하는 예외
 */
class ConfirmedScheduleCannotBeModifiedException(scheduleId: String, cause: Throwable? = null) :
    DomainException(
        ErrorCode.CONFIRMED_SCHEDULE_CANNOT_BE_MODIFIED,
        "확정된 근무 일정은 수정할 수 없습니다: $scheduleId",
        cause
    )

/**
 * 근로자가 매장에 속하지 않을 때 발생하는 예외
 */
class EmployeeNotBelongToStoreException(employeeId: String, storeId: String, cause: Throwable? = null) :
    DomainException(
        ErrorCode.EMPLOYEE_NOT_BELONG_TO_STORE,
        "근로자가 해당 매장에 속하지 않습니다. 근로자: $employeeId, 매장: $storeId",
        cause
    )

/**
 * 관리자가 자신이 관리하지 않는 매장의 일정을 관리하려고 시도할 때 발생하는 예외
 */
class ManagerCanOnlyManageOwnStoreSchedulesException(managerId: String, storeId: String, cause: Throwable? = null) :
    DomainException(
        ErrorCode.MANAGER_CAN_ONLY_MANAGE_OWN_STORE,
        "관리자는 자신의 매장 일정만 관리할 수 있습니다. 관리자: $managerId, 매장: $storeId",
        cause
    )
