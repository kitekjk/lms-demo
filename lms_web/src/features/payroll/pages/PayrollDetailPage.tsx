import { useParams } from 'react-router-dom'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { QueryBoundary } from '@/components/layout/QueryBoundary'
import { usePayrollDetail } from '../api'
import { formatKRW, formatHours } from '@/lib/utils/number'
import { formatDateKorean } from '@/lib/utils/date'

export default function PayrollDetailPage() {
  const { id } = useParams<{ id: string }>()
  const query = usePayrollDetail(id)

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold">급여 상세</h1>

      <QueryBoundary query={query}>
        {(data) => (
          <div className="space-y-4">
            <Card>
              <CardHeader>
                <CardTitle className="text-base">{data.payroll.period}</CardTitle>
              </CardHeader>
              <CardContent className="space-y-1 text-sm">
                <div className="flex justify-between">
                  <span className="text-muted-foreground">기본급</span>
                  <span>{formatKRW(data.payroll.baseAmount)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">연장근무</span>
                  <span>{formatKRW(data.payroll.overtimeAmount)}</span>
                </div>
                <div className="flex justify-between border-t pt-2 font-semibold">
                  <span>합계</span>
                  <span>{formatKRW(data.payroll.totalAmount)}</span>
                </div>
                <div className="pt-2 text-xs text-muted-foreground">
                  {data.payroll.isPaid ? '지급 완료' : '미지급'}
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="text-base">일자별 내역</CardTitle>
              </CardHeader>
              <CardContent>
                {data.details.length === 0 ? (
                  <p className="text-sm text-muted-foreground">상세 내역이 없습니다.</p>
                ) : (
                  <ul className="divide-y text-sm">
                    {data.details.map((d) => (
                      <li key={d.id} className="flex items-center justify-between py-2">
                        <div>
                          <div>{formatDateKorean(d.workDate)}</div>
                          <div className="text-xs text-muted-foreground">
                            {d.workType} · {formatHours(d.hours)} · {formatKRW(d.hourlyRate)}/h × {d.multiplier}
                          </div>
                        </div>
                        <div className="font-medium">{formatKRW(d.amount)}</div>
                      </li>
                    ))}
                  </ul>
                )}
              </CardContent>
            </Card>
          </div>
        )}
      </QueryBoundary>
    </div>
  )
}
