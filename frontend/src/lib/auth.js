export function getToken() {
  return localStorage.getItem('token') || ''
}
export function getUserId() {
  return Number(localStorage.getItem('userId')) || 0
}
export function getRole() {
  return Number(localStorage.getItem('role')) || 0
}
export function getUsername() {
  return localStorage.getItem('username') || ''
}
export function getRealName() {
  return localStorage.getItem('realName') || ''
}

export function setAuth(loginData) {
  localStorage.setItem('token', loginData.token)
  localStorage.setItem('userId', String(loginData.userId))
  localStorage.setItem('role', String(loginData.role))
  localStorage.setItem('username', loginData.username)
  localStorage.setItem('realName', loginData.realName || '')
}

export function clearAuth() {
  localStorage.removeItem('token')
  localStorage.removeItem('userId')
  localStorage.removeItem('role')
  localStorage.removeItem('username')
  localStorage.removeItem('realName')
}

export function isLoggedIn() {
  return !!getToken()
}

export function roleName(role) {
  const map = { 0: '志愿者', 1: '活动管理员', 2: '系统管理员' }
  return map[role] || '未知'
}
