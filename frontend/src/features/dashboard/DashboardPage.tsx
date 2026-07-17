import { Users, Calendar, ListChecks, Wallet, type LucideIcon } from 'lucide-react'
import { useAuth } from '@/features/auth/auth-context'
import { PageHeader } from '@/components/PageHeader'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { useMembers } from '@/features/members/api'
import { useEvents } from '@/features/calendar/api'
import { useChores } from '@/features/chores/api'
import { useStatements } from '@/features/finance/api'

const pad = (n: number) => String(n).padStart(2, '0')

function todayKey() {
  const d = new Date()
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

interface Stat {
  label: string
  icon: LucideIcon
  value: number | undefined
  loading: boolean
}

export function DashboardPage() {
  const { user } = useAuth()

  const members = useMembers()
  const events = useEvents()
  const chores = useChores()
  const statements = useStatements()

  const today = todayKey()

  const stats: Stat[] = [
    {
      label: 'Membros',
      icon: Users,
      value: members.data?.length,
      loading: members.isLoading,
    },
    {
      label: 'Próximos eventos',
      icon: Calendar,
      value: events.data?.filter((e) => e.date >= today).length,
      loading: events.isLoading,
    },
    {
      label: 'Tarefas em aberto',
      icon: ListChecks,
      value: chores.data?.filter((c) => c.status !== 'COMPLETED').length,
      loading: chores.isLoading,
    },
    {
      label: 'Pagamentos pendentes',
      icon: Wallet,
      value: statements.data?.filter((s) => s.myStatus === 'PENDING').length,
      loading: statements.isLoading,
    },
  ]

  return (
    <div>
      <PageHeader
        title={`Bem-vindo de volta, ${user?.name.split(' ')[0]}`}
        description="Veja o que está acontecendo na sua república."
      />
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {stats.map(({ label, value, icon: Icon, loading }) => (
          <Card key={label}>
            <CardHeader className="flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">{label}</CardTitle>
              <Icon className="size-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">
                {loading ? '…' : (value ?? 0)}
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  )
}
