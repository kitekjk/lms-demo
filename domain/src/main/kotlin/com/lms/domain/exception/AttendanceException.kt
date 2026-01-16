package com.lms.domain.exception

/**
 * 출퇴근 기록을 찾을 수 없을 때 발생하는 예외
 */
class AttendanceNotFoundException(attendanceId: String, cause: Throwable? = null) :
    DomainException(ErrorCode.ATTENDANCE_NOT_FOUND, "출퇴근 기록을 찾을 수 없습니다: $attendanceId", cause)

/**
 * 이미 출근한 상태에서 재출근 시도 시 발생하는 예외
 */
class AlreadyCheckedInException(employeeId: String, date: String, cause: Throwable? = null) :
    DomainException(
        ErrorCode.ALREADY_CHECKED_IN,
        "이미 출근 처리되었습니다. 근로자: $employeeId, 날짜: $date",
        cause
    )

/**
 * 출근하지 않은 상태에서 퇴근 시도 시 발생하는 예외
 */
class NotCheckedInException(employeeId: String, date: String, cause: Throwable? = null) :
    DomainException(
        ErrorCode.NOT_CHECKED_IN,
        "출근 기록이 없습니다. 근로자: $employeeId, 날짜: $date",
        cause
    )

/**
 * 이미 퇴근한 상태에서 재퇴근 시도 시 발생하는 예외
 */
class AlreadyCheckedOutException(employeeId: String, date: String, cause: Throwable? = null) :
    DomainException(
        ErrorCode.ALREADY_CHECKED_OUT,
        "이미 퇴근 처리되었습니다. 근로자: $employeeId, 날짜: $date",
        cause
    )
