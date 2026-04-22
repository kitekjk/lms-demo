import { useState } from 'react'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { QueryBoundary } from '@/components/layout/QueryBoundary'
import { useMyAttendance, useCheckIn, useCheckOut } from '../api'
import { formatDate, formatTime, formatDateKorean } from '@/lib/utils/date'
import { attendanceStatusLabel } from '@/lib/utils/labels'
import { formatHours } from '@/lib/utils/number'
import { getErrorMessage } from '@/lib/utils/errors'

export default function AttendancePage() {
  const today = formatDate(new Date())
  const query = useMyAttendance({ startDate: today, endDate: today })
  const checkIn = useCheckIn()
  const checkOut = useCheckOut()
  const [note, setNote] = useState('')

  const handleCheckIn = () => {
    checkIn.mutate({}, {
      onSuccess: () => toast.success('출근이 기록되었습니다.'),
      onError: (e) => toast.error(getErrorMessage(e)),
    })
  }

  const handleCheckOut = () => {
    checkOut.mutate({ note: note || undefined }, {
      onSuccess: () => { toast.success('퇴근이 기록되었습니다.'); setNote('') },
      onError: (e) => toast.error(getErrorMessage(e)),
    })
  }

  return (
    <div className="space-y-4">
      <div className="space-y-1">
        <h1 className="text-xl font-semibold">출퇴근</h1>
        <p className="text-sm text-muted-foreground">{formatDateKorean(new Date())}</p>
      </div>

      <QueryBoundary query={query} loadingFallback="오늘 기록을 불러오는 중...">
        {(data) => {
          const record = data.records[0]
          const notCheckedIn = !record
          const onlyCheckedIn = record && !record.checkOutTime
          const done = record && record.checkOutTime

          return (
            <Card>
              <CardHeader>
                <CardTitle className="text-base">오늘 근무</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                {notCheckedIn && (
                  <div className="space-y-3">
                    <p className="text-sm text-muted-foreground">아직 출근 기록이 없습니다.</p>
                    <Button className="w-full" disabled={checkIn.isPending} onClick={handleCheckIn}>
                      {checkIn.isPending ? '기록 중...' : '출근하기'}
                    </Button>
                  </div>
                )}

                {onlyCheckedIn && (
                  <div className="space-y-3">
                    <div className="text-sm">
                      <div>출근: <span className="font-medium">{formatTime(record!.checkInTime)}</span></div>
                      <div className="text-muted-foreground">상태: {attendanceStatusLabel(record!.status)}</div>
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="note">메모 (선택)</Label>
                      <Input id="note" value={note} onChange={(e) => setNote(e.target.value)} placeholder="퇴근 메모" />
                    </div>
                    <Button className="w-full" disabled={checkOut.isPending} onClick={handleCheckOut}>
                      {checkOut.isPending ? '기록 중...' : '퇴근하기'}
                    </Button>
                  </div>
                )}

                {done && (
                  <div className="space-y-1 text-sm">
                    <div>출근: <span className="font-medium">{formatTime(record!.checkInTime)}</span></div>
                    <div>퇴근: <span className="font-medium">{formatTime(record!.checkOutTime)}</span></div>
                    <div>근무시간: <span className="font-medium">{formatHours(record!.actualWorkHours)}</span></div>
                    <div className="text-muted-foreground">상태: {attendanceStatusLabel(record!.status)}</div>
                    {record!.note && <div className="text-muted-foreground">메모: {record!.note}</div>}
                  </div>
                )}
              </CardContent>
            </Card>
          )
        }}
      </QueryBoundary>
    </div>
  )
}
