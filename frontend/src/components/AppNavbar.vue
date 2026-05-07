<script setup>
import { computed } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { getRole, getRealName, getUsername, clearAuth, roleName } from '../lib/auth.js'

defineProps({
  activePage: {
    type: String,
    default: '',
  },
})

const router = useRouter()
const role = computed(() => getRole())
const displayName = computed(() => getRealName() || getUsername())

function logout() {
  clearAuth()
  router.replace('/login')
}
</script>

<template>
  <nav class="navbar">
    <RouterLink class="navbar-brand" to="/activities">
      <span class="navbar-logo">志</span>
      志愿服务管理系统
    </RouterLink>
    <div class="navbar-nav">
      <RouterLink
        to="/activities"
        :class="{ active: activePage === 'activities' }"
      >
        活动大厅
      </RouterLink>
      <RouterLink
        v-if="role < 1"
        to="/profile"
        :class="{ active: activePage === 'profile' }"
      >
        个人中心
      </RouterLink>
      <RouterLink
        v-if="role >= 1"
        to="/admin/activities"
        :class="{ active: activePage === 'admin' }"
      >
        ○ 管理后台
      </RouterLink>
    </div>
    <div class="navbar-right">
      <span
        >⛁ <span class="user-name">{{ roleName(role) }} · {{ displayName }}</span></span
      >
      <a
        href="javascript:void(0)"
        style="color: var(--text-secondary)"
        @click.prevent="logout"
        >↪ 退出</a
      >
    </div>
  </nav>
</template>
