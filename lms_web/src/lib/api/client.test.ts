import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import MockAdapter from 'axios-mock-adapter'
import { api } from './client'
import { tokenStorage } from '@/lib/auth/token-storage'

describe('axios client — 401 refresh', () => {
  let mock: MockAdapter
  beforeEach(() => {
    mock = new MockAdapter(api)
    localStorage.clear()
  })
  afterEach(() => {
    mock.restore()
    vi.restoreAllMocks()
  })

  it('refreshes access token on 401 and retries original request', async () => {
    tokenStorage.setTokens('stale', 'refresh-1')

    let firstCall = true
    mock.onGet('/profile').reply((config) => {
      const auth = config.headers?.Authorization
      if (firstCall) {
        firstCall = false
        return [401, { success: false, message: '만료', data: null, timestamp: '' }]
      }
      if (auth === 'Bearer new-access') return [200, { ok: true }]
      return [401, { success: false, message: '재시도-잘못된토큰', data: null, timestamp: '' }]
    })

    mock.onPost('/auth/refresh').reply(200, { accessToken: 'new-access' })

    const res = await api.get('/profile')
    expect(res.data).toEqual({ ok: true })
    expect(tokenStorage.getAccessToken()).toBe('new-access')
    expect(tokenStorage.getRefreshToken()).toBe('refresh-1') // refresh token unchanged
  })

  it('clears tokens and rejects when refresh also fails', async () => {
    tokenStorage.setTokens('stale', 'refresh-1')
    mock.onGet('/profile').reply(401, { success: false, message: 'expired', data: null, timestamp: '' })
    mock.onPost('/auth/refresh').reply(401, { code: 'TOKEN001', message: 'bad refresh', timestamp: '' })

    await expect(api.get('/profile')).rejects.toMatchObject({ status: 401 })
    expect(tokenStorage.getAccessToken()).toBeNull()
    expect(tokenStorage.getRefreshToken()).toBeNull()
  })

  it('shares a single refresh promise across concurrent 401s', async () => {
    tokenStorage.setTokens('stale', 'refresh-1')
    let refreshCount = 0

    mock.onGet('/a').replyOnce(401, { success: false, message: 'e', data: null, timestamp: '' })
    mock.onGet('/a').reply(200, { kind: 'a' })
    mock.onGet('/b').replyOnce(401, { success: false, message: 'e', data: null, timestamp: '' })
    mock.onGet('/b').reply(200, { kind: 'b' })

    mock.onPost('/auth/refresh').reply(() => {
      refreshCount++
      return [200, { accessToken: 'new-access' }]
    })

    const [ra, rb] = await Promise.all([api.get('/a'), api.get('/b')])
    expect(ra.data).toEqual({ kind: 'a' })
    expect(rb.data).toEqual({ kind: 'b' })
    expect(refreshCount).toBe(1) // only ONE refresh call, shared by both 401s
  })
})
