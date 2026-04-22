export type AttendanceStatusValue = 'NORMAL' | 'LATE' | 'EARLY_LEAVE' | 'ABSENT' | 'PENDING'

export interface AttendanceRecord {
  id: string
  employeeId: string
  workScheduleId: string | null
  attendanceDate: string          // 'YYYY-MM-DD'
  checkInTime: string             // 'HH:mm:ss'
  checkOutTime: string | null     // 'HH:mm:ss' | null
  actualWorkHours: number | null
  status: AttendanceStatusValue
  note: string | null
  createdAt: string               // ISO-datetime
}

export interface AttendanceListResponse {
  records: AttendanceRecord[]
}
