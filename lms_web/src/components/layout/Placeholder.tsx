interface Props { title: string }
export default function Placeholder({ title }: Props) {
  return (
    <div className="space-y-2">
      <h1 className="text-xl font-semibold">{title}</h1>
      <p className="text-sm text-muted-foreground">이 페이지는 후속 마일스톤에서 구현됩니다.</p>
    </div>
  )
}
