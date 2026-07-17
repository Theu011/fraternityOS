import { NavLink } from 'react-router-dom'
import { Building2 } from 'lucide-react'
import { navItems } from '@/routes/nav'
import { useAuth } from '@/features/auth/auth-context'
import { cn } from '@/lib/utils'

export function Sidebar() {
  const { user } = useAuth()
  const items = navItems.filter((item) => !item.presidentOnly || user?.isPresident)
  return (
    <aside className="hidden w-64 shrink-0 flex-col bg-sidebar text-sidebar-foreground md:flex">
      <div className="flex h-16 items-center gap-2 border-b border-sidebar-border px-6">
        <div className="flex h-8 w-8 items-center justify-center rounded-md bg-primary text-primary-foreground">
          <Building2 className="size-5" />
        </div>
        <span className="text-lg font-semibold">FraternityOS</span>
      </div>
      <nav className="flex-1 space-y-1 p-3">
        {items.map(({ label, to, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            end={to === '/'}
            className={({ isActive }) =>
              cn(
                'flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors',
                isActive
                  ? 'bg-sidebar-accent text-sidebar-accent-foreground'
                  : 'text-sidebar-foreground/70 hover:bg-sidebar-accent/60 hover:text-sidebar-accent-foreground',
              )
            }
          >
            <Icon className="size-4" />
            {label}
          </NavLink>
        ))}
      </nav>
      <div className="border-t border-sidebar-border p-4 text-xs text-sidebar-foreground/50">
        MVP · v0.1
      </div>
      {/* Nota: "FraternityOS" é o nome do produto e permanece em inglês. */}
    </aside>
  )
}
