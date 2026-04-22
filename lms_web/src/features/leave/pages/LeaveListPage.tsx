import { Link } from 'react-router-dom'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { QueryBoundary } from '@/components/layout/QueryBoundary'
import { useMyLeaves, useCancelLeave } from '../api'
import { leaveTypeLabel, leaveStatusLabel } from '@/lib/utils/labels'
import { formatDateKorean } from '@/lib/utils/date'
import { getErrorMessage } from '@/lib/utils/errors'
import { cn } from '@/lib/utils'

const statusTone: Record<string, string> = {
  PENDING: 'text-yellow-600',
  APPROVED: 'text-green-700',
  REJECTED: 'text-destructive',
  CANCELLED: 'text-muted-foreground line-through',
}

export default function LeaveListPage() {
  const query = useMyLeaves()
  const cancel = useCancelLeave()

  const onCancel = (id: string) => {
    cancel.mutate(id, {
      onSuccess: () => toast.success('휴가 신청이 취소되었습니다.'),
      onError: (e) => toast.error(getErrorMessage(e)),
    })
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold">내 휴가</h1>
        <Link to="/leave/request"><Button size="sm">신청하기</Button></Link>
      </div>

      <QueryBoundary
        query={query}
        emptyMessage="신청한 휴가가 없습니다."
        isEmpty={(d) => d.requests.length === 0}
      >
        {(data) => (
          <ul className="space-y-2">
            {data.requests.map((r) => (
              <li key={r.id}>
                <Card>
                  <CardContent className="p-4 text-sm space-y-2">
                    <div className="flex items-center justify-between">
                      <div className="font-medium">{leaveTypeLabel(r.leaveType)} · {r.requestedDays}일</div>
                      <span className={cn('text-xs', statusTone[r.status] ?? '')}>{leaveStatusLabel(r.status)}</span>
                    </div>
                    <div className="text-muted-foreground">
                      {formatDateKorean(r.startDate)} ~ {formatDateKorean(r.endDate)}
                    </div>
                    {r.reason && <div className="text-muted-foreground">사유: {r.reason}</div>}
                    {r.rejectionReason && <div className="text-destructive">반려 사유: {r.rejectionReason}</div>}
                    {r.status === 'PENDING' && (
                      <div className="flex justify-end">
                        <Button variant="outline" size="sm" disabled={cancel.isPending} onClick={() => onCancel(r.id)}>
                          취소
                        </Button>
                      </div>
                    )}
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
