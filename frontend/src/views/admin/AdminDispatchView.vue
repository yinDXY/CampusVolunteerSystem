<script setup>
import { ref, onMounted, watch } from 'vue'
import { get, post, del } from '../../lib/request.js'
import { showToast, showConfirm } from '../../lib/ui.js'

const activities = ref([])
const activitySel = ref('')
const positionSel = ref('')
const positions = ref([])
const posInfoVisible = ref(false)
const posDesc = ref('—')
const posReqs = ref('—')
const posRemain = ref(0)
const candidates = ref([])
const loadingCandidates = ref(false)
const currentPositionData = ref(null)

async function loadActivities() {
  try {
    const data = await get('/api/activities', { pageNum: 1, pageSize: 100 })
    activities.value = data.list || []
  } catch {
    /* */
  }
}

watch(activitySel, async (actId) => {
  positionSel.value = ''
  positions.value = []
  hidePositionInfo()
  showEmptyState('请选择岗位')
  if (!actId) return
  try {
    const list = await get(`/api/activities/${actId}/positions`)
    positions.value = list || []
  } catch {
    /* */
  }
})

watch(positionSel, (posId) => {
  if (!posId) {
    hidePositionInfo()
    showEmptyState('请选择岗位')
    return
  }
  const p = positions.value.find((x) => String(x.id) === posId)
  if (!p) return
  const quota = Number(p.quota) || 0
  const assigned = Number(p.assignedCount) || 0
  const remain = Math.max(0, quota - assigned)
  posInfoVisible.value = true
  posDesc.value = p.description || '—'
  posReqs.value = p.requirements || '—'
  posRemain.value = remain
  currentPositionData.value = { id: Number(posId), remain }
  loadCandidates(posId, remain)
})

function hidePositionInfo() {
  posInfoVisible.value = false
  currentPositionData.value = null
}

async function loadCandidates(positionId, remain) {
  loadingCandidates.value = true
  candidates.value = []
  try {
    const data = await get(`/api/dispatch/recommend/${positionId}`)
    renderCandidates(data.candidates || [], remain)
  } catch {
    candidates.value = []
    showEmptyState('加载推荐列表失败', '⚠️', '')
  } finally {
    loadingCandidates.value = false
  }
}

function renderCandidates(list, remain) {
  if (list.length === 0) {
    candidates.value = []
    showEmptyState('暂无符合条件的志愿者', '🔍', '可能暂无通过审核且未被派岗的报名者')
    return
  }
  const maxScore = list[0]?.matchScore || 1
  candidates.value = list.map((c, i) => ({
    ...c,
    _i: i,
    _remain: remain,
    _maxScore: maxScore,
  }))
}

function matchPct(c, maxScore) {
  const m = maxScore > 0 ? Math.round((c.matchScore / (maxScore + 1)) * 95 + 5) : 50
  return m
}

function skillHtml(c) {
  const raw = c.volunteerTags || c.skills || ''
  if (!raw) return null
  return raw
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean)
}

async function assign(regId, name) {
  if (!currentPositionData.value) return
  if (currentPositionData.value.remain <= 0) {
    showToast('该岗位名额已满', 'error')
    return
  }
  const ok = await showConfirm('确认派岗', `确定将「${name}」派到此岗位？`)
  if (!ok) return
  try {
    await post('/api/dispatch/assign', {
      registrationId: regId,
      positionId: currentPositionData.value.id,
    })
    showToast('派岗成功', 'success')
    currentPositionData.value.remain--
    posRemain.value = currentPositionData.value.remain
    loadCandidates(currentPositionData.value.id, currentPositionData.value.remain)
  } catch {
    /* */
  }
}

async function cancelAssign(regId) {
  const ok = await showConfirm('取消派岗', '确定取消该志愿者的派岗？')
  if (!ok) return
  try {
    await del(`/api/dispatch/assign/${regId}`)
    showToast('已取消派岗', 'success')
    currentPositionData.value.remain++
    posRemain.value = currentPositionData.value.remain
    loadCandidates(currentPositionData.value.id, currentPositionData.value.remain)
  } catch {
    /* */
  }
}

function showEmptyState(msg, icon = '👥', sub = '系统将为您推荐最合适的志愿者') {
  candidates.value = []
  emptyState.value = { msg, icon, sub }
}

const emptyState = ref({ msg: '请先选择活动和岗位', icon: '👥', sub: '系统将为您推荐最合适的志愿者' })

onMounted(loadActivities)
</script>

<template>
  <div>
    <div class="page-title">智能派岗</div>
    <div class="page-subtitle">根据志愿者能力和活动需求，智能推荐合适人选</div>

    <div class="select-bar">
      <div class="select-group">
        <label><span class="req">* </span>选择活动</label>
        <select v-model="activitySel" class="cascade-select">
          <option value="">请选择活动</option>
          <option v-for="a in activities" :key="a.id" :value="String(a.id)">{{ a.title }}</option>
        </select>
      </div>
      <div class="select-group">
        <label><span class="req">* </span>选择岗位</label>
        <select v-model="positionSel" class="cascade-select" :disabled="!activitySel">
          <option value="">请选择岗位</option>
          <option v-for="p in positions" :key="p.id" :value="String(p.id)">
            {{ p.name }} ({{ p.assignedCount ?? 0 }}/{{ p.quota }})
          </option>
        </select>
      </div>
    </div>

    <div v-show="posInfoVisible" class="position-info-card">
      <div class="info-row">岗位描述：<strong>{{ posDesc }}</strong></div>
      <div class="info-row">岗位要求：<strong>{{ posReqs }}</strong></div>
      <div class="info-row">
        剩余名额：<strong style="color: var(--primary)">{{ posRemain }}</strong> 个
      </div>
    </div>

    <div v-if="loadingCandidates" class="empty-state">
      <div class="empty-icon">⏳</div>
      <div>推荐中...</div>
    </div>
    <div v-else-if="candidates.length === 0" class="empty-state">
      <div class="empty-icon">{{ emptyState.icon }}</div>
      <div>{{ emptyState.msg }}</div>
      <div v-if="emptyState.sub" style="margin-top: 4px; font-size: 12px">{{ emptyState.sub }}</div>
    </div>
    <div v-else>
      <div class="candidates-header">智能推荐志愿者</div>
      <div class="candidates-sub">按匹配度排序</div>
      <div
        v-for="c in candidates"
        :key="c.registrationId"
        class="candidate-card"
        :class="{ assigned: c.registrationStatus === 3 }"
      >
        <div class="candidate-avatar" :class="'av-' + (c._i % 6)">
          {{ (c.volunteerName || c.realName || '?').charAt(0) }}
        </div>
        <div class="candidate-info">
          <div class="candidate-name">{{ c.volunteerName || c.realName || '—' }}</div>
          <div class="candidate-hours">🕐 服务时长：{{ c.totalHours ?? 0 }} 小时</div>
        </div>
        <div class="candidate-metrics">
          <div class="metric-group">
            <span class="metric-label">匹配度</span>
            <div class="match-bar-wrap">
              <div class="match-bar">
                <div class="match-bar-fill" :style="{ width: matchPct(c, c._maxScore) + '%' }" />
              </div>
              <span class="match-pct">{{ matchPct(c, c._maxScore) }}%</span>
            </div>
          </div>
          <div class="metric-group">
            <span class="metric-label">擅长技能</span>
            <div class="skill-tags">
              <template v-if="skillHtml(c)?.length">
                <span v-for="(s, si) in skillHtml(c)" :key="si" class="skill-tag">{{ s }}</span>
              </template>
              <span v-else style="color: var(--text-secondary); font-size: 12px">—</span>
            </div>
          </div>
          <div class="metric-group">
            <span class="metric-label">可用性</span>
            <span
              v-if="c.qualified || c.requiredCount === 0 || c.matchedCount >= c.requiredCount"
              class="avail-badge avail-yes"
              >可用</span
            >
            <span v-else-if="c.matchedCount > 0" class="avail-badge avail-part">部分可用</span>
            <span v-else class="avail-badge avail-no">不匹配</span>
          </div>
        </div>
        <button
          v-if="c.registrationStatus === 3"
          type="button"
          class="btn-cancel-assign"
          @click="cancelAssign(c.registrationId)"
        >
          ✕ 取消派岗
        </button>
        <button
          v-else
          type="button"
          class="btn-assign"
          :disabled="!currentPositionData || currentPositionData.remain <= 0"
          @click="assign(c.registrationId, c.volunteerName || c.realName || '')"
        >
          <span>👤＋</span> 派岗
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}
.page-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 24px;
}
.req {
  color: var(--danger);
}
.select-bar {
  background: #fff;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  padding: 20px 24px;
  margin-bottom: 20px;
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  align-items: flex-end;
}
.select-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
  min-width: 220px;
}
.select-group label {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
}
.cascade-select {
  padding: 10px 14px;
  border: 1px solid var(--border);
  border-radius: 8px;
  font-size: 14px;
  color: var(--text-primary);
  background: #fff;
  cursor: pointer;
  outline: none;
  width: 100%;
  transition: border-color 0.15s;
}
.cascade-select:focus {
  border-color: var(--primary);
}
.cascade-select:disabled {
  background: #f3f4f6;
  cursor: not-allowed;
  color: var(--text-secondary);
}
.position-info-card {
  background: var(--primary-light);
  border: 1px solid #c7d9f8;
  border-radius: var(--radius-md);
  padding: 16px 20px;
  margin-bottom: 20px;
  font-size: 13px;
  color: var(--text-secondary);
}
.position-info-card .info-row {
  margin-bottom: 6px;
}
.position-info-card strong {
  color: var(--text-primary);
}
.empty-state {
  text-align: center;
  padding: 80px 0;
  color: var(--text-secondary);
  font-size: 14px;
}
.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}
.candidates-header {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}
.candidates-sub {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 16px;
}
.candidate-card {
  background: #fff;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  padding: 20px 24px;
  margin-bottom: 14px;
  display: flex;
  align-items: center;
  gap: 20px;
  transition:
    border-color 0.15s,
    box-shadow 0.15s;
}
.candidate-card:hover {
  border-color: var(--primary);
  box-shadow: var(--shadow-md);
}
.candidate-card.assigned {
  opacity: 0.55;
}
.candidate-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  flex-shrink: 0;
}
.av-0 {
  background: linear-gradient(135deg, #667eea, #764ba2);
}
.av-1 {
  background: linear-gradient(135deg, #f093fb, #f5576c);
}
.av-2 {
  background: linear-gradient(135deg, #4facfe, #00f2fe);
}
.av-3 {
  background: linear-gradient(135deg, #43e97b, #38f9d7);
}
.av-4 {
  background: linear-gradient(135deg, #fa709a, #fee140);
}
.av-5 {
  background: linear-gradient(135deg, #a18cd1, #fbc2eb);
}
.candidate-info {
  flex: 1;
  min-width: 0;
}
.candidate-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 2px;
}
.candidate-hours {
  font-size: 12px;
  color: var(--text-secondary);
}
.candidate-metrics {
  display: flex;
  gap: 32px;
  align-items: flex-start;
  flex-wrap: wrap;
}
.metric-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 100px;
}
.metric-label {
  font-size: 11px;
  color: var(--text-muted);
}
.match-bar-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}
.match-bar {
  width: 100px;
  height: 6px;
  background: #e9ecef;
  border-radius: 3px;
  overflow: hidden;
}
.match-bar-fill {
  height: 100%;
  background: var(--primary);
  border-radius: 3px;
}
.match-pct {
  font-size: 13px;
  font-weight: 600;
  color: var(--primary);
}
.skill-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}
.skill-tag {
  background: var(--primary-light);
  color: var(--primary);
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 500;
}
.avail-badge {
  font-size: 12px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 10px;
}
.avail-yes {
  background: var(--success-bg);
  color: var(--success);
}
.avail-part {
  background: var(--warning-bg);
  color: #d97706;
}
.avail-no {
  background: var(--danger-bg);
  color: var(--danger);
}
.btn-assign {
  background: var(--primary);
  color: #fff;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition:
    background 0.15s,
    opacity 0.15s;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}
.btn-assign:hover:not(:disabled) {
  background: var(--primary-hover);
}
.btn-assign:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}
.btn-cancel-assign {
  background: var(--danger-bg);
  color: var(--danger);
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.15s;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}
.btn-cancel-assign:hover {
  background: #fecaca;
}
@media (max-width: 768px) {
  .candidate-card {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
