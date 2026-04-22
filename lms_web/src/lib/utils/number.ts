export function formatKRW(amount: number): string {
  return '₩' + amount.toLocaleString('ko-KR')
}

export function formatHours(h: number | null | undefined): string {
  if (h === null || h === undefined) return '—'
  return h.toFixed(1) + 'h'
}
