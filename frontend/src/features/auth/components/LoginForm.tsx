import { useState } from 'react'
import type { FormEvent } from 'react'
import type { LoginRequest } from '../types/auth'

type LoginErrors = {
  email?: string
  password?: string
}

function LoginForm() {
  const [formData, setFormData] = useState<LoginRequest>({
    email: '',
    password: '',
  })

  // bat va xu ly loi khi client nhap sai data
  const [errors, setErrors] = useState<LoginErrors>({})

  function validateForm() {
    const newErrors: LoginErrors = {}

    if (!formData.email.trim()) {
      // bat buoc nhap email
      newErrors.email = 'Email is required.'
    } else if (!formData.email.includes('@')) {
      // email phai co ky tu @
      newErrors.email = 'Please enter a valid email.'
    }

    if (!formData.password) {
      // bat buoc nhap password
      newErrors.password = 'Password is required.'
    } else if (formData.password.length < 8) {
      // chieu dai password toi thieu la 8 ky tu
      newErrors.password = 'Password must be at least 8 characters.'
    }

    // cap nhat danh sach loi
    setErrors(newErrors)

    // neu khong co loi nao thi form hop le
    return Object.keys(newErrors).length === 0
  }

  // xu ly event submit form
  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    // ngan trinh duyet reload lai trang khi submit form
    event.preventDefault()

    const isValid = validateForm()

    if (!isValid) {
      // neu form khong hop le thi dung lai
      return
    }

    // du lieu hop le -> tam thoi in ra console
    // sau nay se thay bang viec goi API backend
    console.log('Login data:', formData)
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      <div>
        <label htmlFor="email">Email</label>

        {/* o nhap email */}
        <input
          id="email"
          type="email"
          value={formData.email}
          onChange={(event) =>
            setFormData({
              // giu lai cac gia tri cu trong formData
              ...formData,

              // cap nhat email moi
              email: event.target.value,
            })
          }
        />

        {/* neu email co loi thi hien thi loi */}
        {errors.email && <p>{errors.email}</p>}
      </div>

      <div>
        <label htmlFor="password">Password</label>

        {/* o nhap password */}
        <input
          id="password"
          type="password"
          value={formData.password}
          onChange={(event) =>
            setFormData({
              // giu lai cac gia tri cu trong formData
              ...formData,

              // cap nhat password moi
              password: event.target.value,
            })
          }
        />

        {/* neu password co loi thi hien thi loi */}
        {errors.password && <p>{errors.password}</p>}
      </div>

      {/* submit form */}
      <button type="submit">Login</button>
    </form>
  )
}

export default LoginForm