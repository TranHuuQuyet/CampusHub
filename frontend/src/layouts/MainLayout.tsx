import { Outlet } from 'react-router'

function MainLayout() {
  return (
    <div className="app-layout">
      <header className="app-header">
        <strong>CampusHub</strong>
      </header>

      <main className="app-main">
        <Outlet />
      </main>
    </div>
  )
}

export default MainLayout