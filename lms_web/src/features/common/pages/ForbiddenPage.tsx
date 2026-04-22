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
