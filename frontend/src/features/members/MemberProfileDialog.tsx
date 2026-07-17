import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Badge } from '@/components/ui/badge'
import type { Member } from './api'

interface Props {
  member: Member
  open: boolean
  onOpenChange: (open: boolean) => void
}

function Row({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div className="flex justify-between gap-4 border-b py-2 last:border-b-0">
      <span className="text-sm text-muted-foreground">{label}</span>
      <span className="text-sm font-medium">{value}</span>
    </div>
  )
}

export function MemberProfileDialog({ member, open, onOpenChange }: Props) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{member.name}</DialogTitle>
          <DialogDescription>{member.email}</DialogDescription>
        </DialogHeader>
        <div className="mt-2">
          <Row label="Telefone" value={member.phone ?? '—'} />
          <Row label="Quarto" value={member.room ?? '—'} />
          <Row label="Status" value={member.status === 'ACTIVE' ? 'Ativo' : 'Ex-membro'} />
          <Row
            label="Cargos"
            value={
              member.positions.length === 0 ? (
                'Residente'
              ) : (
                <span className="flex flex-wrap justify-end gap-1">
                  {member.positions.map((p) => (
                    <Badge key={p} variant="outline">
                      {p}
                    </Badge>
                  ))}
                </span>
              )
            }
          />
          <Row label="Entrou em" value={new Date(member.joinedAt).toLocaleDateString('pt-BR')} />
        </div>
      </DialogContent>
    </Dialog>
  )
}
