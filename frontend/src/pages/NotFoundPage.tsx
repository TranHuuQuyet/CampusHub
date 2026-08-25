import { Link } from 'react-router'

function NotFoundPage() {
  return (
    <section className="not-found">
      <h1>404</h1>

      <p>The page you are looking for does not exist.</p>

      <Link to="/">Back to CampusHub</Link>
    </section>
  )
}

export default NotFoundPage