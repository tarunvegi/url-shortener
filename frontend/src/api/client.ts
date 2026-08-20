import axios from 'axios'

const api = axios.create({
  baseURL: '/api/v1',
  headers: { 'Content-Type': 'application/json' }
})

export interface ShortenRequest {
  originalUrl: string
  customAlias?: string
  expiresAt?: string
}

export interface ShortenResponse {
  shortUrl: string
  code: string
  originalUrl: string
  expiresAt?: string
}

export interface UrlInfo {
  code: string
  shortUrl: string
  originalUrl: string
  customAlias?: string
  createdAt: string
  expiresAt?: string
  active: boolean
}

export interface AnalyticsData {
  code: string
  totalClicks: number
  clicksByDay: Record<string, number>
  topReferrers: Record<string, number>
}

export const urlApi = {
  shorten: (data: ShortenRequest) =>
    api.post<ShortenResponse>('/urls', data).then(r => r.data),

  listAll: () =>
    api.get<UrlInfo[]>('/urls').then(r => r.data),

  getByCode: (code: string) =>
    api.get<UrlInfo>(`/urls/${code}`).then(r => r.data),

  delete: (code: string) =>
    api.delete(`/urls/${code}`),

  getAnalytics: (code: string) =>
    api.get<AnalyticsData>(`/analytics/${code}`).then(r => r.data)
}
