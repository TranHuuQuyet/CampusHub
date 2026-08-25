import './App.css'

function App() {
  return (
    <main className="health-check">
      <section className="health-card">
        <span className="status-badge">● Frontend healthy</span>

        <h1>CampusHub</h1>

        <p className="description">
          CampusHub frontend is running successfully.
        </p>

        <div className="tech-stack">
          <span>React</span>
          <span>TypeScript</span>
          <span>Vite</span>
        </div>
      </section>
    </main>
  )
}

export default App