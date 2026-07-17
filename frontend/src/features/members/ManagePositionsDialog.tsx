import { useEffect, useMemo, useState } from 'react'
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
import { useAssignPositions, usePositions, useRemovePosition, type Member } from './api'

interface Props {
  member: Member
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function ManagePositionsDialog({ member, open, onOpenChange }: Props) {
  const { data: positions, isLoading } = usePositions()
  const assign = useAssignPositions()
  const remove = useRemovePosition()
  const [selected, setSelected] = useState<Set<number>>(new Set())
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  // The member's currently-assigned position ids (mapped from names via the catalog).
  const original = useMemo(
    () => new Set(positions?.filter((p) => member.positions.includes(p.name)).map((p) => p.id)),
    [positions, member.positions],
  )

  useEffect(() => {
    if (open) {
      setSelected(new Set(original))
      setError(null)
    }
  }, [open, original])

  function toggle(id: number) {
    setSelected((prev) => {
      const next = new Set(prev)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      return next
    })
  }

  async function onSave() {
    setError(null)
    setSaving(true)
    const toAdd = [...selected].filter((id) => !original.has(id))
    const toRemove = [...original].filter((id) => !selected.has(id))
    try {
      if (toAdd.length) await assign.mutateAsync({ id: member.id, positionIds: toAdd })
      for (const positionId of toRemove) {
        await remove.mutateAsync({ id: member.id, positionId })
      }
      onOpenChange(false)
    } catch (err) {
      if (axios.isAxiosError(err) && err.response?.status === 409) {
        setError('A república deve ter sempre pelo menos um Presidente ativo.')
      } else {
        setError('Não foi possível atualizar os cargos. Tente novamente.')
      }
    } finally {
      setSaving(false)
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Gerenciar cargos</DialogTitle>
          <DialogDescription>
            Atribua ou remova cargos de {member.name}. Presidente e Tesoureiro concedem permissões.
          </DialogDescription>
        </DialogHeader>

        {isLoading && <p className="text-sm text-muted-foreground">Carregando cargos…</p>}
        <div className="space-y-1">
          {positions?.map((p) => (
            <label
              key={p.id}
              className="flex cursor-pointer items-center gap-3 rounded-md px-2 py-2 hover:bg-muted"
            >
              <input
                type="checkbox"
                className="size-4 accent-primary"
                checked={selected.has(p.id)}
                onChange={() => toggle(p.id)}
              />
              <span className="text-sm font-medium">{p.name}</span>
              {p.description && (
                <span className="truncate text-xs text-muted-foreground">{p.description}</span>
              )}
            </label>
          ))}
        </div>

        {error && <p className="text-sm text-destructive">{error}</p>}
        <DialogFooter className="gap-2">
          <Button variant="outline" type="button" onClick={() => onOpenChange(false)}>
            Cancelar
          </Button>
          <Button type="button" onClick={onSave} disabled={saving || isLoading}>
            {saving ? 'Salvando…' : 'Salvar'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
