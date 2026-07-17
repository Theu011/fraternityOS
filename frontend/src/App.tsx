import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { ProtectedRoute } from '@/components/ProtectedRoute'
import { RequireHouse } from '@/components/RequireHouse'
import { AppLayout } from '@/components/layout/AppLayout'
import { LoginPage } from '@/features/auth/LoginPage'
import { SignupPage } from '@/features/auth/SignupPage'
import { OnboardingPage } from '@/features/house/OnboardingPage'
import { RequestsPage } from '@/features/house/RequestsPage'
import { DashboardPage } from '@/features/dashboard/DashboardPage'
import { MembersPage } from '@/features/members/MembersPage'
import { CalendarPage } from '@/features/calendar/CalendarPage'
import { ResponsibilitiesPage } from '@/features/responsibilities/ResponsibilitiesPage'
import { FinancePage } from '@/features/finance/FinancePage'
import { AnnouncementsPage } from '@/features/announcements/AnnouncementsPage'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route element={<ProtectedRoute />}>
          <Route path="onboarding" element={<OnboardingPage />} />
          <Route element={<RequireHouse />}>
            <Route element={<AppLayout />}>
              <Route index element={<DashboardPage />} />
              <Route path="members" element={<MembersPage />} />
              <Route path="requests" element={<RequestsPage />} />
              <Route path="calendar" element={<CalendarPage />} />
              <Route path="responsibilities" element={<ResponsibilitiesPage />} />
              <Route path="finance" element={<FinancePage />} />
              <Route path="announcements" element={<AnnouncementsPage />} />
            </Route>
          </Route>
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
