import { createContext, useContext, useMemo, useState, type ReactNode } from 'react'
import { api, setToken } from '@/lib/api'
import { isPresident, isTreasurer, type AuthResponse, type AuthUser } from './types'

const USER_KEY = 'fos.user'

function toUser(data: AuthResponse): AuthUser {
  const positions = data.positions ?? []
  return {
    userId: data.userId,
    membershipId: data.membershipId,
    houseId: data.houseId,
    name: data.name,
    positions,
    isPresident: isPresident(positions),
    isTreasurer: isTreasurer(positions),
  }
}

interface AuthContextValue {
  user: AuthUser | null
  login: (email: string, password: string) => Promise<void>
  register: (name: string, email: string, password: string) => Promise<void>
  /** Replace the session from a fresh auth response (e.g. after creating a house). */
  applyAuth: (data: AuthResponse) => void
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

function loadUser(): AuthUser | null {
  const raw = localStorage.getItem(USER_KEY)
  return raw ? (JSON.parse(raw) as AuthUser) : null
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(loadUser)

  const value = useMemo<AuthContextValue>(() => {
    function applyAuth(data: AuthResponse) {
      setToken(data.token)
      const nextUser = toUser(data)
      localStorage.setItem(USER_KEY, JSON.stringify(nextUser))
      setUser(nextUser)
    }
    return {
      user,
      applyAuth,
      async login(email, password) {
        const { data } = await api.post<AuthResponse>('/auth/login', { email, password })
        applyAuth(data)
      },
      async register(name, email, password) {
        const { data } = await api.post<AuthResponse>('/auth/register', { name, email, password })
        applyAuth(data)
      },
      logout() {
        setToken(null)
        localStorage.removeItem(USER_KEY)
        setUser(null)
      },
    }
  }, [user])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider')
  return ctx
}
