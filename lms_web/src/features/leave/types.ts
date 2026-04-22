export type LeaveTypeValue =
  | 'ANNUAL' | 'SICK' | 'PERSONAL' | 'MATERNITY' | 'PATERNITY' | 'BEREAVEMENT' | 'UNPAID'

export type LeaveStatusValue = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED'

export interface LeaveRequest {
  id: string
  employeeId: string
  leaveType: LeaveTypeValue
  startDate: string
  endDate: string
  requestedDays: number
  reason: string | null
  status: LeaveStatusValue
  rejectionReason: string | null
  approvedBy: string | null
  approvedAt: string | null
  createdAt: string
}

export interface LeaveListResponse {
  requests: LeaveRequest[]
}
