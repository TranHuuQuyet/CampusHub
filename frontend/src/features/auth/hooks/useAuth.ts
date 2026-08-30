import { useContext } from 'react'
import { AuthContext } from '../context/authContextDefinition'

// Lay du lieu xac thuc va bao loi neu hook nam ngoai AuthProvider.
export function useAuth() {
  const context = useContext(AuthContext)

  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider.')
  }

  return context
}
