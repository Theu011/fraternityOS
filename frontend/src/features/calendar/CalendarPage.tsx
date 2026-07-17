import { useMemo, useState } from 'react'
import { ChevronLeft, ChevronRight, Plus } from 'lucide-react'
import { useAuth } from '@/features/auth/auth-context'
import { PageHeader } from '@/components/PageHeader'
import { Button } from '@/components/ui/button'
import { Card } from '@/components/ui/card'
import { cn } from '@/lib/utils'
import { useEvents } from './api'
import { EventFormDialog } from './EventFormDialog'
import type { CalendarEvent } from './types'

const WEEKDAYS = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb']
const MONTHS = [
  'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
  'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro',
]

const pad = (n: number) => String(n).padStart(2, '0')
const dateKey = (y: number, m: number, d: number) => `${y}-${pad(m + 1)}-${pad(d)}`

interface Cell {
  day: number | null
  key: string
}

export function CalendarPage() {
  const { user } = useAuth()
  const isPresident = user?.isPresident ?? false
  const { data: events } = useEvents()

  const today = new Date()
  const [year, setYear] = useState(today.getFullYear())
  const [month, setMonth] = useState(today.getMonth())

  const todayKey = dateKey(today.getFullYear(), today.getMonth(), today.getDate())

  const eventsByDay = useMemo(() => {
    const map = new Map<string, CalendarEvent[]>()
    for (const e of events ?? []) {
      const list = map.get(e.date) ?? []
      list.push(e)
      map.set(e.date, list)
    }
    return map
  }, [events])

  const cells = useMemo<Cell[]>(() => {
    const firstWeekday = new Date(year, month, 1).getDay()
    const daysInMonth = new Date(year, month + 1, 0).getDate()
    return Array.from({ length: 42 }, (_, i) => {
      const day = i - firstWeekday + 1
      const valid = day >= 1 && day <= daysInMonth
      return { day: valid ? day : null, key: valid ? dateKey(year, month, day) : `blank-${i}` }
    })
  }, [year, month])

  function shift(delta: number) {
    const d = new Date(year, month + delta, 1)
    setYear(d.getFullYear())
    setMonth(d.getMonth())
  }

  function goToday() {
    setYear(today.getFullYear())
    setMonth(today.getMonth())
  }

  return (
    <div>
      <div className="mb-6 flex flex-wrap items-center justify-between gap-4">
        <PageHeader title="Calendário" description="Eventos compartilhados da república." />
        {isPresident && (
          <EventFormDialog
            defaultDate={todayKey}
            trigger={
              <Button>
                <Plus /> Novo evento
              </Button>
            }
          />
        )}
      </div>

      <div className="mb-4 flex items-center gap-2">
        <Button variant="outline" size="icon" onClick={() => shift(-1)} aria-label="Mês anterior">
          <ChevronLeft />
        </Button>
        <Button variant="outline" size="icon" onClick={() => shift(1)} aria-label="Próximo mês">
          <ChevronRight />
        </Button>
        <h2 className="ml-1 text-lg font-semibold">
          {MONTHS[month]} {year}
        </h2>
        <Button variant="ghost" size="sm" onClick={goToday} className="ml-auto">
          Hoje
        </Button>
      </div>

      <Card className="overflow-hidden p-0">
        <div className="grid grid-cols-7 border-b bg-muted/50 text-center text-xs font-medium text-muted-foreground">
          {WEEKDAYS.map((w) => (
            <div key={w} className="py-2">
              {w}
            </div>
          ))}
        </div>
        <div className="grid grid-cols-7">
          {cells.map((cell) => {
            const dayEvents = cell.day ? (eventsByDay.get(cell.key) ?? []) : []
            const isToday = cell.key === todayKey
            return (
              <div
                key={cell.key}
                className={cn(
                  'group min-h-24 border-b border-r p-1.5 last:border-r-0 [&:nth-child(7n)]:border-r-0',
                  cell.day ? 'bg-background' : 'bg-muted/20',
                )}
              >
                {cell.day && (
                  <>
                    <div className="mb-1 flex items-center justify-between">
                      <span
                        className={cn(
                          'flex h-6 w-6 items-center justify-center rounded-full text-xs',
                          isToday && 'bg-primary font-semibold text-primary-foreground',
                        )}
                      >
                        {cell.day}
                      </span>
                      {isPresident && (
                        <EventFormDialog
                          defaultDate={cell.key}
                          trigger={
                            <button
                              className="text-muted-foreground opacity-0 transition-opacity hover:text-foreground focus:opacity-100 group-hover:opacity-100"
                              aria-label={`Adicionar evento em ${cell.key}`}
                            >
                              <Plus className="size-3.5" />
                            </button>
                          }
                        />
                      )}
                    </div>
                    <div className="space-y-1">
                      {dayEvents.map((e) =>
                        isPresident ? (
                          <EventFormDialog
                            key={e.id}
                            event={e}
                            trigger={
                              <button
                                title={e.description ?? undefined}
                                className="block w-full truncate rounded bg-primary/10 px-1.5 py-0.5 text-left text-xs font-medium text-primary hover:bg-primary/20"
                              >
                                {e.title}
                              </button>
                            }
                          />
                        ) : (
                          <div
                            key={e.id}
                            title={e.description ?? undefined}
                            className="truncate rounded bg-primary/10 px-1.5 py-0.5 text-xs font-medium text-primary"
                          >
                            {e.title}
                          </div>
                        ),
                      )}
                    </div>
                  </>
                )}
              </div>
            )
          })}
        </div>
      </Card>
    </div>
  )
}
