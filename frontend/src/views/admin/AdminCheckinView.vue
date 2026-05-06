<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { get } from '../../lib/request.js'
import { showToast } from '../../lib/ui.js'
import { formatDate, formatTime } from '../../utils/display.js'

const activities = ref([])
const activitySelect = ref('')
const currentActivity = ref(null)
const section = ref('empty')
const qrImage = ref('')
const qrPlaceholder = ref(true)
const qrPlaceholderText = ref('正在加载...')
const qrTokenDisplay = ref('')
const countdownNum = ref(60)
const checkinList = ref([])
const tableEmpty = ref(false)

const checkinCount = ref(0)
const checkoutCount = ref(0)
const absentCount = ref(0)

let countdownTimer = null

const STATUS_MAP = { 0: '草稿', 1: '报名中', 2: '进行中', 3: '已结束', 4: '已取消' }

onMounted(async () => {
  try {
    const res = await get('/api/activities', { pageNum: 1, pageSize: 200 })
    activities.value = res?.list || []
  } catch {
    showToast('加载活动列表失败', 'error')
  }
})

onBeforeUnmount(() => {
  clearTimers()
})

function clearTimers() {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

function onActivityChange() {
  clearTimers()
  const id = activitySelect.value
  if (!id) {
    section.value = 'empty'
    return
  }
  currentActivity.value = activities.value.find((a) => String(a.id) === id)
  if (!currentActivity.value) {
    section.value = 'empty'
    return
  }
  if (currentActivity.value.status === 2) {
    section.value = 'ongoing'
    loadQRCode()
    loadCheckinList()
    startCountdown()
  } else {
    section.value = 'notOngoing'
    loadCheckinList()
  }
}

function loadQRCode() {
  if (!currentActivity.value) return
  qrPlaceholder.value = true
  qrPlaceholderText.value = '正在加载...'
  get(`/api/checkin/qrcode/${currentActivity.value.id}`)
    .then((data) => {
      if (data && data.qrCodeBase64) {
        qrImage.value = data.qrCodeBase64
        qrPlaceholder.value = false
        if (currentActivity.value) {
          qrTokenDisplay.value = `CHECKIN_${currentActivity.value.id}_${Date.now()}`
        }
      }
    })
    .catch(() => {
      qrPlaceholderText.value = '二维码加载失败'
      qrPlaceholder.value = true
      qrImage.value = ''
    })
}

function startCountdown() {
  countdownNum.value = 60
  countdownTimer = setInterval(() => {
    countdownNum.value--
    if (countdownNum.value <= 0) {
      countdownNum.value = 60
      loadQRCode()
      loadCheckinList()
    }
  }, 1000)
}

async function loadCheckinList() {
  if (!currentActivity.value) return
  try {
    const list = (await get(`/api/checkin/activity/${currentActivity.value.id}`)) || []
    checkinList.value = list
    tableEmpty.value = !list.length
    updateStats(list)
  } catch {
    checkinList.value = []
    tableEmpty.value = true
  }
}

function updateStats(list) {
  let checkinN = 0
  let checkoutN = 0
  let absentN = 0
  list.forEach((r) => {
    if (r.checkoutTime) checkoutN++
    else if (r.checkinTime) checkinN++
    else absentN++
  })
  checkinCount.value = checkinN
  checkoutCount.value = checkoutN
  absentCount.value = absentN
}

function rowStatus(r) {
  if (r.checkoutTime) return 'checkout'
  if (r.checkinTime) return 'checkin'
  return 'absent'
}
</script>

<template>
  <div>
    <h1 class="page-title">签到管理</h1>
    <p class="page-subtitle">管理活动签到签退，实时查看参与情况</p>

    <div class="filter-bar">
      <div class="filter-group">
        <label>选择活动 *</label>
        <select v-model="activitySelect" class="filter-select" @change="onActivityChange">
          <option value="">请选择活动</option>
          <option v-for="a in activities" :key="a.id" :value="String(a.id)">
            {{ a.title }} - {{ STATUS_MAP[a.status] || '' }}
          </option>
        </select>
      </div>
    </div>

    <div v-show="section === 'empty'" class="empty-state">
      <div class="hint">请先选择活动</div>
      <div class="sub-hint">选择活动后可查看签到情况</div>
    </div>

    <div v-show="section === 'notOngoing'" class="empty-state">
      <div class="hint">该活动尚未开始</div>
      <div class="sub-hint">只有<span>进行中</span>的活动才能生成签到二维码</div>
    </div>

    <div v-show="section === 'ongoing'" class="qr-stats-grid">
      <div class="qr-card">
        <h2>签到二维码</h2>
        <div class="qr-img-wrap">
          <img v-show="!qrPlaceholder && qrImage" :src="qrImage" alt="签到二维码" />
          <div v-show="qrPlaceholder" class="qr-placeholder">{{ qrPlaceholderText }}</div>
        </div>
        <div class="qr-timer">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="18"
            height="18"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <circle cx="12" cy="12" r="10" />
            <polyline points="12 6 12 12 16 14" />
          </svg>
          二维码将在 <strong>{{ countdownNum }}</strong> 秒后刷新
        </div>
        <div class="qr-hint">志愿者使用手机扫码进行签到/签退</div>
        <div v-if="qrTokenDisplay" class="qr-token-box">{{ qrTokenDisplay }}</div>
      </div>

      <div class="stats-card">
        <h2>实时统计</h2>
        <div class="stat-circles">
          <div class="stat-circle green">
            <div class="icon">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="28"
                height="28"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                <polyline points="22 4 12 14.01 9 11.01" />
              </svg>
            </div>
            <div class="num">{{ checkinCount }}</div>
            <div class="label">已签到</div>
          </div>
          <div class="stat-circle blue">
            <div class="icon">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="28"
                height="28"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                <polyline points="16 17 21 12 16 7" />
                <line x1="21" y1="12" x2="9" y2="12" />
              </svg>
            </div>
            <div class="num">{{ checkoutCount }}</div>
            <div class="label">已签退</div>
          </div>
          <div class="stat-circle red">
            <div class="icon">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="28"
                height="28"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <circle cx="12" cy="12" r="10" />
                <line x1="15" y1="9" x2="9" y2="15" />
                <line x1="9" y1="9" x2="15" y2="15" />
              </svg>
            </div>
            <div class="num">{{ absentCount }}</div>
            <div class="label">缺席</div>
          </div>
        </div>
        <div class="activity-info-box">
          <div class="info-title">活动信息</div>
          <div class="info-line">活动名称：<span>{{ currentActivity?.title || '-' }}</span></div>
          <div class="info-line">活动地点：<span>{{ currentActivity?.location || '-' }}</span></div>
          <div class="info-line">
            活动时间：<span>{{
              currentActivity?.startTime && currentActivity?.endTime
                ? formatDate(currentActivity.startTime) + ' - ' + formatDate(currentActivity.endTime)
                : '-'
            }}</span>
          </div>
        </div>
      </div>
    </div>

    <div v-show="section === 'ongoing' || section === 'notOngoing'" class="detail-card">
      <div class="card-header"><h2>签到明细</h2></div>
      <div class="overflow-x-auto">
        <table class="checkin-table">
          <thead>
            <tr>
              <th>志愿者</th>
              <th>签到时间</th>
              <th>签退时间</th>
              <th>服务时长</th>
              <th>状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in checkinList" :key="r.registrationId || r.userId + '-' + r.checkinTime">
              <td style="font-weight: 600">{{ r.realName || r.username || '-' }}</td>
              <td>{{ r.checkinTime ? formatTime(r.checkinTime) : '-' }}</td>
              <td>{{ r.checkoutTime ? formatTime(r.checkoutTime) : '-' }}</td>
              <td>{{ r.durationHours != null ? r.durationHours + ' 小时' : '-' }}</td>
              <td>
                <span v-if="rowStatus(r) === 'checkout'" class="status-badge status-checkout">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                    <polyline points="22 4 12 14.01 9 11.01" />
                  </svg>
                  已签退
                </span>
                <span v-else-if="rowStatus(r) === 'checkin'" class="status-badge status-checkin">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4" />
                    <polyline points="10 17 15 12 10 7" />
                    <line x1="15" y1="12" x2="3" y2="12" />
                  </svg>
                  已签到
                </span>
                <span v-else class="status-badge status-absent">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <circle cx="12" cy="12" r="10" />
                    <line x1="15" y1="9" x2="9" y2="15" />
                    <line x1="9" y1="9" x2="15" y2="15" />
                  </svg>
                  缺席
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-show="tableEmpty" class="table-empty">暂无签到记录</div>
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
.filter-bar .filter-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 200px;
  max-width: 420px;
  flex: 1;
}
.filter-bar .filter-group label {
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
.empty-state {
  background: #fff;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  padding: 64px 24px;
  text-align: center;
}
.empty-state .hint {
  color: var(--text-muted);
  font-size: 15px;
  margin-bottom: 6px;
}
.empty-state .sub-hint {
  color: var(--text-muted);
  font-size: 13px;
}
.empty-state .sub-hint span {
  color: var(--primary);
}
.qr-stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
}
@media (max-width: 900px) {
  .qr-stats-grid {
    grid-template-columns: 1fr;
  }
}
.qr-card {
  background: #fff;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  padding: 28px;
  text-align: center;
}
.qr-card h2 {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 16px;
}
.qr-img-wrap {
  display: inline-block;
  padding: 24px;
  background: linear-gradient(135deg, #eff6ff, #f5f3ff);
  border-radius: 12px;
  margin-bottom: 16px;
}
.qr-img-wrap img {
  width: 240px;
  height: 240px;
  border-radius: 8px;
  border: 4px solid #e5e7eb;
  display: block;
  background: #fff;
}
.qr-placeholder {
  width: 240px;
  height: 240px;
  border-radius: 8px;
  border: 4px solid #e5e7eb;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  font-size: 14px;
}
.qr-timer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 4px;
}
.qr-timer svg {
  color: var(--primary);
}
.qr-hint {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 12px;
}
.qr-token-box {
  margin-top: 12px;
  padding: 10px 16px;
  background: #eff6ff;
  border-radius: 8px;
  font-size: 12px;
  color: var(--text-secondary);
  word-break: break-all;
}
.stats-card {
  background: #fff;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  padding: 28px;
}
.stats-card h2 {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 16px;
}
.stat-circles {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 12px;
  margin-bottom: 24px;
}
.stat-circle {
  text-align: center;
  padding: 16px 8px;
  border-radius: 12px;
}
.stat-circle.green {
  background: #f0fdf4;
}
.stat-circle.blue {
  background: #eff6ff;
}
.stat-circle.red {
  background: #fef2f2;
}
.stat-circle .icon {
  margin-bottom: 6px;
}
.stat-circle .icon svg {
  width: 28px;
  height: 28px;
}
.stat-circle.green .icon svg {
  color: #16a34a;
}
.stat-circle.blue .icon svg {
  color: #2563eb;
}
.stat-circle.red .icon svg {
  color: #dc2626;
}
.stat-circle .num {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}
.stat-circle .label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 2px;
}
.activity-info-box {
  padding: 16px;
  background: #f8fafc;
  border-radius: 10px;
}
.activity-info-box .info-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 8px;
}
.activity-info-box .info-line {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.7;
}
.detail-card {
  background: #fff;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}
.detail-card .card-header {
  padding: 20px 24px;
  border-bottom: 1px solid var(--border);
}
.detail-card .card-header h2 {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}
.overflow-x-auto {
  overflow-x: auto;
}
.checkin-table {
  width: 100%;
  border-collapse: collapse;
}
.checkin-table th {
  background: #f8fafc;
  padding: 13px 18px;
  text-align: left;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--border);
}
.checkin-table td {
  padding: 13px 18px;
  font-size: 14px;
  color: var(--text-primary);
  border-bottom: 1px solid #f1f5f9;
  vertical-align: middle;
}
.checkin-table tr:last-child td {
  border-bottom: none;
}
.checkin-table tr:hover td {
  background: #f8fafc;
}
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 500;
}
.status-checkin {
  color: #2563eb;
}
.status-checkout {
  color: #16a34a;
}
.status-absent {
  color: #dc2626;
}
.table-empty {
  text-align: center;
  padding: 48px 24px;
  color: var(--text-muted);
  font-size: 14px;
}
</style>
