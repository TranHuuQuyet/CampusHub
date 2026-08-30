import { Link } from 'react-router-dom'

// Hien thi thong bao khi duong dan khong ton tai.
function NotFoundPage() {
  return (
    <main>
      <h1>404</h1>
      <p>Page not found.</p>
      <Link to="/">Back to Home</Link>
    </main>
  )
}

export default NotFoundPage
