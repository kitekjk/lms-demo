import { Link } from 'react-router-dom'
import { Card, CardContent } from '@/components/ui/card'
import { QueryBoundary } from '@/components/layout/QueryBoundary'
import { useMyPayrolls } from '../api'
import { formatKRW } from '@/lib/utils/number'

export default function PayrollListPage() {
  const query = useMyPayrolls()

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold">내 급여</h1>
      <QueryBoundary
        query={query}
        emptyMessage="급여 내역이 없습니다."
        isEmpty={(d) => d.length === 0}
      >
        {(data) => (
          <ul className="space-y-2">
            {data.map((p) => (
              <li key={p.id}>
                <Link to={`/payroll/${p.id}`}>
                  <Card className="transition-colors hover:bg-accent">
                    <CardContent className="flex items-center justify-between p-4">
                      <div>
                        <div className="font-medium">{p.period}</div>
                        <div className="text-xs text-muted-foreground">
                          {p.isPaid ? '지급 완료' : '미지급'}
                        </div>
                      </div>
                      <div className="text-right">
                        <div className="font-semibold">{formatKRW(p.totalAmount)}</div>
                      </div>
                    </CardContent>
                  </Card>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </QueryBoundary>
    </div>
  )
}
