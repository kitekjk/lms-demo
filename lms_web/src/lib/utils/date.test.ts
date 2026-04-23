import { describe, it, expect } from 'vitest'
import { formatDate, formatTime, parseIsoTime, toDateString } from './date'

describe('date helpers', () => {
  it('formatDate renders a Date as YYYY-MM-DD', () => {
    const d = new Date(2026, 3, 23, 15, 30, 0) // month is 0-indexed, tz-stable
    expect(formatDate(d)).toBe('2026-04-23')
  })

  it('toDateString handles string input unchanged', () => {
    expect(toDateString('2026-04-23')).toBe('2026-04-23')
  })

  it('formatTime renders HH:mm from HH:mm:ss', () => {
    expect(formatTime('09:00:00')).toBe('09:00')
    expect(formatTime('18:30:45')).toBe('18:30')
    expect(formatTime(null)).toBe('—')
  })

  it('formatTime handles ISO datetime from backend (Instant)', () => {
    expect(formatTime('2026-04-24T09:15:30Z')).toBe('09:15')
    expect(formatTime('2026-04-24T18:30:45.123Z')).toBe('18:30')
  })

  it('parseIsoTime returns Date on today from HH:mm:ss', () => {
    const d = parseIsoTime('09:15:30')
    expect(d.getHours()).toBe(9)
    expect(d.getMinutes()).toBe(15)
  })
})
