import { showToast } from './ui.js'
import { clearAuth } from './auth.js'

const BASE_URL = import.meta.env.VITE_API_BASE || 'http://localhost:8080'

async function request(url, options = {}) {
  const { method = 'GET', body, params } = options

  let fullUrl = BASE_URL + url
  if (params) {
    const qs = new URLSearchParams()
    for (const [k, v] of Object.entries(params)) {
      if (v !== undefined && v !== null && v !== '') qs.append(k, v)
    }
    const queryStr = qs.toString()
    if (queryStr) fullUrl += (fullUrl.includes('?') ? '&' : '?') + queryStr
  }

  const headers = {}
  const token = localStorage.getItem('token')
  if (token) headers['Authorization'] = 'Bearer ' + token
  if (body !== undefined) headers['Content-Type'] = 'application/json'

  let res
  try {
    res = await fetch(fullUrl, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    })
  } catch {
    showToast('网络异常，请检查网络连接', 'error')
    throw { code: -1, msg: '网络异常' }
  }

  if (res.status === 401) {
    clearAuth()
    showToast('登录已过期，请重新登录', 'error')
    setTimeout(() => {
      window.location.href = '/login'
    }, 1000)
    throw { code: 401, msg: '未登录或Token已过期' }
  }

  const result = await res.json()

  if (result.code !== 200) {
    showToast(result.msg || '操作失败', 'error')
    throw { code: result.code, msg: result.msg }
  }

  return result.data
}

export function get(url, params) {
  return request(url, { method: 'GET', params })
}

export function post(url, body) {
  return request(url, { method: 'POST', body })
}

export function put(url, body) {
  return request(url, { method: 'PUT', body })
}

export function patch(url, body) {
  return request(url, { method: 'PATCH', body })
}

export function del(url) {
  return request(url, { method: 'DELETE' })
}
