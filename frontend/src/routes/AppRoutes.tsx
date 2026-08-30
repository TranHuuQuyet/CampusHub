import { Route, Routes } from 'react-router-dom'
import ProtectedRoute from '../features/auth/components/ProtectedRoute'
import MainLayout from '../layouts/MainLayout'
import CommunityPage from '../pages/CommunityPage'
import HomePage from '../pages/HomePage'
import LoginPage from '../pages/LoginPage'
import LostFoundPage from '../pages/LostFoundPage'
import MarketplacePage from '../pages/MarketplacePage'
import NotFoundPage from '../pages/NotFoundPage'
import ProfilePage from '../pages/ProfilePage'
import RegisterPage from '../pages/RegisterPage'

// Khai bao cac route cong khai va route can dang nhap cua ung dung.
function AppRoutes() {
  return (
    <Routes>
      <Route element={<MainLayout />}>
        <Route index element={<HomePage />} />
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegisterPage />} />
        <Route path="community" element={<CommunityPage />} />
        <Route path="marketplace" element={<MarketplacePage />} />
        <Route path="lost-found" element={<LostFoundPage />} />

        <Route element={<ProtectedRoute />}>
          <Route path="profile" element={<ProfilePage />} />
        </Route>

        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}

export default AppRoutes
