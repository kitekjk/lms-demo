import { useState } from 'react'
import { Card, CardContent } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { QueryBoundary } from '@/components/layout/QueryBoundary'
import { useMyAttendance } from '../api'
import { formatDate, formatTime, formatDateKorean } from '@/lib/utils/date'
import { attendanceStatusLabel } from '@/lib/utils/labels'
import { formatHours } from '@/lib/utils/number'

function firstOfMonth(d: Date): Date { const x = new Date(d); x.setDate(1); return x }

export default function AttendanceHistoryPage() {
  const today = new Date()
  const [startDate, setStartDate] = useState(formatDate(firstOfMonth(today)))
  const [endDate, setEndDate] = useState(formatDate(today))

  const query = useMyAttendance({ startDate, endDate })

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold">근태 이력</h1>

      <div className="grid grid-cols-2 gap-3">
        <div className="space-y-1">
          <Label htmlFor="start">시작일</Label>
          <Input id="start" type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
        </div>
        <div className="space-y-1">
          <Label htmlFor="end">종료일</Label>
          <Input id="end" type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
        </div>
      </div>

      <QueryBoundary
        query={query}
        emptyMessage="해당 기간에 기록이 없습니다."
        isEmpty={(d) => d.records.length === 0}
      >
        {(data) => (
          <ul className="space-y-2">
            {data.records.map((r) => (
              <li key={r.id}>
                <Card>
                  <CardContent className="flex items-center justify-between p-4 text-sm">
                    <div>
                      <div className="font-medium">{formatDateKorean(r.attendanceDate)}</div>
                      <div className="text-muted-foreground">
                        {formatTime(r.checkInTime)} ~ {formatTime(r.checkOutTime)} · {formatHours(r.actualWorkHours)}
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="text-xs text-muted-foreground">{attendanceStatusLabel(r.status)}</div>
                    </div>
                  </CardContent>
                </Card>
              </li>
            ))}
          </ul>
        )}
      </QueryBoundary>
    </div>
  )
}
