import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import {
  Form, FormControl, FormField, FormItem, FormLabel, FormMessage,
} from '@/components/ui/form'
import { Card, CardContent } from '@/components/ui/card'
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from '@/components/ui/select'
import { LeaveRequestSchema, type LeaveRequestInput } from '../schema'
import { useCreateLeave } from '../api'
import { leaveTypeLabel, leaveTypeLabels } from '@/lib/utils/labels'
import { getErrorMessage } from '@/lib/utils/errors'

const leaveTypes = Object.keys(leaveTypeLabels) as Array<keyof typeof leaveTypeLabels>

export default function LeaveRequestPage() {
  const navigate = useNavigate()
  const create = useCreateLeave()

  const form = useForm<LeaveRequestInput>({
    resolver: zodResolver(LeaveRequestSchema),
    defaultValues: { leaveType: 'ANNUAL', startDate: '', endDate: '', reason: '' },
  })

  const onSubmit = form.handleSubmit((values) => {
    create.mutate(values, {
      onSuccess: () => { toast.success('휴가가 신청되었습니다.'); navigate('/leave') },
      onError: (e) => form.setError('root', { message: getErrorMessage(e) }),
    })
  })

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold">휴가 신청</h1>
      <Card>
        <CardContent className="pt-6">
          <Form {...form}>
            <form onSubmit={onSubmit} className="space-y-4">
              <FormField
                control={form.control}
                name="leaveType"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>유형</FormLabel>
                    <Select value={field.value} onValueChange={field.onChange}>
                      <FormControl>
                        <SelectTrigger><SelectValue /></SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {leaveTypes.map((t) => (
                          <SelectItem key={t} value={t}>{leaveTypeLabel(t)}</SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="startDate"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>시작일</FormLabel>
                    <FormControl><Input type="date" {...field} /></FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="endDate"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>종료일</FormLabel>
                    <FormControl><Input type="date" {...field} /></FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="reason"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>사유 (선택)</FormLabel>
                    <FormControl><Input placeholder="예: 병원 방문" {...field} /></FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {form.formState.errors.root && (
                <p className="text-sm text-destructive">{form.formState.errors.root.message}</p>
              )}

              <div className="flex gap-2">
                <Button type="button" variant="outline" className="flex-1" onClick={() => navigate(-1)}>취소</Button>
                <Button type="submit" className="flex-1" disabled={create.isPending}>
                  {create.isPending ? '신청 중...' : '신청'}
                </Button>
              </div>
            </form>
          </Form>
        </CardContent>
      </Card>
    </div>
  )
}
