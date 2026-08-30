import {
  useEffect,
  useState,
  type ReactNode,
} from 'react'
import {
  getCurrentUser,
  login as loginRequest,
  logout as logoutRequest,
  register as registerRequest,
} from '../services/authService'
import { AuthContext } from './authContextDefinition'
import type { LoginRequest, RegisterRequest, User } from '../types/auth'

type AuthProviderProps = {
  children: ReactNode
}

// Cung cap trang thai va cac thao tac xac thuc cho toan bo ung dung.
export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    let isMounted = true

    // Khoi phuc phien dang nhap tu backend khi ung dung duoc tai.
    async function restoreSession() {
      try {
        const response = await getCurrentUser()

        if (isMounted) {
          setUser(response?.user ?? null)
        }
      } catch {
        if (isMounted) {
          setUser(null)
        }
      } finally {
        if (isMounted) {
          setIsLoading(false)
        }
      }
    }

    void restoreSession()

    // Ngan cap nhat state neu provider da bi go bo.
    return () => {
      isMounted = false
    }
  }, [])

  // Dang nhap va luu nguoi dung vao trang thai dung chung.
  async function login(credentials: LoginRequest) {
    const response = await loginRequest(credentials)
    setUser(response.user)
  }

  // Dang ky tai khoan va luu nguoi dung neu backend tao phien ngay lap tuc.
  async function register(data: RegisterRequest) {
    const response = await registerRequest(data)
    setUser(response.user)
  }

  // Dang xuat tren backend va xoa nguoi dung khoi trang thai dung chung.
  async function logout() {
    try {
      await logoutRequest()
    } finally {
      setUser(null)
    }
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: user !== null,
        isLoading,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

