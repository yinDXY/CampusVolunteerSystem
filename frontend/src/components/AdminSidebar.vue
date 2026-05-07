<script setup>
import { RouterLink } from 'vue-router'

defineProps({
  activePage: {
    type: String,
    required: true,
  },
})

const items = [
  { key: 'activities', label: '活动管理', icon: '📋', to: '/admin/activities' },
  { key: 'registrations', label: '报名审核', icon: '📝', to: '/admin/registrations' },
  { key: 'dispatch', label: '智能派岗', icon: '🔀', to: '/admin/dispatch' },
  { key: 'checkin', label: '签到管理', icon: '📱', to: '/admin/checkin' },
  { key: 'stats', label: '数据统计', icon: '📊', to: '/admin/stats' },
]
</script>

<template>
  <aside class="sidebar">
    <div class="sidebar-nav">
      <RouterLink
        v-for="item in items"
        :key="item.key"
        :to="item.to"
        :class="{ active: activePage === item.key }"
      >
        <span class="icon">{{ item.icon }}</span>
        {{ item.label }}
      </RouterLink>
    </div>
  </aside>
</template>
