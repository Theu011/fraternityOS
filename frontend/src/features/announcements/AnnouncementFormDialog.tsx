import { useState, type FormEvent, type ReactNode } from 'react'
import { useCreateAnnouncement, useUpdateAnnouncement } from './api'
import type { Announcement } from './types'
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
  /** When provided, the dialog edits this announcement; otherwise it creates one. */
  announcement?: Announcement
}

export function AnnouncementFormDialog({ trigger, announcement }: Props) {
  const isEdit = Boolean(announcement)
  const [open, setOpen] = useState(false)
  const [title, setTitle] = useState(announcement?.title ?? '')
  const [content, setContent] = useState(announcement?.content ?? '')
  const [pinned, setPinned] = useState(announcement?.pinned ?? false)

  const create = useCreateAnnouncement()
  const update = useUpdateAnnouncement()
  const pending = create.isPending || update.isPending

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    const input = { title, content, pinned }
    if (isEdit && announcement) {
      await update.mutateAsync({ id: announcement.id, input })
    } else {
      await create.mutateAsync(input)
      setTitle('')
      setContent('')
      setPinned(false)
    }
    setOpen(false)
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>{trigger}</DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{isEdit ? 'Editar aviso' : 'Novo aviso'}</DialogTitle>
          <DialogDescription>
            {isEdit ? 'Atualize esta publicação.' : 'Compartilhe novidades com todos da república.'}
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={onSubmit} className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="title">Título</Label>
            <Input
              id="title"
              required
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Falta de água amanhã"
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="content">Conteúdo</Label>
            <Textarea
              id="content"
              required
              rows={4}
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder="Detalhes…"
            />
          </div>
          <label className="flex items-center gap-2 text-sm">
            <input
              type="checkbox"
              className="size-4 accent-[hsl(var(--primary))]"
              checked={pinned}
              onChange={(e) => setPinned(e.target.checked)}
            />
            Fixar no topo do mural
          </label>
          <DialogFooter className="gap-2">
            <DialogClose asChild>
              <Button type="button" variant="outline">
                Cancelar
              </Button>
            </DialogClose>
            <Button type="submit" disabled={pending}>
              {pending ? 'Salvando…' : isEdit ? 'Salvar alterações' : 'Publicar'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
