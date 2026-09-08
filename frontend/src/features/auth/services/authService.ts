import type {
  ApiError,
  AuthResponse,
  LoginRequest,
  RegisterRequest,
} from '../types/auth'

const API_URL = import.meta.env.VITE_API_BASE_URL

/*
 * Doc gia tri cua cookie theo ten.
 *
 * Backend Spring Security luu CSRF token trong:
 *
 * XSRF-TOKEN
 *
 * Cookie nay KHONG HttpOnly de React co the doc.
 *
 * JSESSIONID van la HttpOnly va React khong can doc.
 */
function getCookie(name: string): string | null {
  const cookies = document.cookie.split(';')

  for (const cookie of cookies) {
    const [cookieName, ...cookieValueParts] = cookie.trim().split('=')

    if (cookieName === name) {
      return decodeURIComponent(cookieValueParts.join('='))
    }
  }

  return null
}

/*
 * =========================================================
 * CSRF
 * =========================================================
 *
 * Truoc cac request thay doi du lieu:
 *
 * POST
 * PUT
 * PATCH
 * DELETE
 *
 * React goi:
 *
 * GET /api/v1/csrf
 *
 * Backend se tao / refresh:
 *
 * XSRF-TOKEN cookie
 *
 * Sau do React doc cookie va gui lai qua:
 *
 * X-XSRF-TOKEN header
 */
async function getCsrfToken(): Promise<string> {
  const response = await fetch(`${API_URL}/csrf`, {
    method: 'GET',
    credentials: 'include',
  })

  if (!response.ok) {
    throw new Error('Unable to initialize security token.')
  }

  /*
   * Khong dung token trong JSON response.
   *
   * SPA flow cua CampusHub doc raw token
   * tu XSRF-TOKEN cookie.
   */
  const token = getCookie('XSRF-TOKEN')

  if (!token) {
    throw new Error('Security token was not provided by the server.')
  }

  return token
}

/*
 * Doc thong bao loi tu backend
 * va tao loi than thien cho giao dien.
 */
async function createApiError(response: Response): Promise<Error> {
  try {
    const data = (await response.json()) as Partial<ApiError>

    if (data.message) {
      return new Error(data.message)
    }
  } catch {
    /*
     * Bo qua neu backend khong tra ve JSON hop le.
     */
  }

  if (response.status === 429) {
    return new Error('Too many attempts. Please try again later.')
  }

  return new Error('Something went wrong. Please try again.')
}

/*
 * =========================================================
 * LOGIN
 * =========================================================
 *
 * 1. Lay CSRF token.
 * 2. Gui credentials.
 * 3. Browser luu JSESSIONID.
 *
 * Sau login thanh cong backend se rotate CSRF token.
 */
export async function login(
  credentials: LoginRequest,
): Promise<AuthResponse> {
  const csrfToken = await getCsrfToken()

  const response = await fetch(`${API_URL}/auth/login`, {
    method: 'POST',

    headers: {
      'Content-Type': 'application/json',
      'X-XSRF-TOKEN': csrfToken,
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

/*
 * =========================================================
 * REGISTER
 * =========================================================
 */
export async function register(
  data: RegisterRequest,
): Promise<AuthResponse> {
  const csrfToken = await getCsrfToken()

  const response = await fetch(`${API_URL}/auth/register`, {
    method: 'POST',

    headers: {
      'Content-Type': 'application/json',
      'X-XSRF-TOKEN': csrfToken,
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

/*
 * =========================================================
 * GET CURRENT USER
 * =========================================================
 *
 * GET khong can gui CSRF token.
 *
 * Browser gui JSESSIONID thong qua:
 *
 * credentials: 'include'
 */
export async function getCurrentUser(): Promise<AuthResponse | null> {
  const response = await fetch(`${API_URL}/auth/me`, {
    method: 'GET',
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

/*
 * =========================================================
 * LOGOUT
 * =========================================================
 *
 * Login thanh cong rotate CSRF token.
 *
 * Vi vay truoc logout ta lay token moi.
 *
 * POST logout:
 *
 * - invalidate HttpSession
 * - xoa JSESSIONID
 * - clear XSRF-TOKEN
 * - HTTP 204
 */
export async function logout(): Promise<void> {
  const csrfToken = await getCsrfToken()

  const response = await fetch(`${API_URL}/auth/logout`, {
    method: 'POST',

    headers: {
      'X-XSRF-TOKEN': csrfToken,
    },

    credentials: 'include',
  })

  if (!response.ok && response.status !== 401) {
    throw await createApiError(response)
  }
}
