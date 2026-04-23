export const leaveTypeLabels: Record<string, string> = {
  ANNUAL: '연차',
  SICK: '병가',
  PERSONAL: '개인 사유',
  MATERNITY: '출산 휴가',
  PATERNITY: '육아 휴가',
  BEREAVEMENT: '경조사',
  UNPAID: '무급 휴가',
}

export const leaveStatusLabels: Record<string, string> = {
  PENDING: '승인 대기',
  APPROVED: '승인됨',
  REJECTED: '거부됨',
  CANCELLED: '취소됨',
}

export const attendanceStatusLabels: Record<string, string> = {
  NORMAL: '정상 출근',
  LATE: '지각',
  EARLY_LEAVE: '조퇴',
  ABSENT: '결근',
  PENDING: '퇴근 대기 중',
}

export function leaveTypeLabel(v: string): string { return leaveTypeLabels[v] ?? v }
export function leaveStatusLabel(v: string): string { return leaveStatusLabels[v] ?? v }
export function attendanceStatusLabel(v: string): string { return attendanceStatusLabels[v] ?? v }

export const employeeTypeLabels: Record<string, string> = {
  REGULAR: '정규',
  IRREGULAR: '비정규',
  PART_TIME: '파트타임',
}
export function employeeTypeLabel(v: string): string { return employeeTypeLabels[v] ?? v }

export const workTypeLabels: Record<string, string> = {
  WEEKDAY: '평일',
  NIGHT: '야간',
  WEEKEND: '주말',
  HOLIDAY: '공휴일',
}
export function workTypeLabel(v: string): string { return workTypeLabels[v] ?? v }

export const batchStatusLabels: Record<string, string> = {
  RUNNING: '실행 중',
  COMPLETED: '완료',
  PARTIAL_SUCCESS: '부분 성공',
  FAILED: '실패',
}
export function batchStatusLabel(v: string): string { return batchStatusLabels[v] ?? v }

export const policyTypeLabels: Record<string, string> = {
  OVERTIME_WEEKDAY: '평일 연장',
  OVERTIME_WEEKEND: '주말 연장',
  OVERTIME_HOLIDAY: '공휴일 연장',
  NIGHT_SHIFT: '야간',
  HOLIDAY_WORK: '공휴일 근무',
  BONUS: '보너스',
  ALLOWANCE: '수당',
}
export function policyTypeLabel(v: string): string { return policyTypeLabels[v] ?? v }
