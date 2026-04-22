import { describe, it, expect } from 'vitest'
import { formatKRW, formatHours } from './number'

describe('number helpers', () => {
  it('formatKRW renders integers with ₩ and commas', () => {
    expect(formatKRW(1_234_567)).toBe('₩1,234,567')
    expect(formatKRW(0)).toBe('₩0')
  })

  it('formatHours renders with 1 decimal', () => {
    expect(formatHours(8)).toBe('8.0h')
    expect(formatHours(8.5)).toBe('8.5h')
    expect(formatHours(null)).toBe('—')
  })
})
