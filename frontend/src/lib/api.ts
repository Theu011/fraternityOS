import axios from 'axios'

const TOKEN_KEY = 'fos.token'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string | null) {
  if (token) localStorage.setItem(TOKEN_KEY, token)
  else localStorage.removeItem(TOKEN_KEY)
}

// baseURL '/api' is rewritten to the Spring API by the Vite dev proxy.
export const api = axios.create({ baseURL: '/api' })

api.interceptors.request.use((config) => {
  const token = getToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Token expired/invalid: drop it and bounce to login (except on the login call itself).
    if (error.response?.status === 401 && !error.config?.url?.includes('/auth/')) {
      setToken(null)
      if (window.location.pathname !== '/login') window.location.assign('/login')
    }
    return Promise.reject(error)
  },
)
