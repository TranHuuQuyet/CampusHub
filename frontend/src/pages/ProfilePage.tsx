import { useAuth } from '../features/auth/hooks/useAuth'

// Hien thi thong tin cua nguoi dung dang dang nhap.
function ProfilePage() {
  const { user } = useAuth()

  return (
    <main>
      <h1>Profile</h1>
      <p>{user?.fullName}</p>
      <p>{user?.email}</p>
    </main>
  )
}

export default ProfilePage
