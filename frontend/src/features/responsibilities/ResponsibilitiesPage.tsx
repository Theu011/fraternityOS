import { CheckCircle2, Circle, Clock, ListChecks, Plus, Trash2, User } from 'lucide-react'
import type { LucideIcon } from 'lucide-react'
import { useAuth } from '@/features/auth/auth-context'
import { PageHeader } from '@/components/PageHeader'
import { Button } from '@/components/ui/button'
import { Card } from '@/components/ui/card'
import { cn } from '@/lib/utils'
import { useChores, useCompleteChore, useDeleteChore } from '@/features/chores/api'
import { ChoreFormDialog } from '@/features/chores/ChoreFormDialog'
import type { Chore, ChoreStatus } from '@/features/chores/types'

const COLUMNS: { status: ChoreStatus; label: string; icon: LucideIcon; accent: string }[] = [
  { status: 'PENDING', label: 'Pendentes', icon: Circle, accent: 'text-muted-foreground' },
  { status: 'OVERDUE', label: 'Atrasadas', icon: Clock, accent: 'text-destructive' },
  { status: 'COMPLETED', label: 'Concluídas', icon: CheckCircle2, accent: 'text-emerald-600' },
]

function formatDate(iso: string) {
  return new Date(iso + 'T00:00:00').toLocaleDateString('pt-BR', { month: 'short', day: 'numeric' })
}

export function ResponsibilitiesPage() {
  const { user } = useAuth()
  const isPresident = user?.isPresident ?? false
  const { data: chores, isLoading, isError } = useChores()
  const complete = useCompleteChore()
  const remove = useDeleteChore()

  const canComplete = (c: Chore) =>
    c.status !== 'COMPLETED' && (isPresident || c.assigneeId === user?.membershipId)

  return (
    <div>
      <div className="mb-6 flex flex-wrap items-start justify-between gap-4">
        <PageHeader title="Responsabilidades" description="Tarefas e quem é responsável por elas." />
        {isPresident && (
          <ChoreFormDialog
            trigger={
              <Button>
                <Plus /> Nova tarefa
              </Button>
            }
          />
        )}
      </div>

      {isLoading && <p className="text-sm text-muted-foreground">Carregando…</p>}
      {isError && <p className="text-sm text-destructive">Falha ao carregar as tarefas.</p>}

      {chores && (
        <div className="grid gap-4 md:grid-cols-3">
          {COLUMNS.map(({ status, label, icon: Icon, accent }) => {
            const items = chores.filter((c) => c.status === status)
            return (
              <div key={status} className="flex flex-col gap-3">
                <div className="flex items-center gap-2 px-1">
                  <Icon className={cn('size-4', accent)} />
                  <h2 className="text-sm font-semibold">{label}</h2>
                  <span className="ml-auto rounded-full bg-muted px-2 py-0.5 text-xs text-muted-foreground">
                    {items.length}
                  </span>
                </div>

                <div className="flex flex-col gap-3">
                  {items.length === 0 && (
                    <p className="rounded-lg border border-dashed px-3 py-6 text-center text-xs text-muted-foreground">
                      Nada aqui.
                    </p>
                  )}
                  {items.map((c) => (
                    <Card key={c.id} className="p-4">
                      <div className="flex items-start justify-between gap-2">
                        <h3 className="font-medium leading-tight">{c.title}</h3>
                        {isPresident && (
                          <button
                            aria-label="Excluir tarefa"
                            onClick={() => remove.mutate(c.id)}
                            className="text-muted-foreground hover:text-destructive"
                          >
                            <Trash2 className="size-4" />
                          </button>
                        )}
                      </div>
                      {c.description && (
                        <p className="mt-1 text-sm text-muted-foreground">{c.description}</p>
                      )}
                      <div className="mt-3 flex flex-wrap items-center gap-x-3 gap-y-1 text-xs text-muted-foreground">
                        <span className="flex items-center gap-1">
                          <User className="size-3" />
                          {c.assigneeName ?? 'Sem responsável'}
                        </span>
                        <span
                          className={cn(
                            'flex items-center gap-1',
                            c.status === 'OVERDUE' && 'text-destructive',
                          )}
                        >
                          <Clock className="size-3" />
                          Prazo {formatDate(c.dueDate)}
                        </span>
                      </div>
                      {canComplete(c) && (
                        <Button
                          size="sm"
                          variant="outline"
                          className="mt-3 w-full"
                          onClick={() => complete.mutate(c.id)}
                          disabled={complete.isPending}
                        >
                          <CheckCircle2 /> Concluir
                        </Button>
                      )}
                    </Card>
                  ))}
                </div>
              </div>
            )
          })}
        </div>
      )}

      {chores && chores.length === 0 && !isPresident && (
        <div className="mt-4 flex items-center gap-2 text-sm text-muted-foreground">
          <ListChecks className="size-4" /> Nenhuma tarefa atribuída ainda.
        </div>
      )}
    </div>
  )
}
