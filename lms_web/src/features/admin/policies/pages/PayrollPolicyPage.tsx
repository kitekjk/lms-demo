import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { toast } from 'sonner'
import { MoreHorizontal } from 'lucide-react'
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import {
  Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle, DialogTrigger,
} from '@/components/ui/dialog'
import {
  DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from '@/components/ui/select'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { QueryBoundary } from '@/components/layout/QueryBoundary'
import {
  usePolicies, useCreatePolicy, useUpdatePolicy, useDeletePolicy,
} from '../api'
import {
  PolicyCreateSchema, PolicyUpdateSchema,
  type PolicyCreateInput, type PolicyUpdateInput,
} from '../schema'
import type { PayrollPolicy } from '../types'
import { policyTypeLabel, policyTypeLabels } from '@/lib/utils/labels'
import { formatDateKorean } from '@/lib/utils/date'
import { getErrorMessage } from '@/lib/utils/errors'

const policyTypes = Object.keys(policyTypeLabels) as Array<keyof typeof policyTypeLabels>

export default function PayrollPolicyPage() {
  const query = usePolicies()
  const [createOpen, setCreateOpen] = useState(false)
  const [editItem, setEditItem] = useState<PayrollPolicy | null>(null)

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">급여 정책</h1>
        <Dialog open={createOpen} onOpenChange={setCreateOpen}>
          <DialogTrigger asChild><Button>+ 정책 추가</Button></DialogTrigger>
          <CreateDialog onDone={() => setCreateOpen(false)} />
        </Dialog>
      </div>

      <QueryBoundary
        query={query}
        emptyMessage="등록된 정책이 없습니다."
        isEmpty={(d) => d.policies.length === 0}
      >
        {(data) => (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>유형</TableHead>
                <TableHead>배수</TableHead>
                <TableHead>효력 기간</TableHead>
                <TableHead>상태</TableHead>
                <TableHead className="w-[80px]"></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {data.policies.map((p) => (
                <TableRow key={p.id}>
                  <TableCell>{policyTypeLabel(p.policyType)}</TableCell>
                  <TableCell>× {p.multiplier}</TableCell>
                  <TableCell className="text-xs">
                    {formatDateKorean(p.effectiveFrom)} ~ {p.effectiveTo ? formatDateKorean(p.effectiveTo) : '(무기한)'}
                  </TableCell>
                  <TableCell>
                    {p.isCurrentlyEffective
                      ? <Badge variant="outline">적용 중</Badge>
                      : <Badge variant="secondary">비적용</Badge>}
                  </TableCell>
                  <TableCell>
                    <RowActions policy={p} onEdit={() => setEditItem(p)} />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </QueryBoundary>

      <Dialog open={!!editItem} onOpenChange={(o) => !o && setEditItem(null)}>
        {editItem && <EditDialog policy={editItem} onDone={() => setEditItem(null)} />}
      </Dialog>
    </div>
  )
}

function RowActions({ policy, onEdit }: { policy: PayrollPolicy; onEdit: () => void }) {
  const del = useDeletePolicy()
  const onDelete = () => {
    if (!confirm('이 정책을 삭제하시겠습니까?')) return
    del.mutate(policy.id, {
      onSuccess: () => toast.success('삭제되었습니다.'),
      onError: (e) => toast.error(getErrorMessage(e)),
    })
  }
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild><Button variant="ghost" size="icon"><MoreHorizontal className="h-4 w-4" /></Button></DropdownMenuTrigger>
      <DropdownMenuContent align="end">
        <DropdownMenuItem onClick={onEdit}>수정</DropdownMenuItem>
        <DropdownMenuItem onClick={onDelete} className="text-destructive">삭제</DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}

function CreateDialog({ onDone }: { onDone: () => void }) {
  const create = useCreatePolicy()
  const form = useForm<PolicyCreateInput>({
    resolver: zodResolver(PolicyCreateSchema),
    defaultValues: { policyType: 'OVERTIME_WEEKDAY', multiplier: 1.5, effectiveFrom: '', effectiveTo: '', description: '' },
  })
  const onSubmit = form.handleSubmit((values) => {
    create.mutate(values, {
      onSuccess: () => { toast.success('정책이 추가되었습니다.'); onDone() },
      onError: (e) => form.setError('root', { message: getErrorMessage(e) }),
    })
  })
  return (
    <DialogContent>
      <DialogHeader><DialogTitle>신규 정책</DialogTitle></DialogHeader>
      <Form {...form}>
        <form onSubmit={onSubmit} className="space-y-4">
          <FormField control={form.control} name="policyType" render={({ field }) => (
            <FormItem>
              <FormLabel>정책 유형</FormLabel>
              <Select value={field.value} onValueChange={field.onChange}>
                <FormControl><SelectTrigger><SelectValue /></SelectTrigger></FormControl>
                <SelectContent>
                  {policyTypes.map((t) => <SelectItem key={t} value={t}>{policyTypeLabel(t)}</SelectItem>)}
                </SelectContent>
              </Select>
              <FormMessage />
            </FormItem>
          )} />
          <FormField control={form.control} name="multiplier" render={({ field }) => (
            <FormItem><FormLabel>배수 (0.0 ~ 10.0)</FormLabel><FormControl><Input type="number" step="0.1" {...field} onChange={(e) => field.onChange(e.target.valueAsNumber)} /></FormControl><FormMessage /></FormItem>
          )} />
          <FormField control={form.control} name="effectiveFrom" render={({ field }) => (
            <FormItem><FormLabel>시작일</FormLabel><FormControl><Input type="date" {...field} /></FormControl><FormMessage /></FormItem>
          )} />
          <FormField control={form.control} name="effectiveTo" render={({ field }) => (
            <FormItem><FormLabel>종료일 (선택)</FormLabel><FormControl><Input type="date" {...field} /></FormControl><FormMessage /></FormItem>
          )} />
          <FormField control={form.control} name="description" render={({ field }) => (
            <FormItem><FormLabel>설명 (선택)</FormLabel><FormControl><Input {...field} /></FormControl><FormMessage /></FormItem>
          )} />
          {form.formState.errors.root && <p className="text-sm text-destructive">{form.formState.errors.root.message}</p>}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={onDone}>취소</Button>
            <Button type="submit" disabled={create.isPending}>{create.isPending ? '저장 중...' : '저장'}</Button>
          </DialogFooter>
        </form>
      </Form>
    </DialogContent>
  )
}

function EditDialog({ policy, onDone }: { policy: PayrollPolicy; onDone: () => void }) {
  const update = useUpdatePolicy()
  const form = useForm<PolicyUpdateInput>({
    resolver: zodResolver(PolicyUpdateSchema),
    defaultValues: { multiplier: policy.multiplier, effectiveTo: policy.effectiveTo ?? '', description: policy.description ?? '' },
  })
  const onSubmit = form.handleSubmit((values) => {
    update.mutate({ id: policy.id, input: values }, {
      onSuccess: () => { toast.success('정책이 수정되었습니다.'); onDone() },
      onError: (e) => form.setError('root', { message: getErrorMessage(e) }),
    })
  })
  return (
    <DialogContent>
      <DialogHeader><DialogTitle>정책 수정</DialogTitle></DialogHeader>
      <Form {...form}>
        <form onSubmit={onSubmit} className="space-y-4">
          <div className="text-sm text-muted-foreground">유형: {policyTypeLabel(policy.policyType)} (변경 불가)</div>
          <FormField control={form.control} name="multiplier" render={({ field }) => (
            <FormItem><FormLabel>배수</FormLabel><FormControl><Input type="number" step="0.1" {...field} onChange={(e) => field.onChange(e.target.valueAsNumber)} /></FormControl><FormMessage /></FormItem>
          )} />
          <FormField control={form.control} name="effectiveTo" render={({ field }) => (
            <FormItem><FormLabel>종료일</FormLabel><FormControl><Input type="date" {...field} /></FormControl><FormMessage /></FormItem>
          )} />
          <FormField control={form.control} name="description" render={({ field }) => (
            <FormItem><FormLabel>설명</FormLabel><FormControl><Input {...field} /></FormControl><FormMessage /></FormItem>
          )} />
          {form.formState.errors.root && <p className="text-sm text-destructive">{form.formState.errors.root.message}</p>}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={onDone}>취소</Button>
            <Button type="submit" disabled={update.isPending}>{update.isPending ? '저장 중...' : '저장'}</Button>
          </DialogFooter>
        </form>
      </Form>
    </DialogContent>
  )
}
