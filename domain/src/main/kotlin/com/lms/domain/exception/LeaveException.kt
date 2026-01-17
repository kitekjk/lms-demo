package com.lms.domain.exception

/**
 * 휴가를 찾을 수 없음
 */
class LeaveRequestNotFoundException(leaveRequestId: String, cause: Throwable? = null) :
    DomainException("LEAVE001", "휴가 신청을 찾을 수 없습니다: $leaveRequestId", cause)

/**
 * 잔여 연차 부족
 */
class InsufficientLeaveBalanceException(requestedDays: Double, remainingDays: Double, cause: Throwable? = null) :
    DomainException(
        "LEAVE002",
        "잔여 연차가 부족합니다. 신청: ${requestedDays}일, 잔여: ${remainingDays}일",
        cause
    )

/**
 * 휴가 날짜 중복
 */
class LeaveRequestDateOverlapException(
    employeeId: String,
    startDate: String,
    endDate: String,
    cause: Throwable? = null
) : DomainException(
    "LEAVE003",
    "이미 승인된 휴가와 기간이 겹칩니다. 근로자: $employeeId, 기간: $startDate ~ $endDate",
    cause
)

/**
 * 취소 불가능한 상태
 */
class LeaveRequestCannotBeCancelledException(leaveRequestId: String, currentStatus: String, cause: Throwable? = null) :
    DomainException(
        "LEAVE004",
        "현재 상태에서는 휴가 신청을 취소할 수 없습니다. 휴가 ID: $leaveRequestId, 상태: $currentStatus",
        cause
    )

/**
 * 승인/반려 불가능한 상태
 */
class LeaveRequestCannotBeProcessedException(leaveRequestId: String, currentStatus: String, cause: Throwable? = null) :
    DomainException(
        "LEAVE005",
        "현재 상태에서는 휴가 신청을 승인/반려할 수 없습니다. 휴가 ID: $leaveRequestId, 상태: $currentStatus",
        cause
    )

/**
 * 과거 날짜 신청 불가
 */
class PastDateLeaveRequestException(requestDate: String, cause: Throwable? = null) :
    DomainException("LEAVE006", "과거 날짜로 휴가를 신청할 수 없습니다: $requestDate", cause)

/**
 * 유효하지 않은 날짜 범위
 */
class InvalidLeaveDateRangeException(startDate: String, endDate: String, cause: Throwable? = null) :
    DomainException(
        "LEAVE007",
        "유효하지 않은 휴가 기간입니다. 시작일: $startDate, 종료일: $endDate",
        cause
    )
