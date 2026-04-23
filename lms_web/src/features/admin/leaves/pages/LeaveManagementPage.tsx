import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { toast } from 'sonner'
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import {
  Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle,
} from '@/components/ui/dialog'
import { Textarea } from '@/components/ui/textarea'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { QueryBoundary } from '@/components/layout/QueryBoundary'
import { usePendingLeaves, useApproveLeave, useRejectLeave } from '../api'
import { LeaveRejectionSchema, type LeaveRejectionInput } from '../schema'
import { leaveTypeLabel } from '@/lib/utils/labels'
import { formatDateKorean } from '@/lib/utils/date'
import { getErrorMessage } from '@/lib/utils/errors'
import type { LeaveRequest } from '@/features/leave/types'

export default function LeaveManagementPage() {
  const query = usePendingLeaves()
  const approve = useApproveLeave()
  const reject = useRejectLeave()
  const [rejecting, setRejecting] = useState<LeaveRequest | null>(null)

  const form = useForm<LeaveRejectionInput>({
    resolver: zodResolver(LeaveRejectionSchema),
    defaultValues: { rejectionReason: '' },
  })

  const onApprove = (id: string) => {
    approve.mutate(id, {
      onSuccess: () => toast.success('승인되었습니다.'),
      onError: (e) => toast.error(getErrorMessage(e)),
    })
  }

  const onReject = form.handleSubmit((values) => {
    if (!rejecting) return
    reject.mutate({ id: rejecting.id, rejectionReason: values.rejectionReason }, {
      onSuccess: () => {
        toast.success('반려되었습니다.')
        setRejecting(null)
        form.reset()
      },
      onError: (e) => form.setError('root', { message: getErrorMessage(e) }),
    })
  })

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-semibold">휴가 승인 대기</h1>

      <QueryBoundary
        query={query}
        emptyMessage="승인 대기 중인 휴가가 없습니다."
        isEmpty={(d) => d.requests.length === 0}
      >
        {(data) => (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>직원</TableHead>
                <TableHead>유형</TableHead>
                <TableHead>기간</TableHead>
                <TableHead>일수</TableHead>
                <TableHead>사유</TableHead>
                <TableHead className="text-right">작업</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {data.requests.map((r) => (
                <TableRow key={r.id}>
                  <TableCell className="font-mono text-xs">{r.employeeId}</TableCell>
                  <TableCell>{leaveTypeLabel(r.leaveType)}</TableCell>
                  <TableCell className="text-xs">{formatDateKorean(r.startDate)} ~ {formatDateKorean(r.endDate)}</TableCell>
                  <TableCell>{r.requestedDays}</TableCell>
                  <TableCell className="max-w-[200px] truncate text-muted-foreground">{r.reason ?? '—'}</TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-2">
                      <Button size="sm" variant="outline" disabled={approve.isPending} onClick={() => onApprove(r.id)}>
                        승인
                      </Button>
                      <Button size="sm" variant="destructive" onClick={() => setRejecting(r)}>
                        반려
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </QueryBoundary>

      <Dialog open={!!rejecting} onOpenChange={(open) => !open && setRejecting(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>휴가 반려</DialogTitle>
          </DialogHeader>
          <Form {...form}>
            <form onSubmit={onReject} className="space-y-4">
              <FormField
                control={form.control}
                name="rejectionReason"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>반려 사유</FormLabel>
                    <FormControl><Textarea rows={4} {...field} /></FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              {form.formState.errors.root && (
                <p className="text-sm text-destructive">{form.formState.errors.root.message}</p>
              )}
              <DialogFooter>
                <Button type="button" variant="outline" onClick={() => setRejecting(null)}>취소</Button>
                <Button type="submit" variant="destructive" disabled={reject.isPending}>
                  {reject.isPending ? '반려 중...' : '반려'}
                </Button>
              </DialogFooter>
            </form>
          </Form>
        </DialogContent>
      </Dialog>
    </div>
  )
}
