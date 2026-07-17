import {
  LayoutDashboard,
  Users,
  Calendar,
  ListChecks,
  Wallet,
  Megaphone,
  UserPlus,
  type LucideIcon,
} from 'lucide-react'

export interface NavItem {
  label: string
  to: string
  icon: LucideIcon
  /** Only shown to a President. */
  presidentOnly?: boolean
}

export const navItems: NavItem[] = [
  { label: 'Painel', to: '/', icon: LayoutDashboard },
  { label: 'Membros', to: '/members', icon: Users },
  { label: 'Solicitações', to: '/requests', icon: UserPlus, presidentOnly: true },
  { label: 'Calendário', to: '/calendar', icon: Calendar },
  { label: 'Responsabilidades', to: '/responsibilities', icon: ListChecks },
  { label: 'Finanças', to: '/finance', icon: Wallet },
  { label: 'Avisos', to: '/announcements', icon: Megaphone },
]
