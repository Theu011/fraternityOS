import { useState, type FormEvent, type ReactNode } from 'react'
import axios from 'axios'
import { useUploadStatement } from './api'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'

const MONTHS = [
  'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
  'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro',
]
const selectClass =
  'flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring'

export function UploadStatementDialog({ trigger }: { trigger: ReactNode }) {
  const now = new Date()
  const [open, setOpen] = useState(false)
  const [month, setMonth] = useState(String(now.getMonth() + 1))
  const [year, setYear] = useState(String(now.getFullYear()))
  const [amount, setAmount] = useState('')
  const [notes, setNotes] = useState('')
  const [file, setFile] = useState<File | null>(null)
  const [error, setError] = useState<string | null>(null)

  const upload = useUploadStatement()

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    if (!file) {
      setError('Selecione um arquivo PDF ou imagem.')
      return
    }
    const form = new FormData()
    form.append('month', month)
    form.append('year', year)
    form.append('amountPerMember', amount || '0')
    if (notes) form.append('notes', notes)
    form.append('file', file)
    try {
      await upload.mutateAsync(form)
      setAmount('')
      setNotes('')
      setFile(null)
      setOpen(false)
    } catch (err) {
      if (axios.isAxiosError(err) && err.response?.status === 409) {
        setError('Já existe um demonstrativo para esse mês.')
      } else if (axios.isAxiosError(err) && err.response?.status === 400) {
        setError('Somente arquivos PDF, PNG ou JPEG são permitidos.')
      } else {
        setError('Falha no envio. Tente novamente.')
      }
    }
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>{trigger}</DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Enviar demonstrativo</DialogTitle>
          <DialogDescription>Adicione um demonstrativo de aluguel mensal (PDF ou imagem).</DialogDescription>
        </DialogHeader>
        <form onSubmit={onSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1.5">
              <Label htmlFor="st-month">Mês</Label>
              <select
                id="st-month"
                className={selectClass}
                value={month}
                onChange={(e) => setMonth(e.target.value)}
              >
                {MONTHS.map((m, i) => (
                  <option key={m} value={i + 1}>
                    {m}
                  </option>
                ))}
              </select>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="st-year">Ano</Label>
              <Input
                id="st-year"
                type="number"
                required
                value={year}
                onChange={(e) => setYear(e.target.value)}
              />
            </div>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="st-amount">Valor por membro</Label>
            <Input
              id="st-amount"
              type="number"
              step="0.01"
              min="0"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              placeholder="450.00"
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="st-file">Arquivo do demonstrativo</Label>
            <Input
              id="st-file"
              type="file"
              accept="application/pdf,image/png,image/jpeg"
              required
              onChange={(e) => setFile(e.target.files?.[0] ?? null)}
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="st-notes">Observações</Label>
            <Textarea
              id="st-notes"
              rows={2}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="Opcional…"
            />
          </div>
          {error && <p className="text-sm text-destructive">{error}</p>}
          <DialogFooter className="gap-2">
            <DialogClose asChild>
              <Button type="button" variant="outline">
                Cancelar
              </Button>
            </DialogClose>
            <Button type="submit" disabled={upload.isPending}>
              {upload.isPending ? 'Enviando…' : 'Enviar'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
