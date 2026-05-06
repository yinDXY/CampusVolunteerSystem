<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { post } from '../lib/request.js'
import { setAuth, isLoggedIn, getRole } from '../lib/auth.js'
import { showToast } from '../lib/ui.js'

const router = useRouter()
const username = ref('')
const password = ref('')
const usernameError = ref('')
const passwordError = ref('')
const loading = ref(false)

onMounted(() => {
  if (isLoggedIn()) {
    router.replace(getRole() >= 1 ? '/admin/activities' : '/activities')
  }
})

function showFieldError(field, msg) {
  if (field === 'username') usernameError.value = msg
  if (field === 'password') passwordError.value = msg
}

function clearFieldError(field) {
  if (field === 'username') usernameError.value = ''
  if (field === 'password') passwordError.value = ''
}

async function handleLogin() {
  const u = username.value.trim()
  const p = password.value

  let valid = true
  if (!u) {
    showFieldError('username', '请输入用户名')
    valid = false
  } else clearFieldError('username')
  if (p.length < 6) {
    showFieldError('password', '密码至少6个字符')
    valid = false
  } else clearFieldError('password')
  if (!valid) return

  loading.value = true
  try {
    const data = await post('/api/auth/login', { username: u, password: p })
    setAuth(data)
    showToast('登录成功', 'success')
    setTimeout(() => {
      router.replace(data.role >= 1 ? '/admin/activities' : '/activities')
    }, 500)
  } catch {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-wrapper">
    <div class="auth-card">
      <div class="logo">志</div>
      <h1>志愿服务管理系统</h1>
      <p class="subtitle">欢迎回来，请登录您的账号</p>

      <form @submit.prevent="handleLogin">
        <div class="form-group" :class="{ 'has-error': !!usernameError }">
          <label class="form-label">用户名</label>
          <input
            v-model="username"
            class="form-input"
            type="text"
            placeholder="请输入用户名"
            required
          />
          <div class="form-error">{{ usernameError }}</div>
        </div>
        <div class="form-group" :class="{ 'has-error': !!passwordError }">
          <label class="form-label">密码</label>
          <input
            v-model="password"
            class="form-input"
            type="password"
            placeholder="请输入密码"
            required
          />
          <div class="form-error">{{ passwordError }}</div>
        </div>
        <button
          type="submit"
          class="btn btn-primary btn-block"
          style="margin-top: 8px"
          :disabled="loading"
        >
          {{ loading ? '登录中...' : '→) 登录' }}
        </button>
      </form>

      <p style="margin-top: 20px; font-size: 13px; color: var(--text-secondary)">
        还没有账号？ <RouterLink to="/register">立即注册</RouterLink>
      </p>

      <div class="test-accounts">
        <strong>测试账号：</strong><br />
        超级管理员：admin / 123456<br />
        活动管理员：manager / 123456<br />
        普通志愿者：user / 123456
      </div>
    </div>
  </div>
</template>
