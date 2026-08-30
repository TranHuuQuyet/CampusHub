import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../features/auth/hooks/useAuth'

// Hien thi dieu huong phu hop voi trang thai dang nhap.
function Navbar() {
  const navigate = useNavigate()
  const { isAuthenticated, logout, user } = useAuth()

  // Tra ve class active cho lien ket cua trang hien tai.
  const getLinkClassName = ({ isActive }: { isActive: boolean }) =>
    isActive ? 'active' : ''

  // Dang xuat va dua nguoi dung ve trang dang nhap.
  async function handleLogout() {
    try {
      await logout()
    } finally {
      navigate('/login', { replace: true })
    }
  }

  return (
    <nav>
      <NavLink to="/" className={getLinkClassName}>
        Home
      </NavLink>
      {' | '}
      <NavLink to="/community" className={getLinkClassName}>
        Community
      </NavLink>
      {' | '}
      <NavLink to="/marketplace" className={getLinkClassName}>
        Marketplace
      </NavLink>
      {' | '}
      <NavLink to="/lost-found" className={getLinkClassName}>
        Lost & Found
      </NavLink>
      {' | '}
      {isAuthenticated ? (
        <>
          {' | '}
          <NavLink to="/profile" className={getLinkClassName}>
            {user?.fullName ?? 'Profile'}
          </NavLink>
          {' | '}
          <button type="button" onClick={() => void handleLogout()}>
            Logout
          </button>
        </>
      ) : (
        <>
          {' | '}
          <NavLink to="/login" className={getLinkClassName}>
            Login
          </NavLink>
          {' | '}
          <NavLink to="/register" className={getLinkClassName}>
            Register
          </NavLink>
        </>
      )}
    </nav>
  )
}

export default Navbar
