import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '@/features/auth/auth-context'

/**
 * Gates the house dashboard: an authenticated user with no house is sent to
 * onboarding (create or join a house). Assumes ProtectedRoute already ensured a
 * user is present.
 */
export function RequireHouse() {
  const { user } = useAuth()
  if (user && !user.houseId) return <Navigate to="/onboarding" replace />
  return <Outlet />
}
