import { useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import type { LoginRequest } from '../types/auth'
import {
  validateLogin,
  type LoginErrors,
} from '../utils/authValidation'

type LoginLocationState = {
  from?: string
}

// Hien thi form, kiem tra du lieu va xu ly yeu cau dang nhap.
function LoginForm() {
  const navigate = useNavigate()
  const location = useLocation()
  const { login } = useAuth()

  const [formData, setFormData] = useState<LoginRequest>({
    email: '',
    password: '',
  })

  const [errors, setErrors] = useState<LoginErrors>({})
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [serverError, setServerError] = useState<string | null>(null)

  // Cap nhat truong dang nhap va xoa loi cu cua truong do.
  function handleChange(event: ChangeEvent<HTMLInputElement>) {
    const { name, value } = event.target

    setFormData((currentData) => ({
      ...currentData,
      [name]: value,
    }))

    setErrors((currentErrors) => ({
      ...currentErrors,
      [name]: undefined,
    }))

    setServerError(null)
  }

  // Kiem tra du lieu, goi ham dang nhap va dieu huong khi thanh cong.
  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (isSubmitting) {
      return
    }

    const newErrors = validateLogin(formData)
    setErrors(newErrors)

    if (Object.keys(newErrors).length > 0) {
      return
    }

    try {
      setIsSubmitting(true)
      setServerError(null)

      await login(formData)
      const state = location.state as LoginLocationState | null
      const destination = state?.from?.startsWith('/') ? state.from : '/'
      navigate(destination, { replace: true })
    } catch (error) {
      setServerError(
        error instanceof Error
          ? error.message
          : 'Unable to sign in. Please try again.',
      )
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      {serverError && <p role="alert">{serverError}</p>}

      <div>
        <label htmlFor="email">Email</label>

        <input
          id="email"
          name="email"
          type="email"
          value={formData.email}
          maxLength={254}
          autoComplete="email"
          onChange={handleChange}
          aria-invalid={Boolean(errors.email)}
          aria-describedby={errors.email ? 'email-error' : undefined}
        />

        {errors.email && (
          <p id="email-error" role="alert">
            {errors.email}
          </p>
        )}
      </div>

      <div>
        <label htmlFor="password">Password</label>

        <input
          id="password"
          name="password"
          type="password"
          value={formData.password}
          maxLength={128}
          autoComplete="current-password"
          onChange={handleChange}
          aria-invalid={Boolean(errors.password)}
          aria-describedby={
            errors.password ? 'password-error' : undefined
          }
        />

        {errors.password && (
          <p id="password-error" role="alert">
            {errors.password}
          </p>
        )}
      </div>

      <button type="submit" disabled={isSubmitting}>
        {isSubmitting ? 'Logging in...' : 'Login'}
      </button>

      <p>
        Do not have an account? <Link to="/register">Register</Link>
      </p>
    </form>
  )
}

export default LoginForm
