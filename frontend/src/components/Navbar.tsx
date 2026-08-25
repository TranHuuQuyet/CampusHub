import { NavLink } from 'react-router-dom'

function Navbar() {
  const getLinkClassName = ({ isActive }: { isActive: boolean }) =>
    isActive ? 'active' : ''

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
      <NavLink to="/profile" className={getLinkClassName}>
        Profile
      </NavLink>
      {' | '}
      <NavLink to="/login" className={getLinkClassName}>
        Login
      </NavLink>
      {' | '}
      <NavLink to="/register" className={getLinkClassName}>
        Register
      </NavLink>
    </nav>
  )
}

export default Navbar