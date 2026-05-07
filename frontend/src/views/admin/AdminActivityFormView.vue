<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { get, post, put } from '../../lib/request.js'
import { showToast } from '../../lib/ui.js'

const route = useRoute()
const router = useRouter()
const activityId = computed(() => {
  const id = route.params.id
  return id ? Number(id) : null
})
const isEdit = computed(() => activityId.value != null)

const pageTitle = ref('发布新活动')
const submitLabel = ref('🖫 发布活动')

const title = ref('')
const description = ref('')
const categoryTag = ref('')
const status = ref('1')
const startTime = ref('')
const endTime = ref('')
const location = ref('')
const organizer = ref('')
const totalQuota = ref('')
const coverUrl = ref('')

let posCounter = 0
const positions = ref([])

function addPosition(data = null) {
  const lid = ++posCounter
  positions.value.push({
    localId: lid,
    id: data?.id || null,
    name: data?.name || '',
    description: data?.description || '',
    quota: data?.quota || 10,
    requirements: data?.requirements?.length ? [...data.requirements] : [''],
  })
}

function removePosition(lid) {
  positions.value = positions.value.filter((p) => p.localId !== lid)
}

function addRequirement(lid) {
  const pos = positions.value.find((p) => p.localId === lid)
  if (pos) {
    pos.requirements.push('')
  }
}

function removeRequirement(lid, idx) {
  const pos = positions.value.find((p) => p.localId === lid)
  if (pos && pos.requirements.length > 1) {
    pos.requirements.splice(idx, 1)
  }
}

async function loadActivity() {
  try {
    const a = await get(`/api/activities/${activityId.value}`)
    title.value = a.title || ''
    description.value = a.description || ''
    location.value = a.location || ''
    organizer.value = a.organizer || ''
    totalQuota.value = a.totalQuota != null ? String(a.totalQuota) : ''
    coverUrl.value = a.coverUrl || a.coverImage || ''
    status.value = String(a.status ?? 1)
    if (a.startTime) startTime.value = a.startTime.slice(0, 10)
    if (a.endTime) endTime.value = a.endTime.slice(0, 10)

    const posData = await get(`/api/activities/${activityId.value}/positions`)
    positions.value = []
    posCounter = 0
    ;(posData || []).forEach((p) => {
      const reqs = p.requirements
        ? String(p.requirements).split(/[，,]/)
        : []
      addPosition({
        id: p.id,
        name: p.name,
        description: p.description,
        quota: p.quota,
        requirements: reqs.length ? reqs : [''],
      })
    })
  } catch {
    showToast('加载活动信息失败', 'error')
  }
}

onMounted(() => {
  if (isEdit.value) {
    pageTitle.value = '编辑活动'
    submitLabel.value = '🖫 保存修改'
    loadActivity()
  } else {
    addPosition()
  }
})

async function submitForm() {
  const t = title.value.trim()
  const desc = description.value.trim()
  const loc = location.value.trim()
  const org = organizer.value.trim()
  const tq = Number(totalQuota.value)
  const st = startTime.value
  const et = endTime.value
  const stNum = Number(status.value)
  const cover = coverUrl.value.trim()

  if (!t) {
    showToast('请填写活动名称', 'error')
    return
  }
  if (!desc) {
    showToast('请填写活动描述', 'error')
    return
  }
  if (!loc) {
    showToast('请填写活动地点', 'error')
    return
  }
  if (!org) {
    showToast('请填写主办单位', 'error')
    return
  }
  if (!tq || tq < 1) {
    showToast('请填写有效的总名额', 'error')
    return
  }
  if (!st) {
    showToast('请选择开始时间', 'error')
    return
  }
  if (!et) {
    showToast('请选择结束时间', 'error')
    return
  }

  for (const pos of positions.value) {
    if (!pos.name.trim()) {
      showToast('岗位名称不能为空', 'error')
      return
    }
    if (!pos.description.trim()) {
      showToast('岗位描述不能为空', 'error')
      return
    }
    if (!pos.quota || pos.quota < 1) {
      showToast('岗位招募人数不正确', 'error')
      return
    }
  }

  const activityData = {
    title: t,
    description: desc,
    location: loc,
    organizer: org,
    totalQuota: tq,
    status: stNum,
    startTime: st + 'T00:00:00',
    endTime: et + 'T23:59:59',
  }
  if (cover) activityData.coverUrl = cover

  submitting.value = true
  try {
    let savedId = activityId.value
    if (isEdit.value) {
      await put(`/api/activities/${activityId.value}`, activityData)
    } else {
      const created = await post('/api/activities', activityData)
      savedId = created?.id ?? created
    }

    if (savedId) {
      for (const pos of positions.value) {
        const posBody = {
          name: pos.name.trim(),
          description: pos.description.trim(),
          quota: pos.quota,
          requirements: pos.requirements.filter((r) => r.trim()).join('，'),
        }
        if (pos.id) {
          await put(`/api/positions/${pos.id}`, posBody)
        } else {
          await post(`/api/activities/${savedId}/positions`, posBody)
        }
      }
    }

    showToast(isEdit.value ? '活动已保存' : '活动发布成功', 'success')
    setTimeout(() => router.replace('/admin/activities'), 800)
  } catch {
    submitting.value = false
  }
}

const submitting = ref(false)
</script>

<template>
  <div class="form-wrap">
    <div class="page-title">{{ pageTitle }}</div>
    <div class="page-subtitle">填写活动信息和岗位设置</div>

    <div class="form-card">
      <div class="form-card-title">基础信息</div>

      <div class="form-group">
        <label><span class="req">* </span>活动名称</label>
        <input v-model="title" class="form-input" type="text" placeholder="请输入活动名称" />
      </div>
      <div class="form-group">
        <label><span class="req">* </span>活动描述</label>
        <textarea v-model="description" class="form-input" placeholder="请输入活动详细描述" />
      </div>
      <div class="form-row-grid">
        <div class="form-group">
          <label><span class="req">* </span>活动类别</label>
          <select v-model="categoryTag" class="form-input">
            <option value="">请选择活动类别</option>
            <option value="环境保护">环境保护</option>
            <option value="敬老助残">敬老助残</option>
            <option value="教育辅导">教育辅导</option>
            <option value="医疗健康">医疗健康</option>
            <option value="文化宣传">文化宣传</option>
            <option value="应急救援">应急救援</option>
            <option value="其他">其他</option>
          </select>
        </div>
        <div class="form-group">
          <label><span class="req">* </span>活动状态</label>
          <select v-model="status" class="form-input">
            <option value="0">草稿</option>
            <option value="1">报名中</option>
            <option value="2">进行中</option>
            <option value="3">已结束</option>
          </select>
        </div>
      </div>
      <div class="form-row-grid">
        <div class="form-group">
          <label><span class="req">* </span>开始时间</label>
          <input v-model="startTime" class="form-input" type="date" />
        </div>
        <div class="form-group">
          <label><span class="req">* </span>结束时间</label>
          <input v-model="endTime" class="form-input" type="date" />
        </div>
      </div>
      <div class="form-row-grid">
        <div class="form-group">
          <label><span class="req">* </span>活动地点</label>
          <input v-model="location" class="form-input" type="text" placeholder="请输入活动地点" />
        </div>
        <div class="form-group">
          <label><span class="req">* </span>主办单位</label>
          <input v-model="organizer" class="form-input" type="text" placeholder="请输入主办单位" />
        </div>
      </div>
      <div class="form-row-grid">
        <div class="form-group">
          <label><span class="req">* </span>总名额</label>
          <input
            v-model="totalQuota"
            class="form-input"
            type="number"
            min="1"
            placeholder="请输入总招募人数"
          />
        </div>
        <div class="form-group">
          <label>封面图 URL（可选）</label>
          <input v-model="coverUrl" class="form-input" type="text" placeholder="https://..." />
        </div>
      </div>
    </div>

    <div class="form-card">
      <div class="positions-header">
        <div class="section-title">岗位设置</div>
        <button type="button" class="btn btn-outline" style="padding: 6px 16px; font-size: 13px" @click="addPosition()">
          ＋ 添加岗位
        </button>
      </div>

      <p
        v-if="positions.length === 0"
        style="color: var(--text-secondary); font-size: 14px; text-align: center; padding: 20px 0"
      >
        暂无岗位，点击「添加岗位」新增
      </p>
      <div v-for="(pos, i) in positions" v-else-if="positions.length > 0" :key="pos.localId" class="position-card">
        <div class="position-card-header">
          <div class="position-card-title">岗位 {{ i + 1 }}</div>
          <button
            v-if="positions.length > 1"
            type="button"
            class="btn-remove-position"
            title="删除岗位"
            @click="removePosition(pos.localId)"
          >
            🗑
          </button>
        </div>
        <div class="form-group">
          <label><span class="req">* </span>岗位名称</label>
          <input v-model="pos.name" class="form-input" type="text" placeholder="例如：清洁组志愿者" />
        </div>
        <div class="form-group">
          <label><span class="req">* </span>岗位描述</label>
          <textarea
            v-model="pos.description"
            class="form-input"
            placeholder="请输入岗位职责和工作内容"
          />
        </div>
        <div class="form-group">
          <label>岗位要求</label>
          <div class="requirements-list">
            <div v-for="(req, idx) in pos.requirements" :key="idx" class="requirement-row">
              <input v-model="pos.requirements[idx]" type="text" placeholder="例如：身体健康" />
              <button
                v-if="pos.requirements.length > 1"
                type="button"
                class="btn-remove-req"
                @click="removeRequirement(pos.localId, idx)"
              >
                ✕
              </button>
            </div>
          </div>
          <button type="button" class="btn-add-req" @click="addRequirement(pos.localId)">＋ 添加要求</button>
        </div>
        <div class="form-group">
          <label><span class="req">* </span>招募人数</label>
          <input v-model.number="pos.quota" class="form-input" type="number" min="1" />
        </div>
      </div>
    </div>

    <div class="form-actions">
      <button type="button" class="btn btn-primary" :disabled="submitting" @click="submitForm">
        <span>{{ submitLabel }}</span>
      </button>
      <button type="button" class="btn btn-outline btn-cancel" @click="router.push('/admin/activities')">
        取消
      </button>
    </div>
  </div>
</template>

<style scoped>
.form-wrap {
  max-width: 1100px;
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
  margin-bottom: 28px;
}
.form-card {
  background: #fff;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  padding: 28px;
  margin-bottom: 24px;
}
.form-card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--primary);
  display: inline-block;
}
.form-group {
  margin-bottom: 16px;
}
.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: 6px;
}
.req {
  color: var(--danger);
}
.form-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid var(--border);
  border-radius: 8px;
  font-size: 14px;
  color: var(--text-primary);
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.15s;
}
textarea.form-input {
  min-height: 90px;
  resize: vertical;
}
select.form-input {
  cursor: pointer;
}
.form-input:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.08);
}
.form-row-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
.positions-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}
.position-card {
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  padding: 20px;
  margin-bottom: 16px;
  background: #fafbfc;
  position: relative;
}
.position-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
.position-card-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
}
.btn-remove-position {
  background: none;
  border: none;
  cursor: pointer;
  color: var(--danger);
  font-size: 18px;
  padding: 2px 6px;
  border-radius: 6px;
  transition: background 0.15s;
}
.btn-remove-position:hover {
  background: var(--danger-bg);
}
.requirements-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.requirement-row {
  display: flex;
  gap: 8px;
  align-items: center;
}
.requirement-row input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid var(--border);
  border-radius: 6px;
  font-size: 13px;
  outline: none;
  transition: border-color 0.15s;
}
.requirement-row input:focus {
  border-color: var(--primary);
}
.btn-remove-req {
  background: none;
  border: none;
  cursor: pointer;
  color: var(--danger);
  font-size: 16px;
  padding: 4px 6px;
  border-radius: 4px;
}
.btn-add-req {
  background: none;
  border: none;
  cursor: pointer;
  color: var(--primary);
  font-size: 13px;
  padding: 4px 0;
  font-weight: 500;
  display: inline-block;
  margin-top: 4px;
}
.btn-add-req:hover {
  text-decoration: underline;
}
.form-actions {
  display: flex;
  gap: 16px;
  margin-top: 8px;
}
.form-actions .btn {
  flex: 1;
  padding: 13px;
  font-size: 15px;
}
.form-actions .btn-cancel {
  flex: none;
  width: 120px;
}
@media (max-width: 768px) {
  .form-row-grid {
    grid-template-columns: 1fr;
  }
}
</style>
