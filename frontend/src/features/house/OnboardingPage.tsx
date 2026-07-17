import { useState, type FormEvent } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import axios from 'axios'
import { Building2, Check, LogOut, Plus, Search, Users } from 'lucide-react'
import { useAuth } from '@/features/auth/auth-context'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { useCreateHouse, useHouses, useMyJoinRequests, useRequestJoin } from './api'
import type { JoinRequestStatus } from './api'

function CreateHouseCard() {
  const { applyAuth } = useAuth()
  const navigate = useNavigate()
  const create = useCreateHouse()
  const [name, setName] = useState('')
  const [error, setError] = useState<string | null>(null)

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    try {
      const auth = await create.mutateAsync(name.trim())
      applyAuth(auth)
      navigate('/', { replace: true })
    } catch (err) {
      if (axios.isAxiosError(err) && err.response?.status === 409) {
        setError('Você já pertence a uma república.')
      } else {
        setError('Não foi possível criar a república. Tente novamente.')
      }
    }
  }

  return (
    <Card>
      <CardHeader>
        <div className="mb-1 flex size-9 items-center justify-center rounded-lg bg-primary/10 text-primary">
          <Plus className="size-5" />
        </div>
        <CardTitle>Criar uma república</CardTitle>
        <CardDescription>Comece uma nova república. Você se tornará o Presidente.</CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={onSubmit} className="space-y-3">
          <Input
            aria-label="Nome da república"
            placeholder="ex.: Alpha Beta"
            required
            value={name}
            onChange={(e) => setName(e.target.value)}
          />
          {error && <p className="text-sm text-destructive">{error}</p>}
          <Button type="submit" className="w-full" disabled={create.isPending || !name.trim()}>
            {create.isPending ? 'Criando…' : 'Criar república'}
          </Button>
        </form>
      </CardContent>
    </Card>
  )
}

function JoinHouseCard() {
  const [search, setSearch] = useState('')
  const { data: houses, isLoading, isError } = useHouses(search)
  const join = useRequestJoin()
  const [requested, setRequested] = useState<Set<number>>(new Set())
  const [error, setError] = useState<string | null>(null)

  async function onJoin(houseId: number) {
    setError(null)
    try {
      await join.mutateAsync(houseId)
      setRequested((prev) => new Set(prev).add(houseId))
    } catch (err) {
      if (axios.isAxiosError(err) && err.response?.status === 409) {
        setRequested((prev) => new Set(prev).add(houseId))
      } else {
        setError('Não foi possível enviar a solicitação. Tente novamente.')
      }
    }
  }

  return (
    <Card>
      <CardHeader>
        <div className="mb-1 flex size-9 items-center justify-center rounded-lg bg-primary/10 text-primary">
          <Search className="size-5" />
        </div>
        <CardTitle>Entrar em uma república existente</CardTitle>
        <CardDescription>Encontre sua república e solicite para entrar.</CardDescription>
      </CardHeader>
      <CardContent className="space-y-3">
        <div className="relative">
          <Search className="absolute left-2.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            aria-label="Buscar repúblicas"
            placeholder="Buscar por nome…"
            className="pl-8"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        {error && <p className="text-sm text-destructive">{error}</p>}

        {isLoading && <p className="text-sm text-muted-foreground">Carregando repúblicas…</p>}
        {isError && <p className="text-sm text-destructive">Falha ao carregar as repúblicas.</p>}
        {houses && houses.length === 0 && (
          <p className="py-6 text-center text-sm text-muted-foreground">
            {search.trim() ? 'Nenhuma república corresponde à busca.' : 'Nenhuma república foi criada ainda.'}
          </p>
        )}

        <div className="space-y-2">
          {houses?.map((h) => {
            const done = requested.has(h.id)
            return (
              <div
                key={h.id}
                className="flex items-center justify-between gap-3 rounded-md border p-3"
              >
                <div className="min-w-0">
                  <p className="truncate font-medium">{h.name}</p>
                  <p className="flex items-center gap-1 text-xs text-muted-foreground">
                    <Users className="size-3" />
                    {h.activeMemberCount} {h.activeMemberCount === 1 ? 'membro ativo' : 'membros ativos'}
                  </p>
                </div>
                <Button
                  size="sm"
                  variant={done ? 'secondary' : 'default'}
                  disabled={done || join.isPending}
                  onClick={() => onJoin(h.id)}
                >
                  {done ? (
                    <>
                      <Check /> Solicitado
                    </>
                  ) : (
                    'Entrar'
                  )}
                </Button>
              </div>
            )
          })}
        </div>
      </CardContent>
    </Card>
  )
}

const STATUS_LABEL: Record<JoinRequestStatus, string> = {
  PENDING: 'Pendente',
  APPROVED: 'Aprovada',
  REJECTED: 'Recusada',
}

function MyRequests() {
  const { data, isLoading } = useMyJoinRequests()
  if (isLoading || !data || data.length === 0) return null
  return (
    <Card className="mt-4">
      <CardHeader>
        <CardTitle className="text-base">Suas solicitações</CardTitle>
        <CardDescription>Solicitações de entrada que você enviou.</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="divide-y rounded-md border">
          {data.map((r) => (
            <div key={r.id} className="flex items-center justify-between gap-3 px-3 py-2.5 text-sm">
              <span className="truncate font-medium">{r.houseName}</span>
              <Badge
                variant={
                  r.status === 'APPROVED'
                    ? 'secondary'
                    : r.status === 'REJECTED'
                      ? 'destructive'
                      : 'outline'
                }
              >
                {STATUS_LABEL[r.status]}
              </Badge>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  )
}

export function OnboardingPage() {
  const { user, logout } = useAuth()

  // A user who already belongs to a house has nothing to do here.
  if (user?.houseId) return <Navigate to="/" replace />

  return (
    <div className="min-h-screen bg-muted/40 px-4 py-10">
      <div className="mx-auto max-w-3xl">
        <div className="mb-6 flex justify-end">
          <Button variant="ghost" size="sm" onClick={logout}>
            <LogOut /> Sair
          </Button>
        </div>
        <div className="mb-8 text-center">
          <div className="mx-auto mb-3 flex h-11 w-11 items-center justify-center rounded-lg bg-primary text-primary-foreground">
            <Building2 className="size-6" />
          </div>
          <h1 className="text-2xl font-semibold">Bem-vindo{user?.name ? `, ${user.name}` : ''}</h1>
          <p className="mt-1 text-muted-foreground">
            Você ainda não faz parte de uma república. Crie uma ou entre em uma existente para começar.
          </p>
        </div>
        <div className="grid gap-4 md:grid-cols-2">
          <CreateHouseCard />
          <JoinHouseCard />
        </div>
        <MyRequests />
      </div>
    </div>
  )
}
