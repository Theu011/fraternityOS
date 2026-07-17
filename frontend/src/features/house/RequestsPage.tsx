import { Check, Inbox, X } from 'lucide-react'
import { useAuth } from '@/features/auth/auth-context'
import { PageHeader } from '@/components/PageHeader'
import { Placeholder } from '@/components/Placeholder'
import { Button } from '@/components/ui/button'
import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import { Card, CardContent } from '@/components/ui/card'
import { useDecideJoinRequest, usePendingJoinRequests, type PendingJoinRequest } from './api'

function initials(name: string) {
  return name
    .split(' ')
    .map((p) => p[0])
    .slice(0, 2)
    .join('')
    .toUpperCase()
}

function RequestRow({ request }: { request: PendingJoinRequest }) {
  const decide = useDecideJoinRequest()
  const pending = decide.isPending && decide.variables?.id === request.id

  return (
    <Card>
      <CardContent className="flex flex-wrap items-center justify-between gap-3 p-4">
        <div className="flex min-w-0 items-center gap-3">
          <Avatar>
            <AvatarFallback className="bg-primary/10 text-primary">
              {initials(request.name)}
            </AvatarFallback>
          </Avatar>
          <div className="min-w-0">
            <p className="truncate font-medium">{request.name}</p>
            <p className="truncate text-sm text-muted-foreground">{request.email}</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            disabled={pending}
            onClick={() => decide.mutate({ id: request.id, decision: 'reject' })}
          >
            <X /> Recusar
          </Button>
          <Button
            size="sm"
            disabled={pending}
            onClick={() => decide.mutate({ id: request.id, decision: 'approve' })}
          >
            <Check /> Aprovar
          </Button>
        </div>
      </CardContent>
    </Card>
  )
}

export function RequestsPage() {
  const { user } = useAuth()
  const { data, isLoading, isError } = usePendingJoinRequests(!!user?.isPresident)

  return (
    <div>
      <PageHeader
        title="Solicitações de entrada"
        description="Analise quem pediu para entrar na sua república. Ao aprovar, a pessoa vira membro ativo."
      />

      {isLoading && <p className="text-sm text-muted-foreground">Carregando…</p>}
      {isError && <p className="text-sm text-destructive">Falha ao carregar as solicitações.</p>}

      {data && data.length === 0 && (
        <Placeholder icon={Inbox} message="Nenhuma solicitação de entrada pendente." />
      )}

      {data && data.length > 0 && (
        <div className="space-y-3">
          {data.map((r) => (
            <RequestRow key={r.id} request={r} />
          ))}
        </div>
      )}
    </div>
  )
}
