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
import {
  Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle, DialogTrigger,
} from '@/components/ui/dialog'
import {
  DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { QueryBoundary } from '@/components/layout/QueryBoundary'
import { useStores, useCreateStore, useUpdateStore, useDeleteStore } from '../api'
import { StoreSchema, type StoreInput } from '../schema'
import type { Store } from '../types'
import { getErrorMessage } from '@/lib/utils/errors'

export default function StoreManagementPage() {
  const query = useStores()
  const [createOpen, setCreateOpen] = useState(false)
  const [editItem, setEditItem] = useState<Store | null>(null)

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">매장 관리</h1>
        <Dialog open={createOpen} onOpenChange={setCreateOpen}>
          <DialogTrigger asChild>
            <Button>+ 매장 추가</Button>
          </DialogTrigger>
          <StoreFormDialog title="신규 매장" mode="create" onDone={() => setCreateOpen(false)} />
        </Dialog>
      </div>

      <QueryBoundary
        query={query}
        emptyMessage="등록된 매장이 없습니다."
        isEmpty={(d) => d.stores.length === 0}
      >
        {(data) => (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>이름</TableHead>
                <TableHead>위치</TableHead>
                <TableHead className="w-[80px]"></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {data.stores.map((s) => (
                <TableRow key={s.id}>
                  <TableCell className="font-medium">{s.name}</TableCell>
                  <TableCell className="text-muted-foreground">{s.location}</TableCell>
                  <TableCell>
                    <RowActions store={s} onEdit={() => setEditItem(s)} />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </QueryBoundary>

      <Dialog open={!!editItem} onOpenChange={(o) => !o && setEditItem(null)}>
        {editItem && <StoreFormDialog title="매장 수정" mode="edit" initial={editItem} onDone={() => setEditItem(null)} />}
      </Dialog>
    </div>
  )
}

function RowActions({ store, onEdit }: { store: Store; onEdit: () => void }) {
  const del = useDeleteStore()
  const onDelete = () => {
    if (!confirm(`${store.name} 매장을 삭제하시겠습니까?`)) return
    del.mutate(store.id, {
      onSuccess: () => toast.success('삭제되었습니다.'),
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
        <DropdownMenuItem onClick={onDelete} className="text-destructive">삭제</DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}

interface FormDialogProps {
  title: string
  mode: 'create' | 'edit'
  initial?: Store
  onDone: () => void
}
function StoreFormDialog({ title, mode, initial, onDone }: FormDialogProps) {
  const create = useCreateStore()
  const update = useUpdateStore()

  const form = useForm<StoreInput>({
    resolver: zodResolver(StoreSchema),
    defaultValues: { name: initial?.name ?? '', location: initial?.location ?? '' },
  })

  const onSubmit = form.handleSubmit((values) => {
    if (mode === 'create') {
      create.mutate(values, {
        onSuccess: () => { toast.success('매장이 추가되었습니다.'); onDone() },
        onError: (e) => form.setError('root', { message: getErrorMessage(e) }),
      })
    } else {
      update.mutate({ id: initial!.id, input: values }, {
        onSuccess: () => { toast.success('매장이 수정되었습니다.'); onDone() },
        onError: (e) => form.setError('root', { message: getErrorMessage(e) }),
      })
    }
  })

  const busy = mode === 'create' ? create.isPending : update.isPending

  return (
    <DialogContent>
      <DialogHeader><DialogTitle>{title}</DialogTitle></DialogHeader>
      <Form {...form}>
        <form onSubmit={onSubmit} className="space-y-4">
          <FormField control={form.control} name="name" render={({ field }) => (
            <FormItem>
              <FormLabel>매장 이름</FormLabel>
              <FormControl><Input {...field} /></FormControl>
              <FormMessage />
            </FormItem>
          )} />
          <FormField control={form.control} name="location" render={({ field }) => (
            <FormItem>
              <FormLabel>위치</FormLabel>
              <FormControl><Input {...field} /></FormControl>
              <FormMessage />
            </FormItem>
          )} />
          {form.formState.errors.root && <p className="text-sm text-destructive">{form.formState.errors.root.message}</p>}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={onDone}>취소</Button>
            <Button type="submit" disabled={busy}>{busy ? '저장 중...' : '저장'}</Button>
          </DialogFooter>
        </form>
      </Form>
    </DialogContent>
  )
}
