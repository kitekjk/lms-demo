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
