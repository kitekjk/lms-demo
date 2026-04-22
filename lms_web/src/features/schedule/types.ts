export interface WorkSchedule {
  id: string
  employeeId: string
  storeId: string
  workDate: string             // 'YYYY-MM-DD'
  startTime: string            // 'HH:mm:ss'
  endTime: string              // 'HH:mm:ss'
  workHours: number
  isConfirmed: boolean
  isWeekendWork: boolean
  createdAt: string
}

export interface ScheduleListResponse {
  schedules: WorkSchedule[]
}
