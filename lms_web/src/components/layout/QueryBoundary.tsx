import type { ReactNode } from 'react'
import type { UseQueryResult } from '@tanstack/react-query'
import { Button } from '@/components/ui/button'

interface Props<T> {
  query: UseQueryResult<T>
  children: (data: T) => ReactNode
  loadingFallback?: ReactNode
  emptyMessage?: string
  isEmpty?: (data: T) => boolean
}

export function QueryBoundary<T>({
  query,
  children,
  loadingFallback,
  emptyMessage = '데이터가 없습니다.',
  isEmpty,
}: Props<T>) {
  if (query.isLoading) {
    return <div className="flex justify-center p-8 text-muted-foreground">{loadingFallback ?? '불러오는 중...'}</div>
  }
  if (query.isError) {
    const msg = (query.error as { message?: string } | undefined)?.message ?? '오류가 발생했습니다.'
    return (
      <div className="flex flex-col items-center gap-2 p-8">
        <p className="text-sm text-destructive">{msg}</p>
        <Button variant="outline" size="sm" onClick={() => query.refetch()}>다시 시도</Button>
      </div>
    )
  }
  if (query.data === undefined) return null
  if (isEmpty && isEmpty(query.data)) {
    return <div className="flex justify-center p-8 text-muted-foreground">{emptyMessage}</div>
  }
  return <>{children(query.data)}</>
}
