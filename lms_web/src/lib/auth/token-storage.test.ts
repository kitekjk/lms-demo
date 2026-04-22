import { describe, it, expect, beforeEach } from 'vitest'
import { tokenStorage } from './token-storage'

describe('tokenStorage', () => {
  beforeEach(() => localStorage.clear())

  it('returns null when no tokens are stored', () => {
    expect(tokenStorage.getAccessToken()).toBeNull()
    expect(tokenStorage.getRefreshToken()).toBeNull()
  })

  it('persists and reads tokens via setTokens', () => {
    tokenStorage.setTokens('a1', 'r1')
    expect(tokenStorage.getAccessToken()).toBe('a1')
    expect(tokenStorage.getRefreshToken()).toBe('r1')
  })

  it('updates only the access token via setAccessToken', () => {
    tokenStorage.setTokens('a1', 'r1')
    tokenStorage.setAccessToken('a2')
    expect(tokenStorage.getAccessToken()).toBe('a2')
    expect(tokenStorage.getRefreshToken()).toBe('r1')
  })

  it('clears both tokens', () => {
    tokenStorage.setTokens('a1', 'r1')
    tokenStorage.clear()
    expect(tokenStorage.getAccessToken()).toBeNull()
    expect(tokenStorage.getRefreshToken()).toBeNull()
  })
})
