<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { post } from '../lib/request.js'
import { isLoggedIn } from '../lib/auth.js'
import { showToast } from '../lib/ui.js'

const router = useRouter()
const form = reactive({
  username: '',
  realName: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  studentId: '',
})
const errors = reactive({
  username: '',
  realName: '',
  password: '',
  confirmPassword: '',
})
const selectedTags = ref(new Set())
const loading = ref(false)

const tagOptions = [
  '教育辅导',
  '环境保护',
  '医疗救护',
  '敬老助残',
  '文化宣传',
  '应急救援',
]

onMounted(() => {
  if (isLoggedIn()) router.replace('/activities')
})

function toggleTag(tag) {
  const s = selectedTags.value
  if (s.has(tag)) s.delete(tag)
  else s.add(tag)
  selectedTags.value = new Set(s)
}

function clearAllErrors() {
  errors.username = ''
  errors.realName = ''
  errors.password = ''
  errors.confirmPassword = ''
}

async function handleRegister() {
  clearAllErrors()
  const username = form.username.trim()
  const realName = form.realName.trim()
  const password = form.password
  const confirmPassword = form.confirmPassword

  let valid = true
  if (username.length < 3 || username.length > 50) {
    errors.username = '用户名需3-50个字符'
    valid = false
  }
  if (!realName) {
    errors.realName = '请输入真实姓名'
    valid = false
  }
  if (password.length < 6 || password.length > 50) {
    errors.password = '密码需6-50个字符'
    valid = false
  }
  if (password !== confirmPassword) {
    errors.confirmPassword = '两次密码不一致'
    valid = false
  }
  if (!valid) return

  loading.value = true
  try {
    const body = { username, password, realName }
    const email = form.email.trim()
    const phone = form.phone.trim()
    const studentId = form.studentId.trim()
    if (email) body.email = email
    if (phone) body.phone = phone
    if (studentId) body.studentId = studentId
    if (selectedTags.value.size > 0) body.tags = [...selectedTags.value]
    await post('/api/auth/register', body)
    showToast('注册成功，请登录', 'success')
    setTimeout(() => router.replace('/login'), 1000)
  } catch {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-wrapper">
    <div class="auth-card wide">
      <div class="logo">志</div>
      <h1>志愿者注册</h1>
      <p class="subtitle">加入我们，开启您的志愿服务之旅</p>

      <form @submit.prevent="handleRegister">
        <div class="form-row">
          <div class="form-group" :class="{ 'has-error': !!errors.username }">
            <label class="form-label">用户名 <span class="required">*</span></label>
            <input
              v-model="form.username"
              class="form-input"
              type="text"
              placeholder="至少3个字符"
              required
            />
            <div class="form-error">{{ errors.username }}</div>
          </div>
          <div class="form-group" :class="{ 'has-error': !!errors.realName }">
            <label class="form-label">真实姓名 <span class="required">*</span></label>
            <input
              v-model="form.realName"
              class="form-input"
              type="text"
              placeholder="请输入真实姓名"
              required
            />
            <div class="form-error">{{ errors.realName }}</div>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group" :class="{ 'has-error': !!errors.password }">
            <label class="form-label">密码 <span class="required">*</span></label>
            <input
              v-model="form.password"
              class="form-input"
              type="password"
              placeholder="至少6个字符"
              required
            />
            <div class="form-error">{{ errors.password }}</div>
          </div>
          <div class="form-group" :class="{ 'has-error': !!errors.confirmPassword }">
            <label class="form-label">确认密码 <span class="required">*</span></label>
            <input
              v-model="form.confirmPassword"
              class="form-input"
              type="password"
              placeholder="再次输入密码"
              required
            />
            <div class="form-error">{{ errors.confirmPassword }}</div>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">邮箱</label>
            <input
              v-model="form.email"
              class="form-input"
              type="email"
              placeholder="example@email.com"
            />
          </div>
          <div class="form-group">
            <label class="form-label">手机号</label>
            <input
              v-model="form.phone"
              class="form-input"
              type="tel"
              placeholder="13800138000"
            />
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">学号</label>
          <input
            v-model="form.studentId"
            class="form-input"
            type="text"
            placeholder="请输入学号（选填）"
          />
        </div>

        <div class="form-group">
          <label class="form-label">擅长领域（可选）</label>
          <div class="tag-group">
            <span
              v-for="tag in tagOptions"
              :key="tag"
              class="tag-chip"
              :class="{ selected: selectedTags.has(tag) }"
              @click="toggleTag(tag)"
              >{{ tag }}</span
            >
          </div>
        </div>

        <button
          type="submit"
          class="btn btn-primary btn-block"
          style="margin-top: 8px"
          :disabled="loading"
        >
          {{ loading ? '注册中...' : '⚇ 注册' }}
        </button>
      </form>

      <p style="margin-top: 20px; font-size: 13px; color: var(--text-secondary)">
        已有账号？ <RouterLink to="/login">立即登录</RouterLink>
      </p>
    </div>
  </div>
</template>
