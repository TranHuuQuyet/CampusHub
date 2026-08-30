export type LoginRequest = {
  email: string
  password: string
}

export type RegisterRequest = {
  fullName: string
  email: string
  password: string
  confirmPassword: string
}

export type User = {
  id: string
  fullName: string
  email: string
}

export type AuthResponse = {
  user: User
}

export type ApiError = {
  message: string
  fieldErrors?: Record<string, string>
}
