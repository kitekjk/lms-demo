export type AttendanceStatusValue = 'NORMAL' | 'LATE' | 'EARLY_LEAVE' | 'ABSENT' | 'PENDING'

export interface AttendanceRecord {
  id: string
  employeeId: string
  workScheduleId: string | null
  attendanceDate: string          // 'YYYY-MM-DD'
  checkInTime: string             // ISO datetime (Instant) e.g. '2026-04-24T09:15:30Z'
  checkOutTime: string | null     // ISO datetime | null
  actualWorkHours: number | null
  status: AttendanceStatusValue
  note: string | null
  createdAt: string               // ISO-datetime
}

export interface AttendanceListResponse {
  records: AttendanceRecord[]
}
