import { Link } from 'react-router-dom'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { QueryBoundary } from '@/components/layout/QueryBoundary'
import { useCurrentEmployee } from '../api'
import { useMyAttendance } from '@/features/attendance/api'
import { useMySchedule } from '@/features/schedule/api'
import { useMyLeaves } from '@/features/leave/api'
import { formatDate, formatTime, formatDateKorean } from '@/lib/utils/date'
import { attendanceStatusLabel } from '@/lib/utils/labels'

export default function HomePage() {
  const today = formatDate(new Date())
  const employeeQuery = useCurrentEmployee()
  const attendanceQuery = useMyAttendance({ startDate: today, endDate: today })
  const scheduleQuery = useMySchedule({ startDate: today, endDate: today })
  const leavesQuery = useMyLeaves()

  const pendingCount = leavesQuery.data?.requests.filter((r) => r.status === 'PENDING').length ?? 0
  const todaySchedule = scheduleQuery.data?.schedules[0]
  const todayAttendance = attendanceQuery.data?.records[0]

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-xl font-semibold">
          안녕하세요{employeeQuery.data ? `, ${employeeQuery.data.name}` : ''}님
        </h1>
        <p className="text-sm text-muted-foreground">{formatDateKorean(new Date())}</p>
      </div>

      <Card>
        <CardHeader><CardTitle className="text-base">오늘 근무</CardTitle></CardHeader>
        <CardContent className="text-sm space-y-1">
          {todaySchedule ? (
            <div>{formatTime(todaySchedule.startTime)} ~ {formatTime(todaySchedule.endTime)}</div>
          ) : (
            <div className="text-muted-foreground">오늘 예정된 근무가 없습니다.</div>
          )}
          {todayAttendance ? (
            <div className="text-muted-foreground">
              출근 {formatTime(todayAttendance.checkInTime)}
              {todayAttendance.checkOutTime && ` · 퇴근 ${formatTime(todayAttendance.checkOutTime)}`}
              {` · ${attendanceStatusLabel(todayAttendance.status)}`}
            </div>
          ) : (
            <Link to="/attendance" className="text-sm text-primary">→ 출근하기</Link>
          )}
        </CardContent>
      </Card>

      <div className="grid grid-cols-2 gap-3">
        <Link to="/leave">
          <Card className="h-full transition-colors hover:bg-accent">
            <CardContent className="p-4">
              <div className="text-xs text-muted-foreground">대기 중인 휴가</div>
              <div className="text-2xl font-semibold">{pendingCount}</div>
            </CardContent>
          </Card>
        </Link>

        <QueryBoundary
          query={employeeQuery}
          loadingFallback={<div className="h-full" />}
        >
          {(emp) => (
            <Card className="h-full">
              <CardContent className="p-4">
                <div className="text-xs text-muted-foreground">남은 연차</div>
                <div className="text-2xl font-semibold">{emp.remainingLeave}일</div>
              </CardContent>
            </Card>
          )}
        </QueryBoundary>
      </div>

      <div className="space-y-2 pt-2">
        <Link to="/payroll" className="block text-sm text-primary">→ 내 급여 보기</Link>
        <Link to="/schedule" className="block text-sm text-primary">→ 근무 일정 전체 보기</Link>
      </div>
    </div>
  )
}
