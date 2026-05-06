<script setup>
import { ref, onMounted } from 'vue'
import { get, patch } from '../../lib/request.js'
import { showToast, showConfirm } from '../../lib/ui.js'
import { registrationStatusInfo, formatTime } from '../../utils/display.js'
import PaginationBar from '../../components/PaginationBar.vue'

const activityOptions = ref([])
const activityFilter = ref('')
const statusFilter = ref('')
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 15
const loadError = ref(false)

const scoreModalOpen = ref(false)
const scoreTargetId = ref(null)
const scoreModalName = ref('')
const selectedScore = ref(0)
const starHover = ref(0)

async function loadActivityOptions() {
  try {
    const data = await get('/api/activities', { pageNum: 1, pageSize: 100 })
    activityOptions.value = data.list || []
  } catch {
    /* */
  }
}

async function load() {
  loadError.value = false
  const aid = activityFilter.value
  if (!aid) {
    total.value = 0
    list.value = []
    return
  }
  const params = { pageNum: pageNum.value, pageSize }
  if (statusFilter.value !== '') params.status = Number(statusFilter.value)
  try {
    const data = await get(`/api/registrations/activity/${aid}`, params)
    total.value = data.total || 0
    list.value = data.list || []
  } catch {
    loadError.value = true
    list.value = []
  }
}

function onFilterChange() {
  pageNum.value = 1
  load()
}

function setPage(p) {
  pageNum.value = p
  load()
}

async function review(regId, status) {
  const action = status === 1 ? '通过' : '拒绝'
  const ok = await showConfirm(`确认${action}`, `确定要${action}这条报名申请吗？`)
  if (!ok) return
  try {
    await patch(`/api/registrations/${regId}/status`, { status })
    showToast(`已${action}报名`, 'success')
    load()
  } catch {
    /* */
  }
}

function openScoreModal(regId, name) {
  scoreTargetId.value = regId
  selectedScore.value = 0
  starHover.value = 0
  scoreModalName.value = name
  scoreModalOpen.value = true
}

function closeScoreModal() {
  scoreModalOpen.value = false
  scoreTargetId.value = null
}

function setStars(val) {
  selectedScore.value = val
}

function displayStars(val) {
  starHover.value = val
}

function leaveStars() {
  starHover.value = 0
}

const starDisplay = ref(0)
function effectiveStars() {
  return starHover.value || selectedScore.value
}

async function confirmScore() {
  if (!selectedScore.value) {
    showToast('请先选择星级', 'error')
    return
  }
  try {
    await patch(`/api/registrations/${scoreTargetId.value}/score`, {
      score: selectedScore.value * 20,
    })
    showToast('评分成功', 'success')
    closeScoreModal()
    load()
  } catch {
    /* */
  }
}

onMounted(async () => {
  await loadActivityOptions()
  load()
})
</script>

<template>
  <div>
    <div class="page-title">报名审核</div>
    <div class="page-subtitle">审核志愿者报名申请，对已完成活动进行评分</div>

    <div class="filter-bar">
      <div class="filter-group">
        <label>选择活动</label>
        <select v-model="activityFilter" class="filter-select" @change="onFilterChange">
          <option value="">全部活动</option>
          <option v-for="a in activityOptions" :key="a.id" :value="String(a.id)">{{ a.title }}</option>
        </select>
      </div>
      <div class="filter-group">
        <label>审核状态</label>
        <select v-model="statusFilter" class="filter-select" @change="onFilterChange">
          <option value="">全部状态</option>
          <option value="0">待审核</option>
          <option value="1">已通过</option>
          <option value="2">已拒绝</option>
          <option value="3">已完成</option>
          <option value="4">已取消</option>
        </select>
      </div>
    </div>

    <p class="total-hint">
      <template v-if="!activityFilter">请先选择一个活动</template>
      <template v-else-if="loadError">加载失败</template>
      <template v-else>找到 {{ total }} 条报名记录</template>
    </p>

    <table class="reg-table">
      <thead>
        <tr>
          <th>志愿者</th>
          <th>活动</th>
          <th>报名岗位</th>
          <th>报名时间</th>
          <th>状态</th>
          <th>评分</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="!activityFilter">
          <td colspan="7" class="empty-state">请在上方选择活动后查看报名记录</td>
        </tr>
        <tr v-else-if="loadError">
          <td colspan="7" class="empty-state">加载失败，请稍后重试</td>
        </tr>
        <tr v-else-if="list.length === 0">
          <td colspan="7" class="empty-state">暂无报名记录</td>
        </tr>
        <template v-else-if="activityFilter && !loadError && list.length > 0">
        <tr v-for="r in list" :key="r.id">
          <td class="volunteer-cell">
            <div class="vol-name">{{ r.realName || r.volunteerName || r.userName || '—' }}</div>
            <div class="vol-id">{{ r.studentId || '' }}</div>
          </td>
          <td style="font-size: 13px">{{ r.activityTitle || r.title || '—' }}</td>
          <td style="font-size: 13px; color: var(--text-secondary)">{{ r.positionName || '—' }}</td>
          <td style="font-size: 13px; color: var(--text-secondary)">{{ formatTime(r.createdAt) }}</td>
          <td>
            <span class="badge" :class="registrationStatusInfo(r.status).cls">{{
              registrationStatusInfo(r.status).text
            }}</span>
          </td>
          <td>
            <span v-if="r.score == null" style="color: var(--text-secondary); font-size: 13px">未评分</span>
            <div v-else class="score-display">
              <span class="score-stars">{{ '★'.repeat(Math.round(r.score / 20)) }}{{ '☆'.repeat(5 - Math.round(r.score / 20)) }}</span>
              <span class="score-num">{{ r.score }}</span>
            </div>
          </td>
          <td>
            <div v-if="r.status === 0" class="ops-btns">
              <button type="button" class="btn-approve" @click="review(r.id, 1)">✓ 通过</button>
              <button type="button" class="btn-reject" @click="review(r.id, 2)">✕ 拒绝</button>
            </div>
            <button
              v-else-if="r.status === 3 && r.score == null"
              type="button"
              class="btn-score"
              @click="openScoreModal(r.id, r.realName || r.volunteerName || '')"
            >
              ★ 打分
            </button>
            <span v-else>—</span>
          </td>
        </tr>
        </template>
      </tbody>
    </table>

    <PaginationBar
      v-if="activityFilter"
      style="margin-top: 20px"
      :total="total"
      :page-num="pageNum"
      :page-size="pageSize"
      @update:page-num="setPage"
    />

    <div
      v-if="scoreModalOpen"
      class="score-modal"
      @click.self="closeScoreModal"
    >
      <div class="score-modal-inner" @click.stop>
        <div class="score-modal-title">为志愿者评分</div>
        <div class="score-modal-sub">为「{{ scoreModalName }}」的本次服务打分</div>
        <div
          class="star-picker"
          @mouseleave="leaveStars"
        >
          <span
            v-for="n in 5"
            :key="n"
            class="star"
            :class="{ active: n <= (starHover || selectedScore) }"
            @click="setStars(n)"
            @mouseenter="displayStars(n)"
            >★</span
          >
        </div>
        <div class="score-modal-actions">
          <button type="button" class="btn btn-primary" @click="confirmScore">确认评分</button>
          <button type="button" class="btn btn-outline" @click="closeScoreModal">取消</button>
        </div>
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
.filter-bar {
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
.filter-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
  min-width: 200px;
}
.filter-group label {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
}
.filter-select {
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
.filter-select:focus {
  border-color: var(--primary);
}
.total-hint {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 10px;
}
.reg-table {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}
.reg-table th {
  background: #f8fafc;
  padding: 13px 18px;
  text-align: left;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--border);
}
.reg-table td {
  padding: 13px 18px;
  font-size: 14px;
  color: var(--text-primary);
  border-bottom: 1px solid #f1f5f9;
  vertical-align: middle;
}
.reg-table tr:last-child td {
  border-bottom: none;
}
.reg-table tr:hover td {
  background: #f8fafc;
}
.volunteer-cell .vol-name {
  font-weight: 600;
}
.volunteer-cell .vol-id {
  font-size: 12px;
  color: var(--text-secondary);
}
.ops-btns {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}
.btn-approve {
  background: var(--success);
  color: #fff;
  border: none;
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  font-weight: 500;
  transition: background 0.15s;
}
.btn-approve:hover {
  background: #15803d;
}
.btn-reject {
  background: var(--danger);
  color: #fff;
  border: none;
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  font-weight: 500;
  transition: background 0.15s;
}
.btn-reject:hover {
  background: #b91c1c;
}
.score-display {
  display: flex;
  align-items: center;
  gap: 4px;
}
.score-stars {
  color: #f59e0b;
  font-size: 15px;
}
.score-num {
  font-size: 13px;
  color: var(--text-secondary);
}
.btn-score {
  background: none;
  border: 1px solid var(--primary);
  color: var(--primary);
  padding: 5px 12px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s;
}
.btn-score:hover {
  background: var(--primary);
  color: #fff;
}
.score-modal {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.score-modal-inner {
  background: #fff;
  border-radius: 14px;
  padding: 32px;
  width: 360px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}
.score-modal-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 8px;
}
.score-modal-sub {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 20px;
}
.star-picker {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  justify-content: center;
}
.star-picker .star {
  font-size: 36px;
  cursor: pointer;
  transition: transform 0.1s;
  color: #d1d5db;
}
.star-picker .star.active {
  color: #f59e0b;
}
.star-picker .star:hover {
  transform: scale(1.15);
}
.score-modal-actions {
  display: flex;
  gap: 12px;
}
.score-modal-actions .btn {
  flex: 1;
  padding: 11px;
}
.empty-state {
  text-align: center;
  padding: 60px 0;
  color: var(--text-secondary);
  font-size: 14px;
}
</style>
