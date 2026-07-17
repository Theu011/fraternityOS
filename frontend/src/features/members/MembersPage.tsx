import { useState } from 'react'
import { MoreHorizontal, Search, Users } from 'lucide-react'
import { useAuth } from '@/features/auth/auth-context'
import { PageHeader } from '@/components/PageHeader'
import { Placeholder } from '@/components/Placeholder'
import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { cn } from '@/lib/utils'
import { useAlumni, useMembers, type Member } from './api'
import { ManagePositionsDialog } from './ManagePositionsDialog'
import { ChangeStatusDialog } from './ChangeStatusDialog'
import { MemberProfileDialog } from './MemberProfileDialog'

type Tab = 'active' | 'alumni'
type OpenDialog = 'positions' | 'status' | 'profile' | null

function initials(name: string) {
  return name
    .split(' ')
    .map((p) => p[0])
    .slice(0, 2)
    .join('')
    .toUpperCase()
}

const STATUS_VARIANT: Record<Member['status'], 'secondary' | 'destructive'> = {
  ACTIVE: 'secondary',
  ALUMNI: 'destructive',
}

const STATUS_LABEL: Record<Member['status'], string> = {
  ACTIVE: 'Ativo',
  ALUMNI: 'Ex-membro',
}

function MemberRow({ member, canManage }: { member: Member; canManage: boolean }) {
  const [dialog, setDialog] = useState<OpenDialog>(null)

  return (
    <tr className="border-t">
      <td className="px-4 py-3">
        <div className="flex items-center gap-3">
          <Avatar>
            <AvatarFallback className="bg-primary/10 text-primary">
              {initials(member.name)}
            </AvatarFallback>
          </Avatar>
          <div className="min-w-0">
            <p className="truncate font-medium">{member.name}</p>
            <p className="truncate text-sm text-muted-foreground">{member.email}</p>
          </div>
        </div>
      </td>
      <td className="px-4 py-3 text-sm text-muted-foreground">{member.room ?? '—'}</td>
      <td className="px-4 py-3">
        <div className="flex flex-wrap gap-1">
          {member.positions.length === 0 ? (
            <span className="text-sm text-muted-foreground">Residente</span>
          ) : (
            member.positions.map((p) => (
              <Badge key={p} variant="outline">
                {p}
              </Badge>
            ))
          )}
        </div>
      </td>
      <td className="px-4 py-3">
        <Badge variant={STATUS_VARIANT[member.status]}>{STATUS_LABEL[member.status]}</Badge>
      </td>
      {canManage && (
        <td className="px-4 py-3 text-right">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" aria-label="Ações do membro">
                <MoreHorizontal className="size-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onSelect={() => setDialog('positions')}>
                Gerenciar cargos
              </DropdownMenuItem>
              <DropdownMenuItem onSelect={() => setDialog('status')}>
                Alterar status
              </DropdownMenuItem>
              <DropdownMenuItem onSelect={() => setDialog('profile')}>
                Ver perfil
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>

          <ManagePositionsDialog
            member={member}
            open={dialog === 'positions'}
            onOpenChange={(o) => setDialog(o ? 'positions' : null)}
          />
          <ChangeStatusDialog
            member={member}
            open={dialog === 'status'}
            onOpenChange={(o) => setDialog(o ? 'status' : null)}
          />
          <MemberProfileDialog
            member={member}
            open={dialog === 'profile'}
            onOpenChange={(o) => setDialog(o ? 'profile' : null)}
          />
        </td>
      )}
    </tr>
  )
}

function MemberTable({
  members,
  canManage,
}: {
  members: Member[]
  canManage: boolean
}) {
  return (
    <Card className="overflow-hidden p-0">
      <div className="overflow-x-auto">
        <table className="w-full min-w-[36rem] text-left">
          <thead className="bg-muted/50 text-xs font-medium uppercase tracking-wide text-muted-foreground">
            <tr>
              <th className="px-4 py-2.5">Membro</th>
              <th className="px-4 py-2.5">Quarto</th>
              <th className="px-4 py-2.5">Cargos</th>
              <th className="px-4 py-2.5">Status</th>
              {canManage && <th className="px-4 py-2.5 text-right">Ações</th>}
            </tr>
          </thead>
          <tbody>
            {members.map((m) => (
              <MemberRow key={m.id} member={m} canManage={canManage} />
            ))}
          </tbody>
        </table>
      </div>
    </Card>
  )
}

export function MembersPage() {
  const { user } = useAuth()
  const isPresident = user?.isPresident ?? false
  const [tab, setTab] = useState<Tab>('active')
  const [search, setSearch] = useState('')

  const active = useMembers()
  const alumni = useAlumni()
  const current = tab === 'active' ? active : alumni

  const query = search.trim().toLowerCase()
  const filtered = (current.data ?? []).filter((m) => m.name.toLowerCase().includes(query))

  return (
    <div>
      <PageHeader title="Membros" description="Todos que fazem parte da sua república." />

      <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
        <div className="inline-flex rounded-md border p-0.5">
          {(['active', 'alumni'] as const).map((t) => (
            <button
              key={t}
              onClick={() => setTab(t)}
              className={cn(
                'rounded px-3 py-1.5 text-sm font-medium capitalize transition-colors',
                tab === t
                  ? 'bg-primary text-primary-foreground'
                  : 'text-muted-foreground hover:text-foreground',
              )}
            >
              {t === 'active' ? 'Membros' : 'Ex-membros'}
            </button>
          ))}
        </div>
        <div className="relative w-full max-w-xs">
          <Search className="absolute left-2.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            aria-label="Buscar membros por nome"
            placeholder="Buscar por nome…"
            className="pl-8"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
      </div>

      {current.isLoading && <p className="text-sm text-muted-foreground">Carregando…</p>}
      {current.isError && <p className="text-sm text-destructive">Falha ao carregar os membros.</p>}

      {current.data && filtered.length === 0 && (
        <Placeholder
          icon={Users}
          message={
            query
              ? 'Nenhum membro corresponde à busca.'
              : tab === 'active'
                ? 'Ainda não há membros ativos.'
                : 'Ainda não há ex-membros.'
          }
        />
      )}

      {filtered.length > 0 && <MemberTable members={filtered} canManage={isPresident} />}
    </div>
  )
}
