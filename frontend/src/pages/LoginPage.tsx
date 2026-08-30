import LoginForm from '../features/auth/components/LoginForm'

// Hien thi trang dang nhap tai khoan.
function LoginPage() {
  return (
    <main>
      <h1>Login</h1>
      <p>Sign in to your CampusHub account</p>
      <LoginForm />
    </main>
  )
}

export default LoginPage
