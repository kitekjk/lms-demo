import { format, parse } from 'date-fns'

export function formatDate(d: Date): string {
  return format(d, 'yyyy-MM-dd')
}

export function toDateString(input: Date | string): string {
  return typeof input === 'string' ? input : formatDate(input)
}

export function formatTime(hms: string | null | undefined): string {
  if (!hms) return '—'
  return hms.slice(0, 5) // 'HH:mm:ss' → 'HH:mm'
}

export function parseIsoTime(hms: string): Date {
  return parse(hms, 'HH:mm:ss', new Date())
}

export function formatDateKorean(d: Date | string): string {
  const date = typeof d === 'string' ? parse(d, 'yyyy-MM-dd', new Date()) : d
  return format(date, 'yyyy년 M월 d일 (EEE)')
}
