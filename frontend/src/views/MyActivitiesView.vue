<script setup>
import { ref, computed, onMounted } from 'vue'
import { get, post } from '../lib/request.js'
import { showToast } from '../lib/ui.js'
import { formatDate } from '../utils/display.js'

/* ===== 数据 ===== */
const loading = ref(true)
const items = ref([])   // 合并后的"进行中 + 我有报名"条目

/* ===== 签到弹窗 ===== */
const scanModal = ref(false)
const scanActivityId = ref(null)
const scanActivityTitle = ref('')
const submitting = ref(false)
// QR 码
const qrImage = ref('')
const qrToken = ref('')
const qrLoading = ref(false)

/* ===== 本地签到状态缓存（activityId → 'unchecked'|'checked'|'out'） ===== */
const checkinState = ref({})

/* ===== 加载数据 ===== */
onMounted(async () => {
  loading.value = true
  try {
    const [actRes, regRes] = await Promise.all([
      get('/api/activities', { pageNum: 1, pageSize: 100, status: 2 }),
      get('/api/registrations/my', { pageNum: 1, pageSize: 200 }),
    ])
    const ongoingActivities = actRes?.list || []
    const myRegs = (regRes?.list || []).filter(r => r.status === 1 || r.status === 3)

    // 以 activityId 建立报名索引
    const regMap = {}
    for (const r of myRegs) {
      regMap[r.activityId] = r
    }

    // 只保留我有报名的进行中活动
    items.value = ongoingActivities
      .filter(a => regMap[a.id])
      .map(a => ({ activity: a, reg: regMap[a.id] }))
  } catch {
    showToast('加载失败', 'error')
  } finally {
    loading.value = false
  }
})

/* ===== 签到状态工具 ===== */
function getState(activityId) {
  return checkinState.value[activityId] || 'unchecked'
}
function setState(activityId, state) {
  checkinState.value = { ...checkinState.value, [activityId]: state }
}

/* ===== 打开签到弹窗 ===== */
async function openScanModal(activity) {
  scanActivityId.value = activity.id
  scanActivityTitle.value = activity.title
  qrImage.value = ''
  qrToken.value = ''
  scanModal.value = true
  // 自动加载二维码
  qrLoading.value = true
  try {
    const data = await get(`/api/checkin/qrcode/${activity.id}`)
    qrImage.value = data?.qrCodeBase64 || ''
    qrToken.value = data?.token || ''
  } catch {
    showToast('获取二维码失败', 'error')
  } finally {
    qrLoading.value = false
  }
}
function closeScanModal() {
  scanModal.value = false
  submitting.value = false
}

/* ===== 提交签到（一键） ===== */
async function submitScan() {
  const activityId = scanActivityId.value
  const token = qrToken.value
  if (!token) {
    showToast('二维码尚未加载，请稍候', 'error')
    return
  }
  submitting.value = true
  try {
    await post('/api/checkin/scan', { activityId, token })
    showToast('签到成功！', 'success')
    setState(activityId, 'checked')
    closeScanModal()
  } catch (e) {
    if (e?.code === 5002) {
      setState(activityId, 'checked')
      closeScanModal()
    }
    submitting.value = false
  }
}

/* ===== 签退 ===== */
async function checkout(activityId, title) {
  try {
    await post('/api/checkin/checkout', { activityId })
    showToast(`已完成「${title}」签退`, 'success')
    setState(activityId, 'out')
  } catch (e) {
    if (e?.code === 5003) {
      setState(activityId, 'out')
    }
  }
}

/* ===== 格式化志愿者报名状态 ===== */
const REG_STATUS = { 1: { text: '已通过', cls: 'badge-approved' }, 3: { text: '已派岗', cls: 'badge-signing' } }
function regStatusInfo(s) {
  return REG_STATUS[s] || { text: '未知', cls: '' }
}

const hasItems = computed(() => !loading.value && items.value.length > 0)
</script>

<template>
  <div class="my-activities">
    <div class="page-header">
      <div class="page-title">我的活动</div>
      <div class="page-subtitle">当前进行中、你已报名的活动</div>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="empty-hint">加载中...</div>

    <!-- 空状态 -->
    <div v-else-if="!hasItems" class="empty-hint">
      <div class="empty-icon">📋</div>
      <div>暂无进行中的活动</div>
      <div class="empty-sub">报名并通过审核的进行中活动将显示在这里</div>
    </div>

    <!-- 活动卡片列表 -->
    <div v-else class="card-list">
      <div v-for="{ activity: a, reg } in items" :key="a.id" class="activity-card">
        <!-- 卡片头部 -->
        <div class="card-head">
          <div>
            <div class="card-title">{{ a.title }}</div>
            <div class="card-meta">
              <span class="meta-item">📍 {{ a.location || '—' }}</span>
              <span class="meta-item">📅 {{ formatDate(a.startTime) }} – {{ formatDate(a.endTime) }}</span>
            </div>
          </div>
          <span class="badge badge-ongoing">进行中</span>
        </div>

        <!-- 岗位 & 报名状态 -->
        <div class="card-info">
          <span class="info-label">岗位：</span>
          <span>{{ reg.positionName || '未分配' }}</span>
          <span class="info-sep">|</span>
          <span class="info-label">报名状态：</span>
          <span class="badge" :class="regStatusInfo(reg.status).cls">{{ regStatusInfo(reg.status).text }}</span>
        </div>

        <!-- 签到状态 & 操作区 -->
        <div class="card-actions">
          <!-- 待签到 -->
          <template v-if="getState(a.id) === 'unchecked'">
            <span class="checkin-status status-pending">● 未签到</span>
            <button class="btn btn-primary btn-sm" @click="openScanModal(a)">签到</button>
          </template>

          <!-- 已签到未签退 -->
          <template v-else-if="getState(a.id) === 'checked'">
            <span class="checkin-status status-checked">✔ 已签到</span>
            <button class="btn btn-outline btn-sm" @click="checkout(a.id, a.title)">↪ 签退</button>
          </template>

          <!-- 已签退 -->
          <template v-else>
            <span class="checkin-status status-out">✔ 已签退</span>
            <span class="done-hint">本次服务已完成</span>
          </template>
        </div>
      </div>
    </div>

    <!-- ===== 签到弹窗 ===== -->
    <Teleport to="body">
      <div v-if="scanModal" class="modal-mask" @click.self="closeScanModal">
        <div class="modal-box">
          <div class="modal-title">签到</div>
          <div class="modal-sub">{{ scanActivityTitle }}</div>

          <!-- 二维码展示区 -->
          <div class="qr-area">
            <div v-if="qrLoading" class="qr-placeholder">加载中...</div>
            <img v-else-if="qrImage" :src="qrImage" class="qr-img" alt="签到二维码" />
            <div v-else class="qr-placeholder qr-error">二维码加载失败</div>
          </div>

          <div class="modal-actions">
            <button class="btn btn-primary" :disabled="submitting || qrLoading || !qrToken" @click="submitScan">
              {{ submitting ? '签到中...' : '✔ 一键签到' }}
            </button>
            <button class="btn btn-outline" @click="closeScanModal">取消</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.my-activities {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 0 48px;
}

.page-header {
  margin-bottom: 24px;
}
.page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}
.page-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
}

/* 空状态 */
.empty-hint {
  text-align: center;
  padding: 80px 0;
  color: var(--text-secondary);
  font-size: 15px;
}
.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}
.empty-sub {
  font-size: 13px;
  margin-top: 8px;
  color: var(--text-muted);
}

/* 卡片列表 */
.card-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-card {
  background: #fff;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  padding: 20px 24px;
  border: 1px solid var(--border);
  transition: box-shadow 0.15s;
}
.activity-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}
.card-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 6px;
}
.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.meta-item {
  font-size: 13px;
  color: var(--text-secondary);
}

.card-info {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
}
.info-label {
  color: var(--text-muted);
}
.info-sep {
  color: var(--border);
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-top: 14px;
  border-top: 1px solid #f1f5f9;
}

/* 签到状态标签 */
.checkin-status {
  font-size: 13px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 20px;
}
.status-pending {
  background: #fef9c3;
  color: #854d0e;
}
.status-checked {
  background: #dcfce7;
  color: var(--success);
}
.status-out {
  background: #eff6ff;
  color: var(--primary);
}
.done-hint {
  font-size: 12px;
  color: var(--text-muted);
}

.btn-sm {
  padding: 7px 16px;
  font-size: 13px;
}

/* 弹窗 */
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-box {
  background: #fff;
  border-radius: 16px;
  padding: 28px 32px;
  width: 360px;
  max-width: 90vw;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.18);
  text-align: center;
}
.modal-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 4px;
}
.modal-sub {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 20px;
}
/* 二维码区域 */
.qr-area {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 20px;
  min-height: 200px;
}
.qr-img {
  width: 200px;
  height: 200px;
  border-radius: 8px;
  border: 1px solid var(--border);
}
.qr-placeholder {
  width: 200px;
  height: 200px;
  border-radius: 8px;
  border: 1px dashed var(--border);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  color: var(--text-muted);
}
.qr-error {
  color: var(--danger, #ef4444);
  border-color: var(--danger, #ef4444);
}
.modal-actions {
  display: flex;
  gap: 12px;
}
.modal-actions .btn {
  flex: 1;
  padding: 11px;
}
</style>
