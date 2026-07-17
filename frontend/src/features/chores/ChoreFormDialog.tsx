import { useState, type FormEvent, type ReactNode } from 'react'
import { useCreateChore } from './api'
import { useMembers } from '@/features/members/api'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'

const selectClass =
  'flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring'

export function ChoreFormDialog({ trigger }: { trigger: ReactNode }) {
  const [open, setOpen] = useState(false)
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [assigneeId, setAssigneeId] = useState('')
  const [dueDate, setDueDate] = useState('')

  const { data: members } = useMembers()
  const create = useCreateChore()

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    await create.mutateAsync({
      title,
      description,
      assigneeId: Number(assigneeId),
      dueDate,
    })
    setTitle('')
    setDescription('')
    setAssigneeId('')
    setDueDate('')
    setOpen(false)
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>{trigger}</DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Nova tarefa</DialogTitle>
          <DialogDescription>Atribua uma tarefa a um membro da república.</DialogDescription>
        </DialogHeader>
        <form onSubmit={onSubmit} className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="ch-title">Título</Label>
            <Input
              id="ch-title"
              required
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Tirar o lixo"
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="ch-assignee">Atribuir a</Label>
            <select
              id="ch-assignee"
              required
              className={selectClass}
              value={assigneeId}
              onChange={(e) => setAssigneeId(e.target.value)}
            >
              <option value="" disabled>
                Selecione um membro…
              </option>
              {members?.map((m) => (
                <option key={m.id} value={m.id}>
                  {m.name}{m.positions?.length ? ` (${m.positions.join(', ')})` : ''}
                </option>
              ))}
            </select>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="ch-due">Prazo</Label>
            <Input
              id="ch-due"
              type="date"
              required
              value={dueDate}
              onChange={(e) => setDueDate(e.target.value)}
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="ch-desc">Descrição</Label>
            <Textarea
              id="ch-desc"
              rows={2}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Detalhes (opcional)…"
            />
          </div>
          <DialogFooter className="gap-2">
            <DialogClose asChild>
              <Button type="button" variant="outline">
                Cancelar
              </Button>
            </DialogClose>
            <Button type="submit" disabled={create.isPending}>
              {create.isPending ? 'Salvando…' : 'Adicionar tarefa'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
