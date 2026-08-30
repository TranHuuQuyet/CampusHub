import { Outlet } from 'react-router-dom'
import Navbar from '../components/Navbar'

// Tao bo cuc chung gom thanh dieu huong va noi dung cua tung trang.
function MainLayout() {
  return (
    <div className="app-layout">
      <header className="app-header">
        <strong>CampusHub</strong>
        <Navbar />
      </header>

      <main className="app-main">
        <Outlet />
      </main>
    </div>
  )
}

export default MainLayout
