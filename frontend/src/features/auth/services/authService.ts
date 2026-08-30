import type {
  ApiError,
  AuthResponse,
  LoginRequest,
  RegisterRequest,
} from '../types/auth'

const API_URL = import.meta.env.VITE_API_BASE_URL

// Doc thong bao loi tu backend va tao loi than thien cho giao dien.
async function createApiError(response: Response): Promise<Error> {
  try {
    const data = (await response.json()) as Partial<ApiError>

    if (data.message) {
      return new Error(data.message)
    }
  } catch {
    // Bo qua khi backend khong tra ve JSON hop le.
  }

  if (response.status === 429) {
    return new Error('Too many attempts. Please try again later.')
  }

  return new Error('Something went wrong. Please try again.')
}

// Gui thong tin dang nhap va nhan lai nguoi dung da xac thuc.
export async function login(
  credentials: LoginRequest,
): Promise<AuthResponse> {
  const response = await fetch(`${API_URL}/auth/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
    body: JSON.stringify({
      email: credentials.email.trim(),
      password: credentials.password,
    }),
  })

  if (response.status === 401) {
    throw new Error('Invalid email or password.')
  }

  if (response.status === 429) {
    throw new Error('Too many login attempts. Please try again later.')
  }

  if (!response.ok) {
    throw await createApiError(response)
  }

  return response.json() as Promise<AuthResponse>
}

// Gui thong tin dang ky va nhan lai nguoi dung moi.
export async function register(
  data: RegisterRequest,
): Promise<AuthResponse> {
  const response = await fetch(`${API_URL}/auth/register`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
    body: JSON.stringify({
      fullName: data.fullName.trim(),
      email: data.email.trim(),
      password: data.password,
    }),
  })

  if (response.status === 409) {
    throw new Error('An account with this email already exists.')
  }

  if (!response.ok) {
    throw await createApiError(response)
  }

  return response.json() as Promise<AuthResponse>
}

// Lay nguoi dung hien tai tu cookie phien khi tai lai trang.
export async function getCurrentUser(): Promise<AuthResponse | null> {
  const response = await fetch(`${API_URL}/auth/me`, {
    credentials: 'include',
  })

  if (response.status === 401) {
    return null
  }

  if (!response.ok) {
    throw await createApiError(response)
  }

  return response.json() as Promise<AuthResponse>
}

// Yeu cau backend huy phien dang nhap hien tai.
export async function logout(): Promise<void> {
  const response = await fetch(`${API_URL}/auth/logout`, {
    method: 'POST',
    credentials: 'include',
  })

  if (!response.ok && response.status !== 401) {
    throw await createApiError(response)
  }
}
