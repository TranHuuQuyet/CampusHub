import type { LoginRequest, RegisterRequest } from '../types/auth'

export type LoginErrors = Partial<Record<keyof LoginRequest, string>>
export type RegisterErrors = Partial<Record<keyof RegisterRequest, string>>

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

// Kiem tra du lieu dang nhap va tra ve loi theo tung truong.
export function validateLogin(data: LoginRequest): LoginErrors {
  const errors: LoginErrors = {}
  const email = data.email.trim()

  if (!email) {
    errors.email = 'Email is required.'
  } else if (email.length > 254) {
    errors.email = 'Email is too long.'
  } else if (!EMAIL_PATTERN.test(email)) {
    errors.email = 'Invalid email format.'
  }

  if (!data.password) {
    errors.password = 'Password is required.'
  } else if (data.password.length < 8) {
    errors.password = 'Password must be at least 8 characters.'
  } else if (data.password.length > 128) {
    errors.password = 'Password is too long.'
  }

  return errors
}

// Kiem tra du lieu dang ky va tra ve loi theo tung truong.
export function validateRegister(data: RegisterRequest): RegisterErrors {
  const errors: RegisterErrors = {
    ...validateLogin({ email: data.email, password: data.password }),
  }
  const fullName = data.fullName.trim()

  if (!fullName) {
    errors.fullName = 'Full name is required.'
  } else if (fullName.length > 100) {
    errors.fullName = 'Full name is too long.'
  }

  if (!data.confirmPassword) {
    errors.confirmPassword = 'Please confirm your password.'
  } else if (data.confirmPassword !== data.password) {
    errors.confirmPassword = 'Passwords do not match.'
  }

  return errors
}

