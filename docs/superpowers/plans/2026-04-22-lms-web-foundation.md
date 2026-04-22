# LMS Web Foundation Implementation Plan (Plan 1 of 4)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Bootstrap `lms_web/` React application with working authentication, routing, and role-based shell layouts (Employee/Admin). After this plan, the user can log in, navigate to placeholder pages under each role's shell, and the session persists across reloads.

**Architecture:** Vite + React + TS + Tailwind + shadcn/ui. axios with JWT interceptor (request injection + 401 refresh). TanStack Query for server state, Zustand (persisted) for auth state. React Router v6 `createBrowserRouter` with role-guarded nested shells.

**Tech Stack:** React 19, TypeScript 5.x, Vite 5.x, Tailwind CSS 3.x, shadcn/ui (Radix UI), axios, @tanstack/react-query v5, zustand 4.x, react-router-dom v6, react-hook-form + zod, lucide-react icons, sonner (toast), vitest + @testing-library/react.

**Source spec:** `docs/superpowers/specs/2026-04-22-lms-web-react-port-design.md`

---

## Source of Truth: Backend Contracts (verified)

These are the actual backend shapes verified against `interfaces/` source. Use these verbatim — do not assume an envelope everywhere.

### Auth endpoints (RAW responses, no ApiResponse wrapper)

- `POST /api/auth/login` → request `{ email, password }` → 200 response:
  ```json
  {
    "accessToken": "<jwt>",
    "refreshToken": "<jwt>",
    "userInfo": { "userId": "string", "email": "string", "role": "EMPLOYEE|MANAGER|SUPER_ADMIN", "isActive": true }
  }
  ```
- `POST /api/auth/refresh` → request `{ refreshToken }` → 200 response:
  ```json
  { "accessToken": "<jwt>" }
  ```
  **Note:** only `accessToken` is returned — refresh token is NOT re-issued.
- `POST /api/auth/logout` → returns `{ "message": "로그아웃 되었습니다" }`. Client must clear tokens locally.
- Error response from AuthController: RAW `{ code, message, timestamp }` (not wrapped). Common codes: `AUTH001` (invalid credentials, 401), `AUTH002` (inactive, 403), `REG001` (duplicate email, 409).

### Non-auth endpoints (ApiResponse<T> wrapper)

- Error response from GlobalExceptionHandler: `{ success: false, message, data, timestamp }`.
- Success: varies per controller (some wrap, some raw). Treat per-feature.

### CORS

- `SecurityConfig.corsConfigurationSource()` already allows `allowedOriginPatterns = ["*"]` with credentials. **No backend CORS change needed** for dev.
- Use Vite proxy for dev anyway to avoid credentialed-origin edge cases and keep same-origin semantics.

### `/api/auth/me`

- **Does NOT exist** in backend. Do not call it.
- After login, `userInfo` comes embedded in `LoginResponse`. Persist it in Zustand.
- On bootstrap: if `accessToken` is present AND `userInfo` is persisted, treat as authenticated. If either is missing, redirect to `/login`.

---

## File Structure (what gets created in this plan)

```
lms_web/                                        # ✨ NEW root
├── .gitignore
├── .env
├── .env.example
├── .editorconfig
├── .prettierrc.json
├── eslint.config.js
├── index.html
├── package.json
├── tsconfig.json
├── tsconfig.node.json
├── vite.config.ts
├── tailwind.config.ts
├── postcss.config.js
├── components.json                             # shadcn/ui config
└── src/
    ├── main.tsx
    ├── App.tsx
    ├── index.css                               # tailwind directives + theme tokens
    ├── vite-env.d.ts
    ├── lib/
    │   ├── api/
    │   │   ├── client.ts                       # axios instance + interceptors
    │   │   ├── endpoints.ts                    # path constants
    │   │   └── types.ts                        # ApiResponse<T>, ApiError
    │   ├── auth/
    │   │   ├── token-storage.ts                # localStorage wrapper
    │   │   └── use-auth.ts                     # useAuth() hook
    │   ├── router/
    │   │   ├── routes.tsx                      # createBrowserRouter definition
    │   │   └── protected-route.tsx             # role-guard wrapper
    │   └── utils/
    │       └── errors.ts                       # getErrorMessage()
    ├── components/
    │   ├── ui/                                 # shadcn/ui installed components (button, input, form, card, label, separator, sonner)
    │   └── layout/
    │       ├── EmployeeShell.tsx               # header + bottom tab bar
    │       ├── AdminShell.tsx                  # sidebar + top header
    │       └── QueryBoundary.tsx               # loading/error/empty wrapper
    ├── features/
    │   └── auth/
    │       ├── api.ts                          # useLogin, useLogout, useRefresh hooks
    │       ├── store.ts                        # useAuthStore (zustand + persist)
    │       ├── schema.ts                       # zod LoginSchema
    │       └── pages/
    │           └── LoginPage.tsx
    │   # (M3/M4 features are placeholder-only in this plan — added as empty pages in Task 3.2)
    └── test/
        └── setup.ts                            # vitest setup

# Root updates
.gitignore                                      # ✏️ add lms_web/node_modules, lms_web/dist, lms_web/.env
```

**Files per responsibility (no cross-responsibility bleed):**
- `lib/api/client.ts` owns axios + interceptor logic (single source of HTTP behavior).
- `lib/auth/token-storage.ts` owns localStorage reads/writes for tokens (isolated for testability).
- `features/auth/store.ts` owns user info state (separate from token storage).
- `lib/router/protected-route.tsx` owns auth/role guards (single choke-point).
- Each layout Shell owns its own navigation UI.

---

# Phase M0 — Bootstrap

## Task 0.1: Create Vite + React + TS skeleton

**Files:**
- Create: `lms_web/` (entire scaffold from `npm create vite`)
- Modify: `.gitignore` (root)

- [ ] **Step 1: Scaffold Vite project**

Run from repo root:
```bash
npm create vite@latest lms_web -- --template react-ts
```

Expected: Creates `lms_web/` with default Vite+React+TS template. If prompted, confirm template choice.

- [ ] **Step 2: Install dependencies**

```bash
cd lms_web
npm install
```

Expected: `lms_web/node_modules/` populated, `package-lock.json` created.

- [ ] **Step 3: Verify dev server boots**

```bash
npm run dev
```

Expected: Vite prints `➜  Local:   http://localhost:5173/`. Open browser → default Vite+React page loads. Stop with Ctrl+C.

- [ ] **Step 4: Update root `.gitignore`**

Edit `c:\Users\kitek\IdeaProjects\lms-demo\.gitignore`, append:

```
# React web
lms_web/node_modules/
lms_web/dist/
lms_web/.env
lms_web/.env.*.local
lms_web/*.local
```

- [ ] **Step 5: Commit**

```bash
git add lms_web/ .gitignore
git commit -m "feat(lms_web): scaffold Vite + React + TS project"
```

---

## Task 0.2: Configure Tailwind CSS

**Files:**
- Create: `lms_web/tailwind.config.ts`, `lms_web/postcss.config.js`
- Modify: `lms_web/src/index.css`

- [ ] **Step 1: Install Tailwind**

```bash
cd lms_web
npm install -D tailwindcss@^3 postcss autoprefixer
npx tailwindcss init -p
```

Expected: `tailwind.config.js` and `postcss.config.js` created.

- [ ] **Step 2: Rename and update `tailwind.config.js` to `tailwind.config.ts`**

Delete `tailwind.config.js`. Create `lms_web/tailwind.config.ts`:

```ts
import type { Config } from 'tailwindcss'

const config: Config = {
  darkMode: ['class'],
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    container: {
      center: true,
      padding: '1rem',
      screens: { '2xl': '1400px' },
    },
    extend: {
      colors: {
        border: 'hsl(var(--border))',
        input: 'hsl(var(--input))',
        ring: 'hsl(var(--ring))',
        background: 'hsl(var(--background))',
        foreground: 'hsl(var(--foreground))',
        primary: { DEFAULT: 'hsl(var(--primary))', foreground: 'hsl(var(--primary-foreground))' },
        secondary: { DEFAULT: 'hsl(var(--secondary))', foreground: 'hsl(var(--secondary-foreground))' },
        destructive: { DEFAULT: 'hsl(var(--destructive))', foreground: 'hsl(var(--destructive-foreground))' },
        muted: { DEFAULT: 'hsl(var(--muted))', foreground: 'hsl(var(--muted-foreground))' },
        accent: { DEFAULT: 'hsl(var(--accent))', foreground: 'hsl(var(--accent-foreground))' },
        popover: { DEFAULT: 'hsl(var(--popover))', foreground: 'hsl(var(--popover-foreground))' },
        card: { DEFAULT: 'hsl(var(--card))', foreground: 'hsl(var(--card-foreground))' },
      },
      borderRadius: {
        lg: 'var(--radius)',
        md: 'calc(var(--radius) - 2px)',
        sm: 'calc(var(--radius) - 4px)',
      },
    },
  },
  plugins: [require('tailwindcss-animate')],
}

export default config
```

- [ ] **Step 3: Install tailwindcss-animate**

```bash
npm install -D tailwindcss-animate
```

- [ ] **Step 4: Replace `src/index.css` with Tailwind directives + theme tokens**

Overwrite `lms_web/src/index.css`:

```css
@tailwind base;
@tailwind components;
@tailwind utilities;

@layer base {
  :root {
    --background: 0 0% 100%;
    --foreground: 222.2 84% 4.9%;
    --card: 0 0% 100%;
    --card-foreground: 222.2 84% 4.9%;
    --popover: 0 0% 100%;
    --popover-foreground: 222.2 84% 4.9%;
    --primary: 221.2 83.2% 53.3%;
    --primary-foreground: 210 40% 98%;
    --secondary: 210 40% 96.1%;
    --secondary-foreground: 222.2 47.4% 11.2%;
    --muted: 210 40% 96.1%;
    --muted-foreground: 215.4 16.3% 46.9%;
    --accent: 210 40% 96.1%;
    --accent-foreground: 222.2 47.4% 11.2%;
    --destructive: 0 84.2% 60.2%;
    --destructive-foreground: 210 40% 98%;
    --border: 214.3 31.8% 91.4%;
    --input: 214.3 31.8% 91.4%;
    --ring: 221.2 83.2% 53.3%;
    --radius: 0.5rem;
  }
}

@layer base {
  * { @apply border-border; }
  body { @apply bg-background text-foreground antialiased; font-family: system-ui, -apple-system, 'Malgun Gothic', sans-serif; }
}
```

- [ ] **Step 5: Replace `src/App.tsx` with a Tailwind-smoke test**

Overwrite `lms_web/src/App.tsx`:

```tsx
export default function App() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-background">
      <h1 className="text-3xl font-bold text-primary">LMS Web</h1>
    </div>
  )
}
```

Delete `lms_web/src/App.css`.

- [ ] **Step 6: Run dev server and verify Tailwind works**

```bash
npm run dev
```

Expected: Browser at http://localhost:5173 shows large bold blue "LMS Web" text centered on white background. If styles don't apply, check that `index.css` is imported in `main.tsx` (default Vite setup imports it).

Ctrl+C to stop.

- [ ] **Step 7: Commit**

```bash
git add lms_web/
git commit -m "feat(lms_web): configure Tailwind CSS with shadcn theme tokens"
```

---

## Task 0.3: Configure TypeScript path alias `@/*`

**Files:**
- Modify: `lms_web/tsconfig.json`, `lms_web/tsconfig.node.json`, `lms_web/vite.config.ts`

- [ ] **Step 1: Add `@/*` alias to `tsconfig.json`**

Open `lms_web/tsconfig.json`. Inside the existing `compilerOptions` object, add:

```json
    "baseUrl": ".",
    "paths": { "@/*": ["./src/*"] }
```

Keep all other existing options intact.

- [ ] **Step 2: Install `@types/node` so Vite config can use `path`**

```bash
cd lms_web
npm install -D @types/node
```

- [ ] **Step 3: Add alias to `vite.config.ts`**

Overwrite `lms_web/vite.config.ts`:

```ts
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'node:path'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: { '@': path.resolve(__dirname, './src') },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
```

- [ ] **Step 4: Smoke test the alias**

Edit `lms_web/src/App.tsx` — change relative imports to aliased (none yet, so just verify no regression):

```bash
npm run dev
```

Expected: no type errors, page still renders. Stop with Ctrl+C.

- [ ] **Step 5: Run typecheck**

```bash
npm run build -- --mode development 2>&1 | head -20
# Or simpler:
npx tsc --noEmit
```

Expected: exits 0, no errors.

- [ ] **Step 6: Commit**

```bash
git add lms_web/tsconfig.json lms_web/tsconfig.node.json lms_web/vite.config.ts lms_web/package.json lms_web/package-lock.json
git commit -m "chore(lms_web): add @/* path alias and Vite dev proxy for /api"
```

---

## Task 0.4: Initialize shadcn/ui and install base components

**Files:**
- Create: `lms_web/components.json`, `lms_web/src/lib/utils.ts` (auto-created by shadcn)
- Create: `lms_web/src/components/ui/{button,input,label,card,form,separator,sonner}.tsx` (auto-installed)

- [ ] **Step 1: Run shadcn/ui init**

```bash
cd lms_web
npx shadcn@latest init
```

When prompted, answer:
- Style: `Default`
- Base color: `Slate`
- CSS variables: `Yes`
- tailwind.config path: `tailwind.config.ts` (default)
- components alias: `@/components`
- utils alias: `@/lib/utils`
- React Server Components: `No`
- Write configuration to components.json: `Yes`

Expected: `components.json` created, `src/lib/utils.ts` created with `cn()` helper.

- [ ] **Step 2: Install base components**

```bash
npx shadcn@latest add button input label card form separator sonner
```

Expected: files appear in `src/components/ui/`. Dependencies auto-installed (`@radix-ui/*`, `react-hook-form`, `sonner`, etc.).

- [ ] **Step 3: Smoke test a shadcn component**

Overwrite `lms_web/src/App.tsx`:

```tsx
import { Button } from '@/components/ui/button'

export default function App() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-background">
      <Button>LMS Web — Hello</Button>
    </div>
  )
}
```

```bash
npm run dev
```

Expected: styled button renders with hover states. Ctrl+C to stop.

- [ ] **Step 4: Commit**

```bash
git add lms_web/
git commit -m "feat(lms_web): init shadcn/ui with base components (button/input/label/card/form/separator/sonner)"
```

---

## Task 0.5: Install runtime dependencies

**Files:**
- Modify: `lms_web/package.json`

- [ ] **Step 1: Install runtime libs**

```bash
cd lms_web
npm install axios @tanstack/react-query zustand react-router-dom zod date-fns lucide-react
```

(Note: `react-hook-form` was already pulled in by `shadcn add form`; `sonner` also already installed.)

- [ ] **Step 2: Install dev libs (testing + typing)**

```bash
npm install -D vitest @testing-library/react @testing-library/jest-dom @testing-library/user-event jsdom @vitest/ui msw
```

- [ ] **Step 3: Verify versions**

```bash
cat package.json | grep -E '"(react|react-dom|axios|zustand|@tanstack|react-router-dom|zod|vitest)"'
```

Expected output includes (versions may be newer):
```
"@tanstack/react-query": "^5...",
"axios": "^1...",
"react": "^19...",
"react-dom": "^19...",
"react-router-dom": "^6...",
"zod": "^3...",
"zustand": "^4..."
```

- [ ] **Step 4: Commit**

```bash
git add lms_web/package.json lms_web/package-lock.json
git commit -m "chore(lms_web): install runtime deps (axios, tanstack-query, zustand, react-router, zod, date-fns, lucide) and test deps"
```

---

## Task 0.6: Configure Vitest + add npm scripts

**Files:**
- Create: `lms_web/src/test/setup.ts`
- Modify: `lms_web/vite.config.ts`, `lms_web/package.json`, `lms_web/tsconfig.json`

- [ ] **Step 1: Create Vitest setup file**

Create `lms_web/src/test/setup.ts`:

```ts
import '@testing-library/jest-dom/vitest'
import { afterEach } from 'vitest'
import { cleanup } from '@testing-library/react'

afterEach(() => {
  cleanup()
})
```

- [ ] **Step 2: Wire Vitest into `vite.config.ts`**

Replace the full content of `lms_web/vite.config.ts`:

```ts
/// <reference types="vitest" />
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'node:path'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: { '@': path.resolve(__dirname, './src') },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/test/setup.ts'],
    css: false,
  },
})
```

- [ ] **Step 3: Add scripts to `package.json`**

In `lms_web/package.json` under `"scripts"`, ensure these entries exist (add missing ones, keep existing dev/build/preview):

```json
    "dev": "vite",
    "build": "tsc -b && vite build",
    "preview": "vite preview",
    "lint": "eslint .",
    "test": "vitest",
    "test:ui": "vitest --ui",
    "test:run": "vitest run",
    "typecheck": "tsc -b --noEmit"
```

- [ ] **Step 4: Add vitest globals to `tsconfig.json`**

In `lms_web/tsconfig.app.json` (or `tsconfig.json` depending on Vite scaffold — find the one with `compilerOptions.types`), add `"vitest/globals"` to `types`:

```json
    "types": ["vitest/globals"]
```

If `types` doesn't exist in compilerOptions, add it.

- [ ] **Step 5: Write a sanity Vitest**

Create `lms_web/src/test/smoke.test.ts`:

```ts
import { describe, it, expect } from 'vitest'

describe('vitest setup', () => {
  it('can run a basic assertion', () => {
    expect(1 + 1).toBe(2)
  })
})
```

- [ ] **Step 6: Run it**

```bash
npm run test:run
```

Expected: `1 passed`. If fails, debug setup before moving on.

- [ ] **Step 7: Commit**

```bash
git add lms_web/
git commit -m "chore(lms_web): configure vitest + scripts (dev/build/test/typecheck)"
```

---

## Task 0.7: Env files and boot smoke

**Files:**
- Create: `lms_web/.env`, `lms_web/.env.example`, `lms_web/src/vite-env.d.ts` (update)

- [ ] **Step 1: Create `.env.example`**

Create `lms_web/.env.example`:

```
VITE_API_BASE_URL=/api
VITE_APP_ENV=local
```

- [ ] **Step 2: Copy to `.env`**

```bash
cd lms_web
cp .env.example .env
```

- [ ] **Step 3: Extend `vite-env.d.ts`**

Overwrite `lms_web/src/vite-env.d.ts`:

```ts
/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string
  readonly VITE_APP_ENV: 'local' | 'dev' | 'prod'
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
```

- [ ] **Step 4: Smoke test typecheck**

```bash
npm run typecheck
```

Expected: exits 0.

- [ ] **Step 5: Commit**

```bash
git add lms_web/.env.example lms_web/src/vite-env.d.ts
# Note: .env itself is gitignored
git commit -m "chore(lms_web): add env template and typed import.meta.env"
```

---

# Phase M1 — Authentication Foundation

## Task 1.1: Token storage (TDD)

**Files:**
- Create: `lms_web/src/lib/auth/token-storage.ts`
- Test: `lms_web/src/lib/auth/token-storage.test.ts`

- [ ] **Step 1: Write failing tests**

Create `lms_web/src/lib/auth/token-storage.test.ts`:

```ts
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
```

- [ ] **Step 2: Run test — expect fail (module missing)**

```bash
npm run test:run -- src/lib/auth/token-storage.test.ts
```

Expected: FAIL — "Cannot find module './token-storage'".

- [ ] **Step 3: Implement**

Create `lms_web/src/lib/auth/token-storage.ts`:

```ts
const ACCESS_KEY = 'lms.access_token'
const REFRESH_KEY = 'lms.refresh_token'

export const tokenStorage = {
  getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_KEY)
  },
  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_KEY)
  },
  setTokens(access: string, refresh: string): void {
    localStorage.setItem(ACCESS_KEY, access)
    localStorage.setItem(REFRESH_KEY, refresh)
  },
  setAccessToken(access: string): void {
    localStorage.setItem(ACCESS_KEY, access)
  },
  clear(): void {
    localStorage.removeItem(ACCESS_KEY)
    localStorage.removeItem(REFRESH_KEY)
  },
}
```

- [ ] **Step 4: Run test — expect pass**

```bash
npm run test:run -- src/lib/auth/token-storage.test.ts
```

Expected: 4 passed.

- [ ] **Step 5: Commit**

```bash
git add lms_web/src/lib/auth/
git commit -m "feat(lms_web): add token-storage with separate access/refresh setters"
```

---

## Task 1.2: API client — axios instance + types

**Files:**
- Create: `lms_web/src/lib/api/types.ts`, `lms_web/src/lib/api/endpoints.ts`, `lms_web/src/lib/api/client.ts`
- Create: `lms_web/src/lib/utils/errors.ts`

- [ ] **Step 1: Create `types.ts`**

Create `lms_web/src/lib/api/types.ts`:

```ts
// Generic ApiResponse envelope used by non-auth controllers
export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T | null
  timestamp: string
}

// Raw error shape from AuthController's @ExceptionHandler
export interface AuthErrorResponse {
  code: string
  message: string
  timestamp: string
}

// Normalized error consumed by UI
export interface ApiError {
  status: number
  code: string | null      // AUTH001, AUTH002, REG001, etc. if present
  message: string          // Korean user-facing message
}
```

- [ ] **Step 2: Create `endpoints.ts`**

Create `lms_web/src/lib/api/endpoints.ts`:

```ts
export const endpoints = {
  auth: {
    login: '/auth/login',
    refresh: '/auth/refresh',
    logout: '/auth/logout',
  },
} as const
```

(Other feature endpoints will be added in later plans.)

- [ ] **Step 3: Create `utils/errors.ts`**

Create `lms_web/src/lib/utils/errors.ts`:

```ts
import type { ApiError } from '@/lib/api/types'

export function getErrorMessage(err: unknown): string {
  if (err && typeof err === 'object' && 'message' in err) {
    const m = (err as { message?: unknown }).message
    if (typeof m === 'string' && m.length > 0) return m
  }
  return '알 수 없는 오류가 발생했습니다.'
}

export function isApiError(err: unknown): err is ApiError {
  return !!err && typeof err === 'object' && 'status' in err && 'message' in err
}
```

- [ ] **Step 4: Create `client.ts` — axios instance only (no interceptors yet)**

Create `lms_web/src/lib/api/client.ts`:

```ts
import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { tokenStorage } from '@/lib/auth/token-storage'
import type { ApiError, ApiResponse, AuthErrorResponse } from './types'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15_000,
})

// Request: inject Authorization header
api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = tokenStorage.getAccessToken()
  if (token) {
    config.headers.set?.('Authorization', `Bearer ${token}`)
    // Fallback for older axios where headers is plain object
    ;(config.headers as Record<string, unknown>).Authorization = `Bearer ${token}`
  }
  return config
})

// Normalize errors — response interceptor for errors only at this stage (401 refresh added in Task 1.3)
api.interceptors.response.use(
  (res) => res,
  (err: AxiosError) => Promise.reject(normalizeError(err)),
)

function normalizeError(err: AxiosError): ApiError {
  const status = err.response?.status ?? 0
  const data = err.response?.data as ApiResponse<unknown> | AuthErrorResponse | undefined
  if (data && typeof data === 'object') {
    if ('code' in data && typeof data.code === 'string') {
      return { status, code: data.code, message: data.message ?? '' }
    }
    if ('success' in data) {
      return { status, code: null, message: data.message ?? '' }
    }
  }
  return { status, code: null, message: err.message || '네트워크 오류가 발생했습니다.' }
}
```

- [ ] **Step 5: Smoke typecheck**

```bash
cd lms_web && npm run typecheck
```

Expected: exits 0.

- [ ] **Step 6: Commit**

```bash
git add lms_web/src/lib/
git commit -m "feat(lms_web): axios client with request auth injection and error normalization"
```

---

## Task 1.3: 401 refresh interceptor with race protection (TDD)

**Files:**
- Modify: `lms_web/src/lib/api/client.ts`
- Create: `lms_web/src/lib/api/client.test.ts`

- [ ] **Step 1: Write failing tests**

Create `lms_web/src/lib/api/client.test.ts`:

```ts
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
```

- [ ] **Step 2: Install `axios-mock-adapter`**

```bash
cd lms_web
npm install -D axios-mock-adapter
```

- [ ] **Step 3: Run tests — expect fail**

```bash
npm run test:run -- src/lib/api/client.test.ts
```

Expected: all 3 fail (no refresh logic yet).

- [ ] **Step 4: Implement refresh interceptor**

Replace the full content of `lms_web/src/lib/api/client.ts`:

```ts
import axios, { type AxiosError, type AxiosRequestConfig, type InternalAxiosRequestConfig } from 'axios'
import { tokenStorage } from '@/lib/auth/token-storage'
import type { ApiError, ApiResponse, AuthErrorResponse } from './types'
import { endpoints } from './endpoints'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15_000,
})

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = tokenStorage.getAccessToken()
  if (token) {
    config.headers.set?.('Authorization', `Bearer ${token}`)
    ;(config.headers as Record<string, unknown>).Authorization = `Bearer ${token}`
  }
  return config
})

// Shared refresh promise to deduplicate concurrent 401s
let refreshPromise: Promise<string> | null = null

async function refreshAccessToken(): Promise<string> {
  const refreshToken = tokenStorage.getRefreshToken()
  if (!refreshToken) throw new Error('no refresh token')
  const res = await axios.post<{ accessToken: string }>(
    (import.meta.env.VITE_API_BASE_URL ?? '') + endpoints.auth.refresh,
    { refreshToken },
  )
  tokenStorage.setAccessToken(res.data.accessToken)
  return res.data.accessToken
}

type RetriableRequest = AxiosRequestConfig & { _retry?: boolean }

api.interceptors.response.use(
  (res) => res,
  async (err: AxiosError) => {
    const original = err.config as RetriableRequest | undefined
    const status = err.response?.status
    const isRefreshCall = original?.url?.endsWith(endpoints.auth.refresh)

    if (status === 401 && original && !original._retry && !isRefreshCall) {
      original._retry = true
      try {
        if (!refreshPromise) refreshPromise = refreshAccessToken().finally(() => { refreshPromise = null })
        const newToken = await refreshPromise
        original.headers = { ...(original.headers ?? {}), Authorization: `Bearer ${newToken}` } as never
        return api.request(original)
      } catch (refreshErr) {
        tokenStorage.clear()
        return Promise.reject(normalizeError(err))
      }
    }

    return Promise.reject(normalizeError(err))
  },
)

function normalizeError(err: AxiosError): ApiError {
  const status = err.response?.status ?? 0
  const data = err.response?.data as ApiResponse<unknown> | AuthErrorResponse | undefined
  if (data && typeof data === 'object') {
    if ('code' in data && typeof data.code === 'string') {
      return { status, code: data.code, message: data.message ?? '' }
    }
    if ('success' in data) {
      return { status, code: null, message: data.message ?? '' }
    }
  }
  return { status, code: null, message: err.message || '네트워크 오류가 발생했습니다.' }
}
```

- [ ] **Step 5: Run tests — expect pass**

```bash
npm run test:run -- src/lib/api/client.test.ts
```

Expected: 3 passed.

- [ ] **Step 6: Commit**

```bash
git add lms_web/
git commit -m "feat(lms_web): 401 refresh interceptor with shared refresh promise"
```

---

## Task 1.4: Auth Zustand store (persisted)

**Files:**
- Create: `lms_web/src/features/auth/store.ts`
- Test: `lms_web/src/features/auth/store.test.ts`

- [ ] **Step 1: Write failing tests**

Create `lms_web/src/features/auth/store.test.ts`:

```ts
import { describe, it, expect, beforeEach } from 'vitest'
import { useAuthStore, type Role } from './store'

const sampleUser = { userId: 'u1', email: 'e@x', role: 'EMPLOYEE' as Role, isActive: true }

describe('useAuthStore', () => {
  beforeEach(() => {
    useAuthStore.setState({ currentUser: null })
    localStorage.clear()
  })

  it('starts empty', () => {
    expect(useAuthStore.getState().currentUser).toBeNull()
  })

  it('setCurrentUser persists to localStorage', () => {
    useAuthStore.getState().setCurrentUser(sampleUser)
    expect(useAuthStore.getState().currentUser).toEqual(sampleUser)
    expect(localStorage.getItem('lms.auth')).toContain('u1')
  })

  it('logout clears state', () => {
    useAuthStore.getState().setCurrentUser(sampleUser)
    useAuthStore.getState().logout()
    expect(useAuthStore.getState().currentUser).toBeNull()
  })
})
```

- [ ] **Step 2: Run — expect fail**

```bash
npm run test:run -- src/features/auth/store.test.ts
```

- [ ] **Step 3: Implement**

Create `lms_web/src/features/auth/store.ts`:

```ts
import { create } from 'zustand'
import { persist, createJSONStorage } from 'zustand/middleware'
import { tokenStorage } from '@/lib/auth/token-storage'

export type Role = 'EMPLOYEE' | 'MANAGER' | 'SUPER_ADMIN'

export interface AuthUser {
  userId: string
  email: string
  role: Role
  isActive: boolean
}

interface AuthState {
  currentUser: AuthUser | null
  setCurrentUser: (user: AuthUser | null) => void
  logout: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      currentUser: null,
      setCurrentUser: (user) => set({ currentUser: user }),
      logout: () => {
        tokenStorage.clear()
        set({ currentUser: null })
      },
    }),
    {
      name: 'lms.auth',
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({ currentUser: state.currentUser }),
    },
  ),
)
```

- [ ] **Step 4: Run — expect pass**

```bash
npm run test:run -- src/features/auth/store.test.ts
```

Expected: 3 passed.

- [ ] **Step 5: Commit**

```bash
git add lms_web/src/features/auth/store.ts lms_web/src/features/auth/store.test.ts
git commit -m "feat(lms_web): auth zustand store with persist (currentUser only)"
```

---

## Task 1.5: Login schema + API hooks (TanStack Query)

**Files:**
- Create: `lms_web/src/features/auth/schema.ts`, `lms_web/src/features/auth/api.ts`

- [ ] **Step 1: Create Zod schema**

Create `lms_web/src/features/auth/schema.ts`:

```ts
import { z } from 'zod'

export const LoginSchema = z.object({
  email: z.string().min(1, '이메일을 입력해주세요.').email('올바른 이메일 형식이 아닙니다.'),
  password: z.string().min(1, '비밀번호를 입력해주세요.'),
})

export type LoginInput = z.infer<typeof LoginSchema>
```

- [ ] **Step 2: Create API hooks**

Create `lms_web/src/features/auth/api.ts`:

```ts
import { useMutation } from '@tanstack/react-query'
import { api } from '@/lib/api/client'
import { endpoints } from '@/lib/api/endpoints'
import { tokenStorage } from '@/lib/auth/token-storage'
import { useAuthStore, type AuthUser } from './store'
import type { LoginInput } from './schema'

interface LoginResponse {
  accessToken: string
  refreshToken: string
  userInfo: AuthUser
}

export function useLogin() {
  const setCurrentUser = useAuthStore((s) => s.setCurrentUser)
  return useMutation({
    mutationFn: async (input: LoginInput) => {
      const res = await api.post<LoginResponse>(endpoints.auth.login, input)
      return res.data
    },
    onSuccess: (data) => {
      tokenStorage.setTokens(data.accessToken, data.refreshToken)
      setCurrentUser(data.userInfo)
    },
  })
}

export function useLogout() {
  const logout = useAuthStore((s) => s.logout)
  return useMutation({
    mutationFn: async () => {
      try { await api.post(endpoints.auth.logout) } catch { /* ignore — client-side logout primacy */ }
    },
    onSettled: () => {
      logout()
    },
  })
}
```

- [ ] **Step 3: Typecheck**

```bash
cd lms_web && npm run typecheck
```

Expected: exits 0.

- [ ] **Step 4: Commit**

```bash
git add lms_web/src/features/auth/schema.ts lms_web/src/features/auth/api.ts
git commit -m "feat(lms_web): login/logout mutations and zod schema for auth form"
```

---

## Task 1.6: LoginPage with RHF + Zod + shadcn Form

**Files:**
- Create: `lms_web/src/features/auth/pages/LoginPage.tsx`

- [ ] **Step 1: Implement LoginPage**

Create `lms_web/src/features/auth/pages/LoginPage.tsx`:

```tsx
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useNavigate, useLocation } from 'react-router-dom'
import { toast } from 'sonner'
import { LoginSchema, type LoginInput } from '../schema'
import { useLogin } from '../api'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import {
  Form, FormControl, FormField, FormItem, FormLabel, FormMessage,
} from '@/components/ui/form'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { getErrorMessage } from '@/lib/utils/errors'

export default function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const from = (location.state as { from?: string } | null)?.from ?? '/home'
  const login = useLogin()

  const form = useForm<LoginInput>({
    resolver: zodResolver(LoginSchema),
    defaultValues: { email: '', password: '' },
  })

  const onSubmit = form.handleSubmit((values) => {
    login.mutate(values, {
      onSuccess: (data) => {
        const target = data.userInfo.role === 'EMPLOYEE' ? from : '/admin'
        navigate(target, { replace: true })
      },
      onError: (err) => {
        toast.error(getErrorMessage(err))
      },
    })
  })

  return (
    <div className="flex min-h-screen items-center justify-center bg-muted/40 px-4">
      <Card className="w-full max-w-sm">
        <CardHeader>
          <CardTitle className="text-center text-2xl">LMS 로그인</CardTitle>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={onSubmit} className="space-y-4">
              <FormField
                control={form.control}
                name="email"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>이메일</FormLabel>
                    <FormControl>
                      <Input type="email" autoComplete="email" placeholder="you@example.com" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="password"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>비밀번호</FormLabel>
                    <FormControl>
                      <Input type="password" autoComplete="current-password" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <Button type="submit" className="w-full" disabled={login.isPending}>
                {login.isPending ? '로그인 중...' : '로그인'}
              </Button>
            </form>
          </Form>
        </CardContent>
      </Card>
    </div>
  )
}
```

- [ ] **Step 2: Typecheck**

```bash
cd lms_web && npm run typecheck
```

Expected: exits 0.

- [ ] **Step 3: Commit**

```bash
git add lms_web/src/features/auth/pages/
git commit -m "feat(lms_web): LoginPage with RHF + zod validation and role-aware redirect"
```

---

# Phase M2 — Routing, Layouts, and Bootstrap

## Task 2.1: ProtectedRoute + useAuth hook

**Files:**
- Create: `lms_web/src/lib/auth/use-auth.ts`
- Create: `lms_web/src/lib/router/protected-route.tsx`
- Test: `lms_web/src/lib/router/protected-route.test.tsx`

- [ ] **Step 1: Create `use-auth.ts`**

Create `lms_web/src/lib/auth/use-auth.ts`:

```ts
import { useAuthStore, type Role } from '@/features/auth/store'
import { tokenStorage } from './token-storage'

export function useAuth() {
  const currentUser = useAuthStore((s) => s.currentUser)
  const hasToken = tokenStorage.getAccessToken() !== null
  const isAuthenticated = !!currentUser && hasToken
  const hasRole = (roles: Role[]): boolean => !!currentUser && roles.includes(currentUser.role)
  return { currentUser, isAuthenticated, hasRole }
}
```

- [ ] **Step 2: Write failing test for ProtectedRoute**

Create `lms_web/src/lib/router/protected-route.test.tsx`:

```tsx
import { describe, it, expect, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { useAuthStore } from '@/features/auth/store'
import { tokenStorage } from '@/lib/auth/token-storage'
import { ProtectedRoute } from './protected-route'

function Guarded({ roles }: { roles?: Array<'EMPLOYEE' | 'MANAGER' | 'SUPER_ADMIN'> }) {
  return (
    <MemoryRouter initialEntries={['/secret']}>
      <Routes>
        <Route path="/login" element={<div>LOGIN_PAGE</div>} />
        <Route path="/403" element={<div>FORBIDDEN_PAGE</div>} />
        <Route path="/secret" element={<ProtectedRoute roles={roles}><div>SECRET</div></ProtectedRoute>} />
      </Routes>
    </MemoryRouter>
  )
}

describe('ProtectedRoute', () => {
  beforeEach(() => {
    localStorage.clear()
    useAuthStore.setState({ currentUser: null })
  })

  it('redirects unauthenticated users to /login', () => {
    render(<Guarded />)
    expect(screen.getByText('LOGIN_PAGE')).toBeInTheDocument()
  })

  it('renders children when authenticated without role check', () => {
    tokenStorage.setTokens('a', 'r')
    useAuthStore.setState({ currentUser: { userId: 'u', email: 'e', role: 'EMPLOYEE', isActive: true } })
    render(<Guarded />)
    expect(screen.getByText('SECRET')).toBeInTheDocument()
  })

  it('redirects to /403 when role insufficient', () => {
    tokenStorage.setTokens('a', 'r')
    useAuthStore.setState({ currentUser: { userId: 'u', email: 'e', role: 'EMPLOYEE', isActive: true } })
    render(<Guarded roles={['MANAGER', 'SUPER_ADMIN']} />)
    expect(screen.getByText('FORBIDDEN_PAGE')).toBeInTheDocument()
  })
})
```

- [ ] **Step 3: Run — expect fail (module missing)**

```bash
npm run test:run -- src/lib/router/protected-route.test.tsx
```

- [ ] **Step 4: Implement**

Create `lms_web/src/lib/router/protected-route.tsx`:

```tsx
import { Navigate, Outlet, useLocation } from 'react-router-dom'
import type { ReactNode } from 'react'
import { useAuth } from '@/lib/auth/use-auth'
import type { Role } from '@/features/auth/store'

interface Props {
  children?: ReactNode
  roles?: Role[]
}

export function ProtectedRoute({ children, roles }: Props) {
  const location = useLocation()
  const { isAuthenticated, hasRole } = useAuth()

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />
  }
  if (roles && roles.length > 0 && !hasRole(roles)) {
    return <Navigate to="/403" replace />
  }
  return children ? <>{children}</> : <Outlet />
}
```

- [ ] **Step 5: Run — expect pass**

```bash
npm run test:run -- src/lib/router/protected-route.test.tsx
```

Expected: 3 passed.

- [ ] **Step 6: Commit**

```bash
git add lms_web/src/lib/auth/use-auth.ts lms_web/src/lib/router/
git commit -m "feat(lms_web): useAuth hook and ProtectedRoute with role-based guard"
```

---

## Task 2.2: QueryBoundary reusable component

**Files:**
- Create: `lms_web/src/components/layout/QueryBoundary.tsx`

- [ ] **Step 1: Implement**

Create `lms_web/src/components/layout/QueryBoundary.tsx`:

```tsx
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
```

- [ ] **Step 2: Typecheck**

```bash
cd lms_web && npm run typecheck
```

- [ ] **Step 3: Commit**

```bash
git add lms_web/src/components/layout/QueryBoundary.tsx
git commit -m "feat(lms_web): QueryBoundary — loading/error/empty helper for TanStack Query"
```

---

## Task 2.3: EmployeeShell layout (top header + bottom tab bar)

**Files:**
- Create: `lms_web/src/components/layout/EmployeeShell.tsx`

- [ ] **Step 1: Implement**

Create `lms_web/src/components/layout/EmployeeShell.tsx`:

```tsx
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { Home, Clock, Calendar, FileText, Wallet, LogOut } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { useAuthStore } from '@/features/auth/store'
import { useLogout } from '@/features/auth/api'
import { cn } from '@/lib/utils'

const tabs = [
  { to: '/home', label: '홈', icon: Home },
  { to: '/attendance', label: '출퇴근', icon: Clock },
  { to: '/schedule', label: '일정', icon: Calendar },
  { to: '/leave', label: '휴가', icon: FileText },
  { to: '/payroll', label: '급여', icon: Wallet },
] as const

export default function EmployeeShell() {
  const user = useAuthStore((s) => s.currentUser)
  const logoutMutation = useLogout()
  const navigate = useNavigate()

  const handleLogout = () => {
    logoutMutation.mutate(undefined, {
      onSettled: () => navigate('/login', { replace: true }),
    })
  }

  return (
    <div className="mx-auto flex min-h-screen max-w-xl flex-col bg-background">
      <header className="flex h-14 items-center justify-between border-b px-4">
        <span className="font-semibold">LMS</span>
        <div className="flex items-center gap-2 text-sm text-muted-foreground">
          <span>{user?.email}</span>
          <Button variant="ghost" size="icon" aria-label="로그아웃" onClick={handleLogout}>
            <LogOut className="h-4 w-4" />
          </Button>
        </div>
      </header>

      <main className="flex-1 overflow-y-auto p-4 pb-24">
        <Outlet />
      </main>

      <nav className="fixed bottom-0 left-1/2 w-full max-w-xl -translate-x-1/2 border-t bg-background">
        <ul className="grid grid-cols-5">
          {tabs.map(({ to, label, icon: Icon }) => (
            <li key={to}>
              <NavLink
                to={to}
                className={({ isActive }) =>
                  cn(
                    'flex flex-col items-center justify-center gap-0.5 py-2 text-xs',
                    isActive ? 'text-primary' : 'text-muted-foreground',
                  )
                }
              >
                <Icon className="h-5 w-5" />
                <span>{label}</span>
              </NavLink>
            </li>
          ))}
        </ul>
      </nav>
    </div>
  )
}
```

- [ ] **Step 2: Typecheck**

```bash
cd lms_web && npm run typecheck
```

Expected: exits 0.

- [ ] **Step 3: Commit**

```bash
git add lms_web/src/components/layout/EmployeeShell.tsx
git commit -m "feat(lms_web): EmployeeShell layout with 5-tab bottom nav + logout"
```

---

## Task 2.4: AdminShell layout (sidebar + top header)

**Files:**
- Create: `lms_web/src/components/layout/AdminShell.tsx`

- [ ] **Step 1: Implement**

Create `lms_web/src/components/layout/AdminShell.tsx`:

```tsx
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { LayoutDashboard, Users, Building2, CalendarDays, Clock, FileCheck2, Wallet, Settings2, LogOut } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Separator } from '@/components/ui/separator'
import { useAuthStore } from '@/features/auth/store'
import { useLogout } from '@/features/auth/api'
import { cn } from '@/lib/utils'

interface NavItem {
  to: string
  label: string
  icon: typeof LayoutDashboard
  roles?: Array<'MANAGER' | 'SUPER_ADMIN'>
}

const navItems: NavItem[] = [
  { to: '/admin', label: '대시보드', icon: LayoutDashboard },
  { to: '/admin/employees', label: '직원 관리', icon: Users },
  { to: '/admin/stores', label: '매장 관리', icon: Building2, roles: ['SUPER_ADMIN'] },
  { to: '/admin/schedules', label: '근무 일정', icon: CalendarDays },
  { to: '/admin/attendance', label: '근태 관리', icon: Clock },
  { to: '/admin/leaves', label: '휴가 관리', icon: FileCheck2 },
  { to: '/admin/payroll', label: '급여 관리', icon: Wallet },
  { to: '/admin/payroll/policies', label: '급여 정책', icon: Settings2, roles: ['SUPER_ADMIN'] },
]

export default function AdminShell() {
  const user = useAuthStore((s) => s.currentUser)
  const logoutMutation = useLogout()
  const navigate = useNavigate()

  const handleLogout = () => {
    logoutMutation.mutate(undefined, {
      onSettled: () => navigate('/login', { replace: true }),
    })
  }

  const visibleItems = navItems.filter(
    (item) => !item.roles || (user && item.roles.includes(user.role as 'MANAGER' | 'SUPER_ADMIN')),
  )

  return (
    <div className="flex min-h-screen bg-background">
      <aside className="hidden w-64 shrink-0 border-r bg-muted/30 md:flex md:flex-col">
        <div className="flex h-14 items-center border-b px-4 font-semibold">LMS 관리자</div>
        <nav className="flex-1 space-y-1 p-2">
          {visibleItems.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              end={to === '/admin'}
              className={({ isActive }) =>
                cn(
                  'flex items-center gap-2 rounded-md px-3 py-2 text-sm',
                  isActive ? 'bg-primary text-primary-foreground' : 'hover:bg-accent',
                )
              }
            >
              <Icon className="h-4 w-4" />
              {label}
            </NavLink>
          ))}
        </nav>
        <Separator />
        <div className="p-3 text-xs text-muted-foreground">{user?.email}</div>
      </aside>

      <div className="flex flex-1 flex-col">
        <header className="flex h-14 items-center justify-between border-b px-6">
          <span className="text-sm text-muted-foreground md:hidden">LMS 관리자</span>
          <div className="flex flex-1 items-center justify-end gap-3">
            <span className="text-sm text-muted-foreground">{user?.role}</span>
            <Button variant="ghost" size="icon" aria-label="로그아웃" onClick={handleLogout}>
              <LogOut className="h-4 w-4" />
            </Button>
          </div>
        </header>
        <main className="flex-1 overflow-y-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
```

Note: this plan defers a responsive Drawer for `md` on mobile — current spec marks admin mobile access as "auxiliary only". Sidebar is hidden under `md` breakpoint. A Drawer toggle can be added in Plan 3.

- [ ] **Step 2: Typecheck**

```bash
cd lms_web && npm run typecheck
```

- [ ] **Step 3: Commit**

```bash
git add lms_web/src/components/layout/AdminShell.tsx
git commit -m "feat(lms_web): AdminShell layout with role-filtered sidebar nav"
```

---

## Task 2.5: Placeholder pages for all 18 routes

**Files:**
- Create one tiny file per placeholder page under `lms_web/src/features/*/pages/*.tsx` for every route the plan's routes.tsx will reference.

All placeholder pages share a single body. We'll write a helper component and reuse.

- [ ] **Step 1: Create a shared placeholder component**

Create `lms_web/src/components/layout/Placeholder.tsx`:

```tsx
interface Props { title: string }
export default function Placeholder({ title }: Props) {
  return (
    <div className="space-y-2">
      <h1 className="text-xl font-semibold">{title}</h1>
      <p className="text-sm text-muted-foreground">이 페이지는 후속 마일스톤에서 구현됩니다.</p>
    </div>
  )
}
```

- [ ] **Step 2: Create employee-side placeholders**

Create the following files, each with the exact content shown below:

`lms_web/src/features/home/pages/HomePage.tsx`:
```tsx
import Placeholder from '@/components/layout/Placeholder'
export default function HomePage() { return <Placeholder title="홈 대시보드" /> }
```

`lms_web/src/features/attendance/pages/AttendancePage.tsx`:
```tsx
import Placeholder from '@/components/layout/Placeholder'
export default function AttendancePage() { return <Placeholder title="출퇴근" /> }
```

`lms_web/src/features/attendance/pages/AttendanceHistoryPage.tsx`:
```tsx
import Placeholder from '@/components/layout/Placeholder'
export default function AttendanceHistoryPage() { return <Placeholder title="근태 이력" /> }
```

`lms_web/src/features/schedule/pages/SchedulePage.tsx`:
```tsx
import Placeholder from '@/components/layout/Placeholder'
export default function SchedulePage() { return <Placeholder title="근무 일정" /> }
```

`lms_web/src/features/leave/pages/LeaveListPage.tsx`:
```tsx
import Placeholder from '@/components/layout/Placeholder'
export default function LeaveListPage() { return <Placeholder title="내 휴가" /> }
```

`lms_web/src/features/leave/pages/LeaveRequestPage.tsx`:
```tsx
import Placeholder from '@/components/layout/Placeholder'
export default function LeaveRequestPage() { return <Placeholder title="휴가 신청" /> }
```

`lms_web/src/features/payroll/pages/PayrollListPage.tsx`:
```tsx
import Placeholder from '@/components/layout/Placeholder'
export default function PayrollListPage() { return <Placeholder title="내 급여" /> }
```

`lms_web/src/features/payroll/pages/PayrollDetailPage.tsx`:
```tsx
import Placeholder from '@/components/layout/Placeholder'
import { useParams } from 'react-router-dom'
export default function PayrollDetailPage() {
  const { id } = useParams()
  return <Placeholder title={`급여 상세 #${id ?? ''}`} />
}
```

- [ ] **Step 3: Create admin-side placeholders**

`lms_web/src/features/admin/pages/AdminDashboardPage.tsx`:
```tsx
import Placeholder from '@/components/layout/Placeholder'
export default function AdminDashboardPage() { return <Placeholder title="관리자 대시보드" /> }
```

`lms_web/src/features/admin/pages/EmployeeManagementPage.tsx`:
```tsx
import Placeholder from '@/components/layout/Placeholder'
export default function EmployeeManagementPage() { return <Placeholder title="직원 관리" /> }
```

`lms_web/src/features/admin/pages/StoreManagementPage.tsx`:
```tsx
import Placeholder from '@/components/layout/Placeholder'
export default function StoreManagementPage() { return <Placeholder title="매장 관리" /> }
```

`lms_web/src/features/admin/pages/ScheduleManagementPage.tsx`:
```tsx
import Placeholder from '@/components/layout/Placeholder'
export default function ScheduleManagementPage() { return <Placeholder title="근무 일정 관리" /> }
```

`lms_web/src/features/admin/pages/AttendanceManagementPage.tsx`:
```tsx
import Placeholder from '@/components/layout/Placeholder'
export default function AttendanceManagementPage() { return <Placeholder title="근태 관리 (관리자)" /> }
```

`lms_web/src/features/admin/pages/LeaveManagementPage.tsx`:
```tsx
import Placeholder from '@/components/layout/Placeholder'
export default function LeaveManagementPage() { return <Placeholder title="휴가 승인" /> }
```

`lms_web/src/features/admin/pages/PayrollManagementPage.tsx`:
```tsx
import Placeholder from '@/components/layout/Placeholder'
export default function PayrollManagementPage() { return <Placeholder title="급여 관리" /> }
```

`lms_web/src/features/admin/pages/PayrollPolicyPage.tsx`:
```tsx
import Placeholder from '@/components/layout/Placeholder'
export default function PayrollPolicyPage() { return <Placeholder title="급여 정책" /> }
```

- [ ] **Step 4: Create public placeholders (403, not-found)**

`lms_web/src/features/common/pages/ForbiddenPage.tsx`:
```tsx
export default function ForbiddenPage() {
  return (
    <div className="flex min-h-screen items-center justify-center">
      <div className="text-center">
        <h1 className="text-2xl font-semibold">403 — 접근 권한 없음</h1>
        <p className="mt-2 text-muted-foreground">이 페이지에 접근할 권한이 없습니다.</p>
      </div>
    </div>
  )
}
```

`lms_web/src/features/common/pages/NotFoundPage.tsx`:
```tsx
export default function NotFoundPage() {
  return (
    <div className="flex min-h-screen items-center justify-center">
      <div className="text-center">
        <h1 className="text-2xl font-semibold">404 — 페이지를 찾을 수 없습니다</h1>
      </div>
    </div>
  )
}
```

- [ ] **Step 5: Typecheck**

```bash
cd lms_web && npm run typecheck
```

Expected: exits 0 (nothing is imported yet from routes.tsx so these are unused but valid).

- [ ] **Step 6: Commit**

```bash
git add lms_web/src/features/ lms_web/src/components/layout/Placeholder.tsx
git commit -m "feat(lms_web): placeholder pages for all 18 routes + 403/404"
```

---

## Task 2.6: Router definition (all routes wired with guards + shells)

**Files:**
- Create: `lms_web/src/lib/router/routes.tsx`

- [ ] **Step 1: Implement the router**

Create `lms_web/src/lib/router/routes.tsx`:

```tsx
import { Navigate, createBrowserRouter } from 'react-router-dom'
import { ProtectedRoute } from './protected-route'
import EmployeeShell from '@/components/layout/EmployeeShell'
import AdminShell from '@/components/layout/AdminShell'

import LoginPage from '@/features/auth/pages/LoginPage'
import ForbiddenPage from '@/features/common/pages/ForbiddenPage'
import NotFoundPage from '@/features/common/pages/NotFoundPage'

import HomePage from '@/features/home/pages/HomePage'
import AttendancePage from '@/features/attendance/pages/AttendancePage'
import AttendanceHistoryPage from '@/features/attendance/pages/AttendanceHistoryPage'
import SchedulePage from '@/features/schedule/pages/SchedulePage'
import LeaveListPage from '@/features/leave/pages/LeaveListPage'
import LeaveRequestPage from '@/features/leave/pages/LeaveRequestPage'
import PayrollListPage from '@/features/payroll/pages/PayrollListPage'
import PayrollDetailPage from '@/features/payroll/pages/PayrollDetailPage'

import AdminDashboardPage from '@/features/admin/pages/AdminDashboardPage'
import EmployeeManagementPage from '@/features/admin/pages/EmployeeManagementPage'
import StoreManagementPage from '@/features/admin/pages/StoreManagementPage'
import ScheduleManagementPage from '@/features/admin/pages/ScheduleManagementPage'
import AttendanceManagementPage from '@/features/admin/pages/AttendanceManagementPage'
import LeaveManagementPage from '@/features/admin/pages/LeaveManagementPage'
import PayrollManagementPage from '@/features/admin/pages/PayrollManagementPage'
import PayrollPolicyPage from '@/features/admin/pages/PayrollPolicyPage'

import { tokenStorage } from '@/lib/auth/token-storage'
import { useAuthStore } from '@/features/auth/store'

function RootRedirect() {
  const user = useAuthStore((s) => s.currentUser)
  const hasToken = tokenStorage.getAccessToken() !== null
  if (!hasToken || !user) return <Navigate to="/login" replace />
  return <Navigate to={user.role === 'EMPLOYEE' ? '/home' : '/admin'} replace />
}

export const router = createBrowserRouter([
  { path: '/', element: <RootRedirect /> },
  { path: '/login', element: <LoginPage /> },
  { path: '/403', element: <ForbiddenPage /> },

  {
    element: (
      <ProtectedRoute>
        <EmployeeShell />
      </ProtectedRoute>
    ),
    children: [
      { path: '/home', element: <HomePage /> },
      { path: '/attendance', element: <AttendancePage /> },
      { path: '/attendance/history', element: <AttendanceHistoryPage /> },
      { path: '/schedule', element: <SchedulePage /> },
      { path: '/leave', element: <LeaveListPage /> },
      { path: '/leave/request', element: <LeaveRequestPage /> },
      { path: '/payroll', element: <PayrollListPage /> },
      { path: '/payroll/:id', element: <PayrollDetailPage /> },
    ],
  },

  {
    element: (
      <ProtectedRoute roles={['MANAGER', 'SUPER_ADMIN']}>
        <AdminShell />
      </ProtectedRoute>
    ),
    children: [
      { path: '/admin', element: <AdminDashboardPage /> },
      { path: '/admin/employees', element: <EmployeeManagementPage /> },
      {
        path: '/admin/stores',
        element: (
          <ProtectedRoute roles={['SUPER_ADMIN']}>
            <StoreManagementPage />
          </ProtectedRoute>
        ),
      },
      { path: '/admin/schedules', element: <ScheduleManagementPage /> },
      { path: '/admin/attendance', element: <AttendanceManagementPage /> },
      { path: '/admin/leaves', element: <LeaveManagementPage /> },
      { path: '/admin/payroll', element: <PayrollManagementPage /> },
      {
        path: '/admin/payroll/policies',
        element: (
          <ProtectedRoute roles={['SUPER_ADMIN']}>
            <PayrollPolicyPage />
          </ProtectedRoute>
        ),
      },
    ],
  },

  { path: '*', element: <NotFoundPage /> },
])
```

- [ ] **Step 2: Typecheck**

```bash
cd lms_web && npm run typecheck
```

Expected: exits 0.

- [ ] **Step 3: Commit**

```bash
git add lms_web/src/lib/router/routes.tsx
git commit -m "feat(lms_web): router with 18 guarded routes + SUPER_ADMIN-only sub-routes"
```

---

## Task 2.7: Wire App.tsx (QueryClient + RouterProvider + Toaster)

**Files:**
- Modify: `lms_web/src/App.tsx`, `lms_web/src/main.tsx`

- [ ] **Step 1: Update `App.tsx`**

Overwrite `lms_web/src/App.tsx`:

```tsx
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { RouterProvider } from 'react-router-dom'
import { Toaster } from '@/components/ui/sonner'
import { router } from '@/lib/router/routes'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: (failureCount, error) => {
        const status = (error as { status?: number } | null)?.status
        if (status === 401 || status === 403) return false
        return failureCount < 2
      },
      refetchOnWindowFocus: false,
    },
  },
})

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
      <Toaster position="top-center" richColors />
    </QueryClientProvider>
  )
}
```

- [ ] **Step 2: Verify `main.tsx` mounts App**

Ensure `lms_web/src/main.tsx` contains (if different, align to this):

```tsx
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
```

- [ ] **Step 3: Typecheck and test suite**

```bash
cd lms_web
npm run typecheck
npm run test:run
```

Expected: both exit 0; all previously written tests pass.

- [ ] **Step 4: Commit**

```bash
git add lms_web/src/App.tsx lms_web/src/main.tsx
git commit -m "feat(lms_web): wire RouterProvider + QueryClient + global Toaster"
```

---

## Task 2.8: End-to-end dev smoke against live backend

This task is an interactive smoke test, not code. It confirms the foundation works against the real Spring Boot backend.

- [ ] **Step 1: Start MySQL and backend**

From repo root, in two terminals:
```bash
# Terminal A
docker-compose up -d
```
```bash
# Terminal B
./gradlew :interfaces:bootRun
```

Wait for backend to log `Started ... in ... seconds`.

- [ ] **Step 2: Start Vite dev server**

```bash
# Terminal C
cd lms_web && npm run dev
```

Open http://localhost:5173/login.

- [ ] **Step 3: Log in with a known demo account**

Use a demo account seeded by `DemoDataInitializer` or manual seed. If demo accounts are listed in `docs/ADMIN_LOGIN_TEST.md` or similar, use one there. Typical demo seed includes:
- EMPLOYEE: `emp-001` style account
- MANAGER: `emp-manager-001`
- SUPER_ADMIN: a super admin account

If unsure, read `docs/DEMO_SCENARIOS.md` or `infrastructure/src/main/kotlin/com/lms/infrastructure/config/DemoDataInitializer.kt` for credentials.

- [ ] **Step 4: Verify flows**

- [ ] Unauthenticated `GET /` → redirects to `/login`.
- [ ] Login with EMPLOYEE role → redirects to `/home`, bottom tab bar visible with 5 tabs.
- [ ] Click each of the 5 tabs → placeholder text loads, URL updates.
- [ ] Manually navigate `/admin` → redirects to `/403` (insufficient role).
- [ ] Logout → returns to `/login`, localStorage for `lms.access_token` / `lms.refresh_token` / `lms.auth` is cleared.
- [ ] Log in with MANAGER role → redirects to `/admin`, sidebar visible.
- [ ] Manually navigate `/admin/stores` → `/403` for MANAGER (SUPER_ADMIN-only).
- [ ] Log in with SUPER_ADMIN → all admin sidebar items clickable.
- [ ] Refresh the browser on any authenticated page → session persists (not redirected to login).

If any step fails, stop and fix before Task 2.9. Common culprits: CORS (shouldn't happen due to wildcard + proxy), incorrect env var, wrong baseURL.

- [ ] **Step 5: (Manual) simulate token expiry to verify refresh**

Manual verification that the refresh interceptor works end-to-end:
1. Log in.
2. Open DevTools → Application → Local Storage → edit `lms.access_token` to an invalid value like `EXPIRED`.
3. Navigate between tabs (trigger any authenticated request — later plans will add these; for now, manually call `api.get('/api/employees/<id>')` from DevTools console if needed, OR defer this step until Plan 2 when real API calls exist).

If no authenticated GET endpoint is wired yet at this plan's endpoint, the refresh interceptor is proven by Task 1.3's unit tests. Skip this step if manual verification is not feasible.

- [ ] **Step 6: No commit — smoke only**

---

## Task 2.9: README touch-up for lms_web

**Files:**
- Create: `lms_web/README.md`
- Modify: root `README.md` (add a short section pointing at `lms_web/`)

- [ ] **Step 1: Create `lms_web/README.md`**

Create `lms_web/README.md`:

```markdown
# LMS Web (React)

React port of the LMS web UI. Coexists with Flutter app in `lms_mobile_web/` (mobile) and shares the Spring Boot backend.

## Prerequisites
- Node.js 20+
- Running backend at http://localhost:8080 (`./gradlew :interfaces:bootRun` with the `local` profile)
- Running MySQL (via root `docker-compose up -d`)

## Development
```
npm install
npm run dev         # http://localhost:5173
npm run test        # vitest watch
npm run test:run    # vitest single-run
npm run typecheck
npm run build
```

API calls go through Vite proxy: `/api/*` → `http://localhost:8080/*`.

## Structure
See `docs/superpowers/specs/2026-04-22-lms-web-react-port-design.md` for architecture, and `docs/superpowers/plans/2026-04-22-lms-web-foundation.md` for the implementation plan.
```

- [ ] **Step 2: Add a short pointer in root README**

Edit `c:\Users\kitek\IdeaProjects\lms-demo\README.md`. Somewhere near the top-level web-related section, add:

```markdown
### Web Clients
- **Flutter** (`lms_mobile_web/`) — mobile-primary channel (iOS/Android + responsive web).
- **React** (`lms_web/`) — desktop-primary web channel. See `lms_web/README.md`.
```

(Place it near the existing project overview. Exact placement can be adjusted for readability, but the section must exist.)

- [ ] **Step 3: Commit**

```bash
git add lms_web/README.md README.md
git commit -m "docs: add lms_web README and mention React web channel in root README"
```

---

# Self-Review Checklist

Before closing this plan out, run all of these:

- [ ] **Typecheck**: `cd lms_web && npm run typecheck` exits 0.
- [ ] **Tests**: `cd lms_web && npm run test:run` — all suites pass (token-storage, client 401-refresh, auth store, ProtectedRoute).
- [ ] **Build**: `cd lms_web && npm run build` succeeds and produces `dist/`.
- [ ] **Git log clean**: `git log --oneline -20` shows one commit per completed task (not a single giant commit).
- [ ] **No placeholder content leaked**: no `TODO` / `TBD` in committed code.
- [ ] **Spec coverage** (cross-reference `docs/superpowers/specs/2026-04-22-lms-web-react-port-design.md`):
  - §3.1, §3.2 Directory structure — reflected in Tasks 0.1–2.7. ✅
  - §4.1 Token storage — Task 1.1. ✅
  - §4.2 Interceptor with shared refresh promise — Task 1.3. ✅
  - §4.3 Bootstrap flow — partially. RootRedirect in Task 2.6 handles the "token+user present" branch; Plan 2/3 extends with real data fetches. ✅ for foundation.
  - §4.4 Protected Route with roles — Task 2.1. ✅
  - §4.5 CORS via Vite proxy — Task 0.3. ✅
  - §5 Routing + 2-Shell layouts — Tasks 2.3, 2.4, 2.6. ✅
  - §6.3 QueryClient defaults — Task 2.7. ✅
  - §6.5 QueryBoundary — Task 2.2. ✅
  - §6.6 Form pattern — Task 1.6 (LoginPage demonstrates the pattern). ✅
  - §10 Milestones M0/M1/M2 — this plan covers them entirely. ✅
- [ ] **Out of scope verification**: M3 (employee features), M4 (admin features), M5 (polish), M6 (E2E) are NOT in this plan. They are Plans 2/3/4.

---

# Backend Spec Corrections (updated from verification)

While writing this plan, the following spec assumptions were verified or corrected. Source spec should be updated before later plans:

1. **CORS**: already open (`allowedOriginPatterns = ["*"]` in `SecurityConfig`). Spec §4.5 and §9.1 describe this as optionally needed — it's not. Vite proxy is used for other reasons (same-origin cookies, etc.).
2. **`/api/auth/me`**: does NOT exist. Spec §4.1 mentions `useMe` — there is no such endpoint. This plan uses `LoginResponse.userInfo` + Zustand `persist` instead.
3. **Refresh response shape**: `{ accessToken }` only, NOT `{ accessToken, refreshToken }`. Interceptor must NOT overwrite the refresh token on refresh.
4. **AuthController error shape**: RAW `{ code, message, timestamp }` — does NOT use `ApiResponse` envelope. Other controllers DO use `ApiResponse<T>`. The `normalizeError` helper handles both.

Plans 2/3/4 authors should consume these corrections.

---

# Execution Handoff

**Plan complete and saved to `docs/superpowers/plans/2026-04-22-lms-web-foundation.md`. Two execution options:**

**1. Subagent-Driven (recommended)** — I dispatch a fresh subagent per task, review between tasks, fast iteration.

**2. Inline Execution** — Execute tasks in this session using executing-plans, batch execution with checkpoints.

**Which approach?**
