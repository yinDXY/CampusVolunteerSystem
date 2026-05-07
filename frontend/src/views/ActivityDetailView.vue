<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import { get, post, del } from '../lib/request.js'
import { showToast, showConfirm } from '../lib/ui.js'
import {
  activityStatusInfo,
  registrationStatusInfo,
  formatDate,
  getCategoryTag,
  DETAIL_GRADIENTS,
} from '../utils/display.js'

const route = useRoute()
const router = useRouter()
const activityId = computed(() => Number(route.params.id))

const loading = ref(true)
const loadFailed = ref(false)
const activity = ref(null)
const positions = ref([])
const myRegistration = ref(null)
const selectedPositionId = ref(null)

const bannerGrad = computed(() => {
  if (!activity.value) return DETAIL_GRADIENTS[0]
  return DETAIL_GRADIENTS[activity.value.id % DETAIL_GRADIENTS.length]
})

function extractTags(text) {
  const patterns = text.match(/[\u4e00-\u9fa5a-zA-Z]{2,8}/g)
  if (patterns && patterns.length <= 6) {
    return patterns.slice(0, 4)
  }
  return []
}

async function loadAll() {
  if (!activityId.value) return
  loading.value = true
  loadFailed.value = false
  try {
    const [actData, posData, regData] = await Promise.all([
      get(`/api/activities/${activityId.value}`),
      get(`/api/activities/${activityId.value}/positions`),
      get('/api/registrations/my', { pageNum: 1, pageSize: 100 }),
    ])
    activity.value = actData
    positions.value = posData || []
    const myRegs = (regData.list || []).filter(
      (r) => r.activityId === activityId.value && r.status !== 4,
    )
    myRegistration.value = myRegs.length > 0 ? myRegs[0] : null
    selectedPositionId.value = null
  } catch {
    loadFailed.value = true
    activity.value = null
  } finally {
    loading.value = false
  }
}

function selectPosition(posId) {
  selectedPositionId.value = selectedPositionId.value === posId ? null : posId
}

async function submitRegistration() {
  if (!selectedPositionId.value) return
  try {
    await post('/api/registrations', { activityId: activityId.value })
    showToast('报名成功！', 'success')
    await loadAll()
  } catch {
    /* toast in request */
  }
}

async function cancelRegistration() {
  if (!myRegistration.value) return
  const ok = await showConfirm('取消报名', '确定要取消报名吗？此操作不可撤销。')
  if (!ok) return
  try {
    await del(`/api/registrations/${myRegistration.value.id}`)
    showToast('已取消报名', 'success')
    await loadAll()
  } catch {
    /* */
  }
}

watch(
  () => route.params.id,
  () => {
    if (!activityId.value || Number.isNaN(activityId.value)) {
      router.replace('/activities')
      return
    }
    loadAll()
  },
  { immediate: true },
)
</script>

<template>
  <div class="detail-container">
    <RouterLink to="/activities" class="back-link">‹ 返回活动列表</RouterLink>

    <div v-if="loading" class="spinner" />

    <template v-else-if="loadFailed">
      <p style="text-align: center; padding: 40px; color: var(--text-secondary)">
        活动不存在或加载失败
      </p>
    </template>

    <template v-else-if="activity">
      <div class="banner">
        <div class="bg" :style="{ background: bannerGrad }" />
        <span class="badge" :class="activityStatusInfo(activity.status).cls">{{
          activityStatusInfo(activity.status).text
        }}</span>
        <span class="tag-label">{{ getCategoryTag(activity.title, activity.description) }}</span>
      </div>

      <h1 class="detail-title">{{ activity.title }}</h1>

      <div class="info-grid">
        <div class="info-item">
          <span class="icon">📅</span>
          <div>
            <div class="label">活动时间</div>
            <div class="value">
              {{ formatDate(activity.startTime)
              }}{{ activity.endTime ? ' - ' + formatDate(activity.endTime) : '' }}
            </div>
          </div>
        </div>
        <div class="info-item">
          <span class="icon">🏢</span>
          <div>
            <div class="label">主办单位</div>
            <div class="value">{{ activity.creatorName || '未知' }}</div>
          </div>
        </div>
        <div class="info-item">
          <span class="icon">📍</span>
          <div>
            <div class="label">活动地点</div>
            <div class="value">{{ activity.location || '待定' }}</div>
          </div>
        </div>
        <div class="info-item">
          <span class="icon">👥</span>
          <div>
            <div class="label">报名情况</div>
            <div class="value">{{ activity.signedCount || 0 }}/{{ activity.totalQuota || 0 }} 人已报名</div>
          </div>
        </div>
      </div>

      <hr class="hr" />

      <h2 class="section-title">活动介绍</h2>
      <p class="desc-block">{{ activity.description || '暂无描述' }}</p>

      <hr class="hr" />

      <h2 class="section-title">岗位列表</h2>
      <p v-if="positions.length === 0" style="color: var(--text-secondary)">暂无岗位信息</p>
      <div
        v-for="pos in positions"
        v-else-if="positions.length > 0"
        :key="pos.id"
        class="position-card"
        :class="{ selected: selectedPositionId === pos.id }"
      >
        <div style="flex: 1">
          <div class="pos-header">
            <span class="pos-name">{{ pos.name }}</span>
            <span v-if="myRegistration && myRegistration.positionId === pos.id" class="badge badge-approved"
              >已报名</span
            >
            <span
              v-else-if="(pos.assignedCount || 0) >= pos.quota"
              class="badge"
              style="background: #f1f5f9; color: #64748b"
              >已满</span
            >
          </div>
          <div class="pos-desc">{{ pos.description || '' }}</div>
          <div v-if="pos.description && extractTags(pos.description).length" class="pos-tags">
            <span v-for="(t, ti) in extractTags(pos.description)" :key="ti" class="pos-tag">{{ t }}</span>
          </div>
          <div class="pos-quota">名额：{{ pos.assignedCount || 0 }}/{{ pos.quota }}</div>
        </div>
        <button
          v-if="!myRegistration && activity.status === 1 && (pos.assignedCount || 0) < pos.quota"
          type="button"
          class="btn btn-outline btn-sm btn-select"
          @click="selectPosition(pos.id)"
        >
          选择
        </button>
      </div>

      <template v-if="myRegistration">
        <div class="action-box registered">
          <div class="action-title">✔ 您已报名此活动</div>
          <div class="action-info">报名岗位：{{ myRegistration.positionName || '待分配' }}</div>
          <div class="action-info">
            审核状态：{{ registrationStatusInfo(myRegistration.status).text }}
          </div>
        </div>
        <button
          v-if="myRegistration.status === 0 || myRegistration.status === 1"
          type="button"
          class="btn btn-danger btn-block"
          style="margin-top: 16px"
          @click="cancelRegistration"
        >
          取消报名
        </button>
      </template>
      <template v-else-if="activity.status !== 1">
        <div class="action-box closed">
          <div style="display: flex; align-items: center; gap: 8px; color: var(--text-secondary)">
            ⓘ 该活动当前不接受报名
          </div>
        </div>
      </template>
      <button
        v-else
        type="button"
        class="btn btn-primary btn-block"
        style="margin-top: 24px"
        :style="
          selectedPositionId
            ? {}
            : { opacity: 0.5, cursor: 'not-allowed', background: '#9ca3af' }
        "
        :disabled="!selectedPositionId"
        @click="submitRegistration"
      >
        确认报名
      </button>
    </template>
  </div>
</template>

<style scoped>
.detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px 48px;
}
.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--text-secondary);
  text-decoration: none;
  margin-bottom: 16px;
  font-size: 14px;
}
.back-link:hover {
  color: var(--primary);
}
.banner {
  position: relative;
  height: 220px;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 24px;
}
.banner .bg {
  width: 100%;
  height: 100%;
}
.banner .badge {
  position: absolute;
  top: 16px;
  right: 16px;
}
.tag-label {
  position: absolute;
  bottom: 16px;
  left: 16px;
  background: rgba(255, 255, 255, 0.9);
  padding: 4px 14px;
  border-radius: 6px;
  font-size: 13px;
}
.detail-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 20px;
}
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px 48px;
  margin-bottom: 20px;
}
.info-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}
.info-item .icon {
  font-size: 20px;
  color: var(--primary);
  flex-shrink: 0;
  margin-top: 2px;
}
.info-item .label {
  font-size: 12px;
  color: var(--text-secondary);
}
.info-item .value {
  font-size: 15px;
  color: var(--text-primary);
  font-weight: 500;
}
.section-title {
  font-size: 18px;
  font-weight: 600;
  margin: 24px 0 16px;
}
.hr {
  border: none;
  border-top: 1px solid var(--border);
  margin: 20px 0;
}
.desc-block {
  color: var(--text-primary);
  line-height: 1.8;
}
.position-card {
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  transition: border-color 0.2s;
}
.position-card.selected {
  border-color: var(--primary);
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.1);
}
.pos-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}
.pos-name {
  font-size: 16px;
  font-weight: 600;
}
.pos-desc {
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 8px;
}
.pos-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}
.pos-tag {
  background: var(--bg-page);
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  color: var(--text-secondary);
}
.pos-quota {
  font-size: 13px;
  color: var(--text-secondary);
}
.btn-select {
  flex-shrink: 0;
  align-self: center;
}
.action-box {
  border-radius: 10px;
  padding: 20px 24px;
  margin-top: 24px;
}
.action-box.registered {
  background: #eff6ff;
}
.action-box.closed {
  background: var(--bg-page);
}
.action-title {
  font-weight: 600;
  color: var(--primary);
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.action-info {
  font-size: 14px;
  color: var(--primary);
}
@media (max-width: 640px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
