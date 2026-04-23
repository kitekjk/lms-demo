import { useState } from 'react'
import { toast } from 'sonner'
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from '@/components/ui/select'
import { QueryBoundary } from '@/components/layout/QueryBoundary'
import { usePayrollsByPeriod, useBatchHistory, useExecuteBatch } from '../api'
import { useStores } from '@/features/admin/stores/api'
import { useAuthStore } from '@/features/auth/store'
import { formatKRW } from '@/lib/utils/number'
import { batchStatusLabel } from '@/lib/utils/labels'
import { getErrorMessage } from '@/lib/utils/errors'

function currentPeriod(): string {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}

export default function PayrollManagementPage() {
  const role = useAuthStore((s) => s.currentUser?.role)
  const [period, setPeriod] = useState(currentPeriod())
  const [batchStoreId, setBatchStoreId] = useState<string | undefined>(undefined)
  const stores = useStores()
  const payrollQuery = usePayrollsByPeriod(period)
  const historyQuery = useBatchHistory()
  const execBatch = useExecuteBatch()

  const onExecute = () => {
    if (!confirm(`${period} 배치를 실행하시겠습니까?`)) return
    execBatch.mutate({ period, storeId: batchStoreId }, {
      onSuccess: () => toast.success('배치가 시작되었습니다.'),
      onError: (e) => toast.error(getErrorMessage(e)),
    })
  }

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-semibold">급여 관리</h1>

      <Card>
        <CardHeader><CardTitle className="text-base">조회</CardTitle></CardHeader>
        <CardContent>
          <div className="flex items-end gap-3">
            <div className="space-y-1">
              <Label>기간 (YYYY-MM)</Label>
              <Input value={period} onChange={(e) => setPeriod(e.target.value)} placeholder="2026-04" className="w-32" />
            </div>
          </div>
        </CardContent>
      </Card>

      {role === 'SUPER_ADMIN' && (
        <Card>
          <CardHeader><CardTitle className="text-base">배치 실행</CardTitle></CardHeader>
          <CardContent>
            <div className="flex items-end gap-3">
              <div className="space-y-1">
                <Label>매장 (선택, 전체면 비움)</Label>
                <Select value={batchStoreId ?? '__all'} onValueChange={(v) => setBatchStoreId(v === '__all' ? undefined : v)}>
                  <SelectTrigger className="w-[200px]"><SelectValue /></SelectTrigger>
                  <SelectContent>
                    <SelectItem value="__all">(전체)</SelectItem>
                    {stores.data?.stores.map((s) => <SelectItem key={s.id} value={s.id}>{s.name}</SelectItem>)}
                  </SelectContent>
                </Select>
              </div>
              <Button onClick={onExecute} disabled={execBatch.isPending}>
                {execBatch.isPending ? '실행 중...' : '배치 실행'}
              </Button>
            </div>
          </CardContent>
        </Card>
      )}

      <QueryBoundary
        query={payrollQuery}
        emptyMessage="해당 기간 급여가 없습니다."
        isEmpty={(d) => d.length === 0}
      >
        {(data) => (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>직원</TableHead>
                <TableHead>기본급</TableHead>
                <TableHead>연장근무</TableHead>
                <TableHead>합계</TableHead>
                <TableHead>지급 상태</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {data.map((p) => (
                <TableRow key={p.id}>
                  <TableCell className="font-mono text-xs">{p.employeeId}</TableCell>
                  <TableCell>{formatKRW(p.baseAmount)}</TableCell>
                  <TableCell>{formatKRW(p.overtimeAmount)}</TableCell>
                  <TableCell className="font-medium">{formatKRW(p.totalAmount)}</TableCell>
                  <TableCell>
                    {p.isPaid ? <Badge variant="outline">지급 완료</Badge> : <Badge variant="secondary">미지급</Badge>}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </QueryBoundary>

      <Card>
        <CardHeader><CardTitle className="text-base">최근 배치 이력</CardTitle></CardHeader>
        <CardContent>
          <QueryBoundary
            query={historyQuery}
            emptyMessage="배치 이력이 없습니다."
            isEmpty={(d) => d.length === 0}
          >
            {(data) => (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>기간</TableHead>
                    <TableHead>매장</TableHead>
                    <TableHead>상태</TableHead>
                    <TableHead>성공/실패</TableHead>
                    <TableHead>시작</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {data.slice(0, 10).map((h) => (
                    <TableRow key={h.id}>
                      <TableCell>{h.period}</TableCell>
                      <TableCell className="font-mono text-xs">{h.storeId ?? '(전체)'}</TableCell>
                      <TableCell>{batchStatusLabel(h.status)}</TableCell>
                      <TableCell>{h.successCount} / {h.failureCount}</TableCell>
                      <TableCell className="text-xs">{new Date(h.startedAt).toLocaleString('ko-KR')}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </QueryBoundary>
        </CardContent>
      </Card>
    </div>
  )
}
