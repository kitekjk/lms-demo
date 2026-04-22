import { useState } from 'react'
import { Card, CardContent } from '@/components/ui/card'
import { Calendar } from '@/components/ui/calendar'
import { QueryBoundary } from '@/components/layout/QueryBoundary'
import { useMySchedule } from '../api'
import { formatDate, formatTime, formatDateKorean } from '@/lib/utils/date'
import { formatHours } from '@/lib/utils/number'

function monthRange(d: Date) {
  const start = new Date(d.getFullYear(), d.getMonth(), 1)
  const end = new Date(d.getFullYear(), d.getMonth() + 1, 0)
  return { startDate: formatDate(start), endDate: formatDate(end) }
}

export default function SchedulePage() {
  const [month, setMonth] = useState(new Date())
  const [selected, setSelected] = useState<Date | undefined>(new Date())
  const range = monthRange(month)
  const query = useMySchedule(range)

  const selectedSchedule = query.data?.schedules.find(
    (s) => selected && s.workDate === formatDate(selected),
  )

  const scheduledDates = (query.data?.schedules ?? []).map((s) => {
    const [y, m, d] = s.workDate.split('-').map(Number)
    return new Date(y, m - 1, d)
  })

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold">근무 일정</h1>

      <Card>
        <CardContent className="flex justify-center p-3">
          <Calendar
            mode="single"
            month={month}
            onMonthChange={setMonth}
            selected={selected}
            onSelect={setSelected}
            modifiers={{ scheduled: scheduledDates }}
            modifiersClassNames={{ scheduled: 'bg-primary/15 font-semibold' }}
          />
        </CardContent>
      </Card>

      <QueryBoundary query={query} loadingFallback="일정을 불러오는 중...">
        {() => (
          selectedSchedule ? (
            <Card>
              <CardContent className="p-4 text-sm space-y-1">
                <div className="font-medium">{formatDateKorean(selectedSchedule.workDate)}</div>
                <div>{formatTime(selectedSchedule.startTime)} ~ {formatTime(selectedSchedule.endTime)} · {formatHours(selectedSchedule.workHours)}</div>
                <div className="text-muted-foreground">
                  {selectedSchedule.isConfirmed ? '확정됨' : '미확정'}
                  {selectedSchedule.isWeekendWork && ' · 주말 근무'}
                </div>
              </CardContent>
            </Card>
          ) : (
            <p className="text-center text-sm text-muted-foreground">선택한 날짜에 일정이 없습니다.</p>
          )
        )}
      </QueryBoundary>
    </div>
  )
}
