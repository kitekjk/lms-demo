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
  useEmployees, useCreateEmployee, useUpdateEmployee, useDeactivateEmployee,
} from '../api'
import {
  EmployeeCreateSchema, EmployeeUpdateSchema,
  type EmployeeCreateInput, type EmployeeUpdateInput,
} from '../schema'
import type { AdminEmployee } from '../types'
import { useStores } from '@/features/admin/stores/api'
import { employeeTypeLabel, employeeTypeLabels } from '@/lib/utils/labels'
import { getErrorMessage } from '@/lib/utils/errors'

const employeeTypes = Object.keys(employeeTypeLabels) as Array<keyof typeof employeeTypeLabels>

export default function EmployeeManagementPage() {
  const query = useEmployees()
  const [createOpen, setCreateOpen] = useState(false)
  const [editItem, setEditItem] = useState<AdminEmployee | null>(null)

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">직원 관리</h1>
        <Dialog open={createOpen} onOpenChange={setCreateOpen}>
          <DialogTrigger asChild><Button>+ 직원 추가</Button></DialogTrigger>
          <CreateDialog onDone={() => setCreateOpen(false)} />
        </Dialog>
      </div>

      <QueryBoundary
        query={query}
        emptyMessage="등록된 직원이 없습니다."
        isEmpty={(d) => d.employees.length === 0}
      >
        {(data) => (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>이름</TableHead>
                <TableHead>유형</TableHead>
                <TableHead>매장</TableHead>
                <TableHead>남은 연차</TableHead>
                <TableHead>상태</TableHead>
                <TableHead className="w-[80px]"></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {data.employees.map((e) => (
                <TableRow key={e.id}>
                  <TableCell className="font-medium">{e.name}</TableCell>
                  <TableCell>{employeeTypeLabel(e.employeeType)}</TableCell>
                  <TableCell className="font-mono text-xs">{e.storeId ?? '—'}</TableCell>
                  <TableCell>{e.remainingLeave}</TableCell>
                  <TableCell>
                    {e.isActive
                      ? <Badge variant="outline">활성</Badge>
                      : <Badge variant="secondary">비활성</Badge>}
                  </TableCell>
                  <TableCell>
                    {e.isActive && <RowActions emp={e} onEdit={() => setEditItem(e)} />}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </QueryBoundary>

      <Dialog open={!!editItem} onOpenChange={(o) => !o && setEditItem(null)}>
        {editItem && <EditDialog emp={editItem} onDone={() => setEditItem(null)} />}
      </Dialog>
    </div>
  )
}

function RowActions({ emp, onEdit }: { emp: AdminEmployee; onEdit: () => void }) {
  const deact = useDeactivateEmployee()
  const onDeactivate = () => {
    if (!confirm(`${emp.name}을(를) 비활성화하시겠습니까?`)) return
    deact.mutate(emp.id, {
      onSuccess: () => toast.success('비활성화되었습니다.'),
      onError: (e) => toast.error(getErrorMessage(e)),
    })
  }
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" size="icon"><MoreHorizontal className="h-4 w-4" /></Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end">
        <DropdownMenuItem onClick={onEdit}>수정</DropdownMenuItem>
        <DropdownMenuItem onClick={onDeactivate} className="text-destructive">비활성화</DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}

function StoreSelect({ value, onChange }: { value?: string; onChange: (v: string | undefined) => void }) {
  const stores = useStores()
  return (
    <Select value={value ?? '__none'} onValueChange={(v) => onChange(v === '__none' ? undefined : v)}>
      <SelectTrigger><SelectValue placeholder="매장 선택" /></SelectTrigger>
      <SelectContent>
        <SelectItem value="__none">(없음)</SelectItem>
        {stores.data?.stores.map((s) => (
          <SelectItem key={s.id} value={s.id}>{s.name}</SelectItem>
        ))}
      </SelectContent>
    </Select>
  )
}

function CreateDialog({ onDone }: { onDone: () => void }) {
  const create = useCreateEmployee()
  const form = useForm<EmployeeCreateInput>({
    resolver: zodResolver(EmployeeCreateSchema),
    defaultValues: { userId: '', name: '', employeeType: 'REGULAR', storeId: undefined },
  })
  const onSubmit = form.handleSubmit((values) => {
    create.mutate(values, {
      onSuccess: () => { toast.success('직원이 등록되었습니다.'); onDone() },
      onError: (e) => form.setError('root', { message: getErrorMessage(e) }),
    })
  })
  return (
    <DialogContent>
      <DialogHeader><DialogTitle>신규 직원</DialogTitle></DialogHeader>
      <Form {...form}>
        <form onSubmit={onSubmit} className="space-y-4">
          <FormField control={form.control} name="userId" render={({ field }) => (
            <FormItem>
              <FormLabel>사용자 ID</FormLabel>
              <FormControl><Input {...field} /></FormControl>
              <FormMessage />
            </FormItem>
          )} />
          <FormField control={form.control} name="name" render={({ field }) => (
            <FormItem>
              <FormLabel>이름</FormLabel>
              <FormControl><Input {...field} /></FormControl>
              <FormMessage />
            </FormItem>
          )} />
          <FormField control={form.control} name="employeeType" render={({ field }) => (
            <FormItem>
              <FormLabel>유형</FormLabel>
              <Select value={field.value} onValueChange={field.onChange}>
                <FormControl><SelectTrigger><SelectValue /></SelectTrigger></FormControl>
                <SelectContent>
                  {employeeTypes.map((t) => (
                    <SelectItem key={t} value={t}>{employeeTypeLabel(t)}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <FormMessage />
            </FormItem>
          )} />
          <FormField control={form.control} name="storeId" render={({ field }) => (
            <FormItem>
              <FormLabel>매장</FormLabel>
              <StoreSelect value={field.value} onChange={field.onChange} />
              <FormMessage />
            </FormItem>
          )} />
          {form.formState.errors.root && <p className="text-sm text-destructive">{form.formState.errors.root.message}</p>}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={onDone}>취소</Button>
            <Button type="submit" disabled={create.isPending}>{create.isPending ? '등록 중...' : '등록'}</Button>
          </DialogFooter>
        </form>
      </Form>
    </DialogContent>
  )
}

function EditDialog({ emp, onDone }: { emp: AdminEmployee; onDone: () => void }) {
  const update = useUpdateEmployee()
  const form = useForm<EmployeeUpdateInput>({
    resolver: zodResolver(EmployeeUpdateSchema),
    defaultValues: { name: emp.name, employeeType: emp.employeeType, storeId: emp.storeId ?? undefined },
  })
  const onSubmit = form.handleSubmit((values) => {
    update.mutate({ id: emp.id, input: values }, {
      onSuccess: () => { toast.success('직원 정보가 수정되었습니다.'); onDone() },
      onError: (e) => form.setError('root', { message: getErrorMessage(e) }),
    })
  })
  return (
    <DialogContent>
      <DialogHeader><DialogTitle>직원 수정</DialogTitle></DialogHeader>
      <Form {...form}>
        <form onSubmit={onSubmit} className="space-y-4">
          <FormField control={form.control} name="name" render={({ field }) => (
            <FormItem><FormLabel>이름</FormLabel><FormControl><Input {...field} /></FormControl><FormMessage /></FormItem>
          )} />
          <FormField control={form.control} name="employeeType" render={({ field }) => (
            <FormItem>
              <FormLabel>유형</FormLabel>
              <Select value={field.value} onValueChange={field.onChange}>
                <FormControl><SelectTrigger><SelectValue /></SelectTrigger></FormControl>
                <SelectContent>
                  {employeeTypes.map((t) => (<SelectItem key={t} value={t}>{employeeTypeLabel(t)}</SelectItem>))}
                </SelectContent>
              </Select>
              <FormMessage />
            </FormItem>
          )} />
          <FormField control={form.control} name="storeId" render={({ field }) => (
            <FormItem><FormLabel>매장</FormLabel><StoreSelect value={field.value} onChange={field.onChange} /><FormMessage /></FormItem>
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
