import Navbar from './components/Navbar'
import { Route, Routes } from 'react-router-dom'
import CommunityPage from './pages/CommunityPage'
import HomePage from './pages/HomePage'
import LoginPage from './pages/LoginPage'
import LostFoundPage from './pages/LostFoundPage'
import MarketplacePage from './pages/MarketplacePage'
import NotFoundPage from './pages/NotFoundPage'
import ProfilePage from './pages/ProfilePage'
import RegisterPage from './pages/RegisterPage'

function App() {
  return (
    <>
      <Navbar />

      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/community" element={<CommunityPage />} />
        <Route path="/marketplace" element={<MarketplacePage />} />
        <Route path="/lost-found" element={<LostFoundPage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </>
  )
}

export default App  