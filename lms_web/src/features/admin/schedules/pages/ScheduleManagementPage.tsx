import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { toast } from 'sonner'
import { MoreHorizontal } from 'lucide-react'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { QueryBoundary } from '@/components/layout/QueryBoundary'
import {
  useAdminSchedules,
  useCreateSchedule,
  useUpdateSchedule,
  useDeleteSchedule,
} from '../api'
import {
  ScheduleCreateSchema,
  ScheduleUpdateSchema,
  type ScheduleCreateInput,
  type ScheduleUpdateInput,
} from '../schema'
import { useStores } from '@/features/admin/stores/api'
import { useEmployees } from '@/features/admin/employees/api'
import type { WorkSchedule } from '@/features/schedule/types'
import { formatDate, formatDateKorean, formatTime } from '@/lib/utils/date'
import { getErrorMessage } from '@/lib/utils/errors'

function firstOfMonth(d: Date): Date {
  const x = new Date(d)
  x.setDate(1)
  return x
}

export default function ScheduleManagementPage() {
  const [storeId, setStoreId] = useState<string | undefined>(undefined)
  const today = new Date()
  const [startDate, setStartDate] = useState(formatDate(firstOfMonth(today)))
  const [endDate, setEndDate] = useState(
    formatDate(new Date(today.getFullYear(), today.getMonth() + 1, 0)),
  )
  const stores = useStores()
  const query = useAdminSchedules({ storeId, startDate, endDate })
  const [createOpen, setCreateOpen] = useState(false)
  const [editItem, setEditItem] = useState<WorkSchedule | null>(null)

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">근무 일정 관리</h1>
        <Dialog open={createOpen} onOpenChange={setCreateOpen}>
          <DialogTrigger asChild>
            <Button>+ 일정 추가</Button>
          </DialogTrigger>
          <CreateDialog onDone={() => setCreateOpen(false)} />
        </Dialog>
      </div>

      <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
        <div className="space-y-1">
          <Label>매장</Label>
          <Select
            value={storeId ?? '__all'}
            onValueChange={(v) => setStoreId(v === '__all' ? undefined : v)}
          >
            <SelectTrigger>
              <SelectValue placeholder="매장 선택" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="__all">(전체)</SelectItem>
              {stores.data?.stores.map((s) => (
                <SelectItem key={s.id} value={s.id}>
                  {s.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div className="space-y-1">
          <Label>시작일</Label>
          <Input
            type="date"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
          />
        </div>
        <div className="space-y-1">
          <Label>종료일</Label>
          <Input
            type="date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
          />
        </div>
      </div>

      {!storeId ? (
        <p className="text-center text-sm text-muted-foreground">매장을 선택해주세요.</p>
      ) : (
        <QueryBoundary
          query={query}
          emptyMessage="해당 기간 일정이 없습니다."
          isEmpty={(d) => d.schedules.length === 0}
        >
          {(data) => (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>직원</TableHead>
                  <TableHead>날짜</TableHead>
                  <TableHead>시간</TableHead>
                  <TableHead>시수</TableHead>
                  <TableHead className="w-[80px]"></TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {data.schedules.map((s) => (
                  <TableRow key={s.id}>
                    <TableCell className="font-mono text-xs">{s.employeeId}</TableCell>
                    <TableCell>{formatDateKorean(s.workDate)}</TableCell>
                    <TableCell>
                      {formatTime(s.startTime)} ~ {formatTime(s.endTime)}
                    </TableCell>
                    <TableCell>{s.workHours}h</TableCell>
                    <TableCell>
                      <RowActions schedule={s} onEdit={() => setEditItem(s)} />
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </QueryBoundary>
      )}

      <Dialog open={!!editItem} onOpenChange={(o) => !o && setEditItem(null)}>
        {editItem && <EditDialog schedule={editItem} onDone={() => setEditItem(null)} />}
      </Dialog>
    </div>
  )
}

function RowActions({
  schedule,
  onEdit,
}: {
  schedule: WorkSchedule
  onEdit: () => void
}) {
  const del = useDeleteSchedule()
  const onDelete = () => {
    if (!confirm(`${schedule.workDate} 일정을 삭제하시겠습니까?`)) return
    del.mutate(schedule.id, {
      onSuccess: () => toast.success('삭제되었습니다.'),
      onError: (e) => toast.error(getErrorMessage(e)),
    })
  }
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" size="icon">
          <MoreHorizontal className="h-4 w-4" />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end">
        <DropdownMenuItem onClick={onEdit}>수정</DropdownMenuItem>
        <DropdownMenuItem onClick={onDelete} className="text-destructive">
          삭제
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}

function CreateDialog({ onDone }: { onDone: () => void }) {
  const create = useCreateSchedule()
  const stores = useStores()
  const employees = useEmployees({ activeOnly: true })
  const form = useForm<ScheduleCreateInput>({
    resolver: zodResolver(ScheduleCreateSchema),
    defaultValues: {
      employeeId: '',
      storeId: '',
      workDate: '',
      startTime: '09:00',
      endTime: '18:00',
    },
  })
  const onSubmit = form.handleSubmit((values) => {
    create.mutate(values, {
      onSuccess: () => {
        toast.success('일정이 추가되었습니다.')
        onDone()
      },
      onError: (e) => form.setError('root', { message: getErrorMessage(e) }),
    })
  })
  return (
    <DialogContent>
      <DialogHeader>
        <DialogTitle>신규 일정</DialogTitle>
      </DialogHeader>
      <Form {...form}>
        <form onSubmit={onSubmit} className="space-y-4">
          <FormField
            control={form.control}
            name="employeeId"
            render={({ field }) => (
              <FormItem>
                <FormLabel>직원</FormLabel>
                <Select value={field.value} onValueChange={field.onChange}>
                  <FormControl>
                    <SelectTrigger>
                      <SelectValue placeholder="직원 선택" />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent>
                    {employees.data?.employees.map((e) => (
                      <SelectItem key={e.id} value={e.id}>
                        {e.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name="storeId"
            render={({ field }) => (
              <FormItem>
                <FormLabel>매장</FormLabel>
                <Select value={field.value} onValueChange={field.onChange}>
                  <FormControl>
                    <SelectTrigger>
                      <SelectValue placeholder="매장 선택" />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent>
                    {stores.data?.stores.map((s) => (
                      <SelectItem key={s.id} value={s.id}>
                        {s.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name="workDate"
            render={({ field }) => (
              <FormItem>
                <FormLabel>날짜</FormLabel>
                <FormControl>
                  <Input type="date" {...field} />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <div className="grid grid-cols-2 gap-3">
            <FormField
              control={form.control}
              name="startTime"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>시작시간</FormLabel>
                  <FormControl>
                    <Input type="time" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="endTime"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>종료시간</FormLabel>
                  <FormControl>
                    <Input type="time" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
          </div>
          {form.formState.errors.root && (
            <p className="text-sm text-destructive">{form.formState.errors.root.message}</p>
          )}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={onDone}>
              취소
            </Button>
            <Button type="submit" disabled={create.isPending}>
              {create.isPending ? '저장 중...' : '저장'}
            </Button>
          </DialogFooter>
        </form>
      </Form>
    </DialogContent>
  )
}

function EditDialog({
  schedule,
  onDone,
}: {
  schedule: WorkSchedule
  onDone: () => void
}) {
  const update = useUpdateSchedule()
  const form = useForm<ScheduleUpdateInput>({
    resolver: zodResolver(ScheduleUpdateSchema),
    defaultValues: {
      workDate: schedule.workDate,
      startTime: schedule.startTime.slice(0, 5),
      endTime: schedule.endTime.slice(0, 5),
    },
  })
  const onSubmit = form.handleSubmit((values) => {
    update.mutate(
      { id: schedule.id, input: values },
      {
        onSuccess: () => {
          toast.success('일정이 수정되었습니다.')
          onDone()
        },
        onError: (e) => form.setError('root', { message: getErrorMessage(e) }),
      },
    )
  })
  return (
    <DialogContent>
      <DialogHeader>
        <DialogTitle>일정 수정</DialogTitle>
      </DialogHeader>
      <Form {...form}>
        <form onSubmit={onSubmit} className="space-y-4">
          <FormField
            control={form.control}
            name="workDate"
            render={({ field }) => (
              <FormItem>
                <FormLabel>날짜</FormLabel>
                <FormControl>
                  <Input type="date" {...field} />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <div className="grid grid-cols-2 gap-3">
            <FormField
              control={form.control}
              name="startTime"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>시작시간</FormLabel>
                  <FormControl>
                    <Input type="time" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="endTime"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>종료시간</FormLabel>
                  <FormControl>
                    <Input type="time" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
          </div>
          {form.formState.errors.root && (
            <p className="text-sm text-destructive">{form.formState.errors.root.message}</p>
          )}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={onDone}>
              취소
            </Button>
            <Button type="submit" disabled={update.isPending}>
              {update.isPending ? '저장 중...' : '저장'}
            </Button>
          </DialogFooter>
        </form>
      </Form>
    </DialogContent>
  )
}
