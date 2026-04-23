import { format, parse } from 'date-fns'

export function formatDate(d: Date): string {
  return format(d, 'yyyy-MM-dd')
}

export function toDateString(input: Date | string): string {
  return typeof input === 'string' ? input : formatDate(input)
}

export function formatTime(value: string | null | undefined): string {
  if (!value) return '—'
  // ISO datetime (2026-04-24T00:15:30Z or 2026-04-24T00:15:30.000Z): extract HH:mm from time portion
  if (value.includes('T')) {
    const timePart = value.split('T')[1] // '00:15:30Z' or '00:15:30.000Z'
    return timePart.slice(0, 5) // 'HH:mm'
  }
  // HH:mm:ss (legacy or explicitly LocalTime-formatted): just slice
  return value.slice(0, 5)
}

export function parseIsoTime(hms: string): Date {
  return parse(hms, 'HH:mm:ss', new Date())
}

export function formatDateKorean(d: Date | string): string {
  const date = typeof d === 'string' ? parse(d, 'yyyy-MM-dd', new Date()) : d
  return format(date, 'yyyy년 M월 d일 (EEE)')
}
