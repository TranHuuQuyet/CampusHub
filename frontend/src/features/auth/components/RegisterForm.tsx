import { useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import type { RegisterRequest } from '../types/auth'
import {
  type RegisterErrors,
  validateRegister,
} from '../utils/authValidation'

// Hien thi form, kiem tra du lieu va xu ly yeu cau dang ky.
function RegisterForm() {
  const navigate = useNavigate()
  const { register } = useAuth()
  const [formData, setFormData] = useState<RegisterRequest>({
    fullName: '',
    email: '',
    password: '',
    confirmPassword: '',
  })
  const [errors, setErrors] = useState<RegisterErrors>({})
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [serverError, setServerError] = useState<string | null>(null)

  // Cap nhat truong dang ky va xoa loi cu cua truong do.
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

  // Kiem tra du lieu, goi ham dang ky va dieu huong khi thanh cong.
  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (isSubmitting) {
      return
    }

    const newErrors = validateRegister(formData)
    setErrors(newErrors)

    if (Object.keys(newErrors).length > 0) {
      return
    }

    try {
      setIsSubmitting(true)
      setServerError(null)
      await register(formData)
      navigate('/', { replace: true })
    } catch (error) {
      setServerError(
        error instanceof Error
          ? error.message
          : 'Unable to register. Please try again.',
      )
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      {serverError && <p role="alert">{serverError}</p>}

      <div>
        <label htmlFor="fullName">Full name</label>
        <input
          id="fullName"
          name="fullName"
          type="text"
          value={formData.fullName}
          maxLength={100}
          autoComplete="name"
          onChange={handleChange}
          aria-invalid={Boolean(errors.fullName)}
          aria-describedby={errors.fullName ? 'full-name-error' : undefined}
        />
        {errors.fullName && (
          <p id="full-name-error" role="alert">
            {errors.fullName}
          </p>
        )}
      </div>

      <div>
        <label htmlFor="register-email">Email</label>
        <input
          id="register-email"
          name="email"
          type="email"
          value={formData.email}
          maxLength={254}
          autoComplete="email"
          onChange={handleChange}
          aria-invalid={Boolean(errors.email)}
          aria-describedby={errors.email ? 'register-email-error' : undefined}
        />
        {errors.email && (
          <p id="register-email-error" role="alert">
            {errors.email}
          </p>
        )}
      </div>

      <div>
        <label htmlFor="register-password">Password</label>
        <input
          id="register-password"
          name="password"
          type="password"
          value={formData.password}
          maxLength={128}
          autoComplete="new-password"
          onChange={handleChange}
          aria-invalid={Boolean(errors.password)}
          aria-describedby={errors.password ? 'register-password-error' : undefined}
        />
        {errors.password && (
          <p id="register-password-error" role="alert">
            {errors.password}
          </p>
        )}
      </div>

      <div>
        <label htmlFor="confirmPassword">Confirm password</label>
        <input
          id="confirmPassword"
          name="confirmPassword"
          type="password"
          value={formData.confirmPassword}
          maxLength={128}
          autoComplete="new-password"
          onChange={handleChange}
          aria-invalid={Boolean(errors.confirmPassword)}
          aria-describedby={
            errors.confirmPassword ? 'confirm-password-error' : undefined
          }
        />
        {errors.confirmPassword && (
          <p id="confirm-password-error" role="alert">
            {errors.confirmPassword}
          </p>
        )}
      </div>

      <button type="submit" disabled={isSubmitting}>
        {isSubmitting ? 'Creating account...' : 'Register'}
      </button>

      <p>
        Already have an account? <Link to="/login">Login</Link>
      </p>
    </form>
  )
}

export default RegisterForm
