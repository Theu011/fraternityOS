import { Megaphone, Pin, Plus, Pencil, Trash2 } from 'lucide-react'
import { useAuth } from '@/features/auth/auth-context'
import { PageHeader } from '@/components/PageHeader'
import { Placeholder } from '@/components/Placeholder'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { useAnnouncements, useDeleteAnnouncement } from './api'
import { AnnouncementFormDialog } from './AnnouncementFormDialog'
import type { Announcement } from './types'

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('pt-BR', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  })
}

export function AnnouncementsPage() {
  const { user } = useAuth()
  const isPresident = user?.isPresident ?? false
  const { data, isLoading, isError } = useAnnouncements()
  const del = useDeleteAnnouncement()

  return (
    <div>
      <div className="mb-6 flex items-start justify-between gap-4">
        <PageHeader title="Avisos" description="Notícias da república e avisos fixados." />
        {isPresident && (
          <AnnouncementFormDialog
            trigger={
              <Button>
                <Plus /> Novo
              </Button>
            }
          />
        )}
      </div>

      {isLoading && <p className="text-sm text-muted-foreground">Carregando…</p>}
      {isError && <p className="text-sm text-destructive">Falha ao carregar os avisos.</p>}

      {data && data.length === 0 && (
        <Placeholder
          icon={Megaphone}
          message={
            isPresident
              ? 'Ainda não há avisos. Crie o primeiro.'
              : 'Ainda não há avisos.'
          }
        />
      )}

      {data && data.length > 0 && (
        <div className="space-y-4">
          {data.map((a: Announcement) => (
            <Card key={a.id}>
              <CardHeader className="flex-row items-start justify-between gap-4 space-y-0">
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <CardTitle>{a.title}</CardTitle>
                    {a.pinned && (
                      <Badge variant="secondary">
                        <Pin className="size-3" /> Fixado
                      </Badge>
                    )}
                  </div>
                  <p className="text-xs text-muted-foreground">
                    {a.authorName ?? 'Desconhecido'} · {formatDate(a.createdAt)}
                  </p>
                </div>
                {isPresident && (
                  <div className="flex shrink-0 gap-1">
                    <AnnouncementFormDialog
                      announcement={a}
                      trigger={
                        <Button variant="ghost" size="icon" aria-label="Editar">
                          <Pencil />
                        </Button>
                      }
                    />
                    <Button
                      variant="ghost"
                      size="icon"
                      aria-label="Excluir"
                      onClick={() => del.mutate(a.id)}
                    >
                      <Trash2 className="text-destructive" />
                    </Button>
                  </div>
                )}
              </CardHeader>
              <CardContent className="whitespace-pre-wrap text-sm text-foreground/90">
                {a.content}
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
