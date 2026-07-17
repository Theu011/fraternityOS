import { useState } from 'react'
import { CheckCircle2, ChevronDown, FileText, Plus, Trash2, Upload, Wallet } from 'lucide-react'
import { useAuth } from '@/features/auth/auth-context'
import { PageHeader } from '@/components/PageHeader'
import { Placeholder } from '@/components/Placeholder'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { cn } from '@/lib/utils'
import {
  openAttachment,
  useDeleteStatement,
  usePayStatement,
  useStatementPayments,
  useStatements,
} from './api'
import { UploadStatementDialog } from './UploadStatementDialog'
import type { Statement } from './types'

const MONTHS = [
  'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
  'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro',
]
const money = (n: number) => n.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })

function PaymentRoster({ statementId }: { statementId: number }) {
  const { data, isLoading } = useStatementPayments(statementId, true)
  if (isLoading) return <p className="text-xs text-muted-foreground">Carregando pagamentos…</p>
  return (
    <div className="divide-y rounded-md border">
      {data?.map((p) => (
        <div key={p.memberId} className="flex items-center justify-between px-3 py-2 text-sm">
          <span>{p.memberName ?? 'Desconhecido'}</span>
          <span className="flex items-center gap-3">
            <span className="text-muted-foreground">{money(p.amount)}</span>
            <Badge variant={p.status === 'PAID' ? 'secondary' : 'outline'}>
              {p.status === 'PAID' ? 'Pago' : 'Pendente'}
            </Badge>
          </span>
        </div>
      ))}
    </div>
  )
}

function StatementCard({ statement }: { statement: Statement }) {
  const { user } = useAuth()
  const isTreasurer = user?.isTreasurer ?? false
  const canSeeRoster = isTreasurer || (user?.isPresident ?? false)
  const pay = usePayStatement()
  const del = useDeleteStatement()
  const [showRoster, setShowRoster] = useState(false)

  return (
    <Card>
      <CardHeader className="flex-row items-start justify-between gap-3 space-y-0">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <CardTitle>
              {MONTHS[statement.month - 1]} {statement.year}
            </CardTitle>
            {statement.myStatus && (
              <Badge variant={statement.myStatus === 'PAID' ? 'secondary' : 'outline'}>
                {statement.myStatus === 'PAID' ? 'Você pagou' : 'Você deve'}
              </Badge>
            )}
          </div>
          <p className="text-xs text-muted-foreground">
            Enviado por {statement.uploadedByName ?? 'Desconhecido'} ·{' '}
            <span className="font-medium text-foreground">
              {statement.paidCount}/{statement.totalCount} pagos
            </span>
            {statement.myAmount != null && <> · Sua parte {money(statement.myAmount)}</>}
          </p>
        </div>
        {isTreasurer && (
          <button
            aria-label="Excluir demonstrativo"
            onClick={() => del.mutate(statement.id)}
            className="text-muted-foreground hover:text-destructive"
          >
            <Trash2 className="size-4" />
          </button>
        )}
      </CardHeader>
      <CardContent className="space-y-3">
        {statement.notes && <p className="text-sm text-muted-foreground">{statement.notes}</p>}
        <div className="flex flex-wrap items-center gap-2">
          {statement.hasAttachment && (
            <Button variant="outline" size="sm" onClick={() => openAttachment(statement.id)}>
              <FileText /> Abrir demonstrativo
            </Button>
          )}
          {statement.myStatus === 'PENDING' && (
            <Button size="sm" onClick={() => pay.mutate(statement.id)} disabled={pay.isPending}>
              <CheckCircle2 /> Marcar como pago
            </Button>
          )}
          {canSeeRoster && (
            <Button variant="ghost" size="sm" onClick={() => setShowRoster((s) => !s)}>
              <ChevronDown className={cn('transition-transform', showRoster && 'rotate-180')} />
              Pagamentos
            </Button>
          )}
        </div>
        {showRoster && <PaymentRoster statementId={statement.id} />}
      </CardContent>
    </Card>
  )
}

export function FinancePage() {
  const { user } = useAuth()
  const isTreasurer = user?.isTreasurer ?? false
  const { data, isLoading, isError } = useStatements()

  return (
    <div>
      <div className="mb-6 flex flex-wrap items-start justify-between gap-4">
        <PageHeader title="Finanças" description="Demonstrativos de aluguel mensais e pagamentos." />
        {isTreasurer && (
          <UploadStatementDialog
            trigger={
              <Button>
                <Upload /> Enviar demonstrativo
              </Button>
            }
          />
        )}
      </div>

      {isLoading && <p className="text-sm text-muted-foreground">Carregando…</p>}
      {isError && <p className="text-sm text-destructive">Falha ao carregar os demonstrativos.</p>}

      {data && data.length === 0 && (
        <Placeholder
          icon={Wallet}
          message={
            isTreasurer
              ? 'Ainda não há demonstrativos. Envie o primeiro demonstrativo mensal.'
              : 'Nenhum demonstrativo foi publicado ainda.'
          }
        />
      )}

      {data && data.length > 0 && (
        <div className="space-y-4">
          {data.map((s) => (
            <StatementCard key={s.id} statement={s} />
          ))}
        </div>
      )}

      {!isTreasurer && data && data.length > 0 && (
        <p className="mt-4 flex items-center gap-1 text-xs text-muted-foreground">
          <Plus className="size-3" /> Apenas o tesoureiro pode enviar demonstrativos.
        </p>
      )}
    </div>
  )
}
