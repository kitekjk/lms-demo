import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { toast } from 'sonner'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { QueryBoundary } from '@/components/layout/QueryBoundary'
import { useStoreAttendance, useAdjustAttendance } from '../api'
import { AttendanceAdjustSchema, type AttendanceAdjustInput } from '../schema'
import { useStores } from '@/features/admin/stores/api'
import type { AttendanceRecord } from '@/features/attendance/types'
import { formatDate, formatDateKorean, formatTime } from '@/lib/utils/date'
import { attendanceStatusLabel } from '@/lib/utils/labels'
import { formatHours } from '@/lib/utils/number'
import { getErrorMessage } from '@/lib/utils/errors'

function firstOfMonth(d: Date): Date {
  const x = new Date(d)
  x.setDate(1)
  return x
}

export default function AttendanceManagementPage() {
  const [storeId, setStoreId] = useState<string | undefined>(undefined)
  const today = new Date()
  const [startDate, setStartDate] = useState(formatDate(firstOfMonth(today)))
  const [endDate, setEndDate] = useState(formatDate(today))
  const stores = useStores()
  const query = useStoreAttendance({ storeId, startDate, endDate })
  const [adjusting, setAdjusting] = useState<AttendanceRecord | null>(null)

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-semibold">근태 관리</h1>

      <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
        <div className="space-y-1">
          <Label>매장</Label>
          <Select
            value={storeId ?? '__all'}
            onValueChange={(v) => setStoreId(v === '__all' ? undefined : v)}
          >
            <SelectTrigger>
              <SelectValue placeholder="매장 선택 (필수)" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="__all">(선택)</SelectItem>
              {stores.data?.stores.map((s) => (
                <SelectItem key={s.id} value={s.id}>
                  {s.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div className="space-y-1">
          <Label>시작일</Label>
          <Input
            type="date"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
          />
        </div>
        <div className="space-y-1">
          <Label>종료일</Label>
          <Input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
        </div>
      </div>

      {!storeId ? (
        <p className="text-center text-sm text-muted-foreground">매장을 선택해주세요.</p>
      ) : (
        <QueryBoundary
          query={query}
          emptyMessage="해당 기간 기록이 없습니다."
          isEmpty={(d) => d.records.length === 0}
        >
          {(data) => (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>직원</TableHead>
                  <TableHead>날짜</TableHead>
                  <TableHead>출근</TableHead>
                  <TableHead>퇴근</TableHead>
                  <TableHead>시수</TableHead>
                  <TableHead>상태</TableHead>
                  <TableHead className="text-right">작업</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {data.records.map((r) => (
                  <TableRow key={r.id}>
                    <TableCell className="font-mono text-xs">{r.employeeId}</TableCell>
                    <TableCell>{formatDateKorean(r.attendanceDate)}</TableCell>
                    <TableCell>{formatTime(r.checkInTime)}</TableCell>
                    <TableCell>{formatTime(r.checkOutTime)}</TableCell>
                    <TableCell>{formatHours(r.actualWorkHours)}</TableCell>
                    <TableCell className="text-xs">{attendanceStatusLabel(r.status)}</TableCell>
                    <TableCell className="text-right">
                      <Button size="sm" variant="outline" onClick={() => setAdjusting(r)}>
                        조정
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </QueryBoundary>
      )}

      <Dialog open={!!adjusting} onOpenChange={(o) => !o && setAdjusting(null)}>
        {adjusting && <AdjustDialog record={adjusting} onDone={() => setAdjusting(null)} />}
      </Dialog>
    </div>
  )
}

function AdjustDialog({
  record,
  onDone,
}: {
  record: AttendanceRecord
  onDone: () => void
}) {
  const adjust = useAdjustAttendance()
  const form = useForm<AttendanceAdjustInput>({
    resolver: zodResolver(AttendanceAdjustSchema),
    defaultValues: {
      adjustedCheckInTime: record.checkInTime.slice(0, 5),
      adjustedCheckOutTime: record.checkOutTime?.slice(0, 5) ?? '',
      reason: '',
    },
  })
  const onSubmit = form.handleSubmit((values) => {
    adjust.mutate(
      { recordId: record.id, attendanceDate: record.attendanceDate, input: values },
      {
        onSuccess: () => {
          toast.success('조정되었습니다.')
          onDone()
        },
        onError: (e) => form.setError('root', { message: getErrorMessage(e) }),
      },
    )
  })
  return (
    <DialogContent>
      <DialogHeader>
        <DialogTitle>근태 조정 — {formatDateKorean(record.attendanceDate)}</DialogTitle>
      </DialogHeader>
      <Form {...form}>
        <form onSubmit={onSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-3">
            <FormField
              control={form.control}
              name="adjustedCheckInTime"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>출근</FormLabel>
                  <FormControl>
                    <Input type="time" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="adjustedCheckOutTime"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>퇴근 (선택)</FormLabel>
                  <FormControl>
                    <Input type="time" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
          </div>
          <FormField
            control={form.control}
            name="reason"
            render={({ field }) => (
              <FormItem>
                <FormLabel>조정 사유</FormLabel>
                <FormControl>
                  <Textarea rows={3} {...field} />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          {form.formState.errors.root && (
            <p className="text-sm text-destructive">{form.formState.errors.root.message}</p>
          )}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={onDone}>
              취소
            </Button>
            <Button type="submit" disabled={adjust.isPending}>
              {adjust.isPending ? '저장 중...' : '저장'}
            </Button>
          </DialogFooter>
        </form>
      </Form>
    </DialogContent>
  )
}
