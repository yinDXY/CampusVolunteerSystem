<script setup>
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { get } from '../lib/request.js'
import { debounce } from '../utils/debounce.js'
import {
  activityStatusInfo,
  formatDate,
  getCategoryTag,
  GRADIENTS,
} from '../utils/display.js'
import PaginationBar from '../components/PaginationBar.vue'

const keyword = ref('')
const statusFilter = ref('')
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 9
const loadError = ref(false)

const loadDebounced = debounce(() => {
  pageNum.value = 1
  loadActivities()
}, 400)

function onKeywordInput() {
  loadDebounced()
}

function onStatusChange() {
  pageNum.value = 1
  loadActivities()
}

async function loadActivities() {
  loadError.value = false
  const params = { pageNum: pageNum.value, pageSize }
  const kw = keyword.value.trim()
  if (kw) params.keyword = kw
  if (statusFilter.value !== '') params.status = Number(statusFilter.value)
  try {
    const data = await get('/api/activities', params)
    total.value = data.total || 0
    list.value = data.list || []
  } catch {
    loadError.value = true
    list.value = []
  }
}

function setPage(p) {
  pageNum.value = p
  loadActivities()
}

onMounted(() => {
  loadActivities()
})
</script>

<template>
  <div class="page-header">
    <h1>活动大厅</h1>
    <p>探索志愿服务机会，用行动传递温暖</p>
  </div>

  <div class="search-bar" style="max-width: 1200px; margin: 0 auto 16px">
    <div style="display: flex; gap: 16px; align-items: center; flex-wrap: wrap">
      <div style="flex: 1; min-width: 240px; position: relative">
        <input
          v-model="keyword"
          class="form-input"
          type="text"
          placeholder="🔍 搜索活动名称、类别..."
          style="padding-left: 12px"
          @input="onKeywordInput"
        />
      </div>
      <select
        v-model="statusFilter"
        class="form-input"
        style="width: 160px"
        @change="onStatusChange"
      >
        <option value="">🔻 全部状态</option>
        <option value="1">报名中</option>
        <option value="2">进行中</option>
        <option value="3">已结束</option>
        <option value="0">草稿</option>
        <option value="4">已取消</option>
      </select>
    </div>
    <p style="margin-top: 12px; color: var(--primary); font-size: 14px">
      找到 {{ total }} 个活动
    </p>
  </div>

  <div
    v-if="loadError"
    class="activity-grid"
    style="max-width: 1200px; margin: 0 auto"
  >
    <p style="text-align: center; color: var(--text-secondary)">加载失败，请稍后重试</p>
  </div>
  <div
    v-else
    class="activity-grid"
    style="max-width: 1200px; margin: 0 auto"
  >
    <template v-if="list.length === 0">
      <p
        style="
          text-align: center;
          color: var(--text-secondary);
          grid-column: 1 / -1;
          padding: 60px 0;
        "
      >
        暂无活动
      </p>
    </template>
    <template v-else-if="list.length > 0">
    <RouterLink
      v-for="(a, i) in list"
      :key="a.id"
      :to="'/activities/' + a.id"
      class="activity-card"
    >
      <div class="cover" :style="a.coverUrl
        ? { backgroundImage: 'url(' + a.coverUrl + ')', backgroundSize: 'cover', backgroundPosition: 'center' }
        : { background: GRADIENTS[i % GRADIENTS.length] }">
        <span class="badge" :class="activityStatusInfo(a.status).cls" style="position: absolute; top: 12px; right: 12px">
          {{ activityStatusInfo(a.status).text }}
        </span>
        <span
          style="
            position: absolute;
            bottom: 12px;
            left: 12px;
            background: rgba(255, 255, 255, 0.85);
            padding: 3px 10px;
            border-radius: 4px;
            font-size: 12px;
            color: #333;
          "
          >{{ getCategoryTag(a.title, a.description) }}</span
        >
      </div>
      <div class="body">
        <h3>{{ a.title }}</h3>
        <p class="desc">{{ a.description || '' }}</p>
        <div class="meta">
          <span
            >📅 {{ formatDate(a.startTime)
            }}{{ a.endTime ? ' - ' + formatDate(a.endTime) : '' }}</span
          >
          <span>📍 {{ a.location || '待定' }}</span>
          <span>👥 {{ a.signedCount || 0 }}/{{ a.totalQuota || 0 }} 人已报名</span>
        </div>
      </div>
      <div class="footer">
        <span style="color: var(--text-secondary); font-size: 13px">{{ a.creatorName || '' }}</span>
        <span class="link" style="font-size: 13px">查看详情 →</span>
      </div>
    </RouterLink>
    </template>
  </div>

  <PaginationBar
    style="max-width: 1200px; margin: 24px auto"
    :total="total"
    :page-num="pageNum"
    :page-size="pageSize"
    @update:page-num="setPage"
  />
</template>
