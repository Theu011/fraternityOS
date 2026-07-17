import { useState } from 'react'
import axios from 'axios'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import { useChangeStatus, type Member, type MemberStatus } from './api'

interface Props {
  member: Member
  open: boolean
  onOpenChange: (open: boolean) => void
}

const selectClass =
  'flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring'

export function ChangeStatusDialog({ member, open, onOpenChange }: Props) {
  const change = useChangeStatus()
  const [status, setStatus] = useState<MemberStatus>(member.status)
  const [error, setError] = useState<string | null>(null)

  async function onSave() {
    setError(null)
    try {
      await change.mutateAsync({ id: member.id, status })
      onOpenChange(false)
    } catch (err) {
      if (axios.isAxiosError(err) && err.response?.status === 409) {
        setError('A república deve ter sempre pelo menos um Presidente ativo.')
      } else {
        setError('Não foi possível alterar o status. Tente novamente.')
      }
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Alterar status</DialogTitle>
          <DialogDescription>Atualize o status da participação de {member.name}.</DialogDescription>
        </DialogHeader>
        <div className="space-y-1.5">
          <Label htmlFor="member-status">Status</Label>
          <select
            id="member-status"
            className={selectClass}
            value={status}
            onChange={(e) => setStatus(e.target.value as MemberStatus)}
          >
            <option value="ACTIVE">Ativo</option>
            <option value="ALUMNI">Ex-membro</option>
          </select>
        </div>
        {error && <p className="text-sm text-destructive">{error}</p>}
        <DialogFooter className="gap-2">
          <Button variant="outline" type="button" onClick={() => onOpenChange(false)}>
            Cancelar
          </Button>
          <Button type="button" onClick={onSave} disabled={change.isPending || status === member.status}>
            {change.isPending ? 'Salvando…' : 'Salvar'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
