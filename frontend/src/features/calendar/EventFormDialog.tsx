import { useEffect, useState, type FormEvent, type ReactNode } from 'react'
import { Trash2 } from 'lucide-react'
import { useCreateEvent, useDeleteEvent, useUpdateEvent } from './api'
import type { CalendarEvent } from './types'
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

interface Props {
  trigger: ReactNode
  /** Edit this event; otherwise create a new one. */
  event?: CalendarEvent
  /** Prefilled date for a new event (e.g. the clicked day). */
  defaultDate?: string
}

export function EventFormDialog({ trigger, event, defaultDate }: Props) {
  const isEdit = Boolean(event)
  const [open, setOpen] = useState(false)
  const [title, setTitle] = useState(event?.title ?? '')
  const [date, setDate] = useState(event?.date ?? defaultDate ?? '')
  const [description, setDescription] = useState(event?.description ?? '')

  // Reset the date to the clicked day each time a "new event" dialog opens.
  useEffect(() => {
    if (open && !isEdit) setDate(defaultDate ?? '')
  }, [open, isEdit, defaultDate])

  const create = useCreateEvent()
  const update = useUpdateEvent()
  const remove = useDeleteEvent()
  const pending = create.isPending || update.isPending || remove.isPending

  async function onDelete() {
    if (!event) return
    await remove.mutateAsync(event.id)
    setOpen(false)
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    const input = { title, date, description }
    if (isEdit && event) {
      await update.mutateAsync({ id: event.id, input })
    } else {
      await create.mutateAsync(input)
      setTitle('')
      setDescription('')
    }
    setOpen(false)
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>{trigger}</DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{isEdit ? 'Editar evento' : 'Novo evento'}</DialogTitle>
          <DialogDescription>
            {isEdit ? 'Atualize este item do calendário.' : 'Adicione um item ao calendário da república.'}
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={onSubmit} className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="ev-title">Título</Label>
            <Input
              id="ev-title"
              required
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Reunião da república"
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="ev-date">Data</Label>
            <Input
              id="ev-date"
              type="date"
              required
              value={date}
              onChange={(e) => setDate(e.target.value)}
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="ev-desc">Descrição</Label>
            <Textarea
              id="ev-desc"
              rows={3}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Detalhes (opcional)…"
            />
          </div>
          <DialogFooter className="gap-2 sm:justify-between">
            {isEdit ? (
              <Button
                type="button"
                variant="ghost"
                className="text-destructive hover:text-destructive"
                onClick={onDelete}
                disabled={pending}
              >
                <Trash2 /> Excluir
              </Button>
            ) : (
              <span />
            )}
            <div className="flex gap-2">
              <DialogClose asChild>
                <Button type="button" variant="outline">
                  Cancelar
                </Button>
              </DialogClose>
              <Button type="submit" disabled={pending}>
                {pending ? 'Salvando…' : isEdit ? 'Salvar alterações' : 'Adicionar evento'}
              </Button>
            </div>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
