<script setup>
import { ref, onMounted, computed } from 'vue'
import { RouterLink } from 'vue-router'
import { get, put } from '../lib/request.js'
import { getUsername } from '../lib/auth.js'
import { registrationStatusInfo, formatTime } from '../utils/display.js'

const profile = ref(null)
const achievements = ref([])
const records = ref([])
const failed = ref(false)

// ---- 技能标签编辑状态 ----
const editingTags = ref(false)
const editTagList = ref([])   // 编辑中的标签数组
const newTagInput = ref('')
const savingTags = ref(false)
const PRESET_TAGS = ['教育辅导', '环境保护', '医疗救护', '敬老助残', '文化宣传', '应急救援', '摄影', '急救知识', '组织协调']

function openTagEdit() {
  editTagList.value = [...skillTags.value]
  newTagInput.value = ''
  editingTags.value = true
}

function cancelTagEdit() {
  editingTags.value = false
}

function removeEditTag(t) {
  editTagList.value = editTagList.value.filter(x => x !== t)
}

function addPresetTag(t) {
  if (!editTagList.value.includes(t)) editTagList.value.push(t)
}

function addCustomTag() {
  const v = newTagInput.value.trim()
  if (v && !editTagList.value.includes(v)) {
    editTagList.value.push(v)
  }
  newTagInput.value = ''
}

async function saveTags() {
  savingTags.value = true
  try {
    await put('/api/profile/tags', editTagList.value)
    profile.value.skillTags = editTagList.value.join(',')
    editingTags.value = false
  } catch { /* toast in request */ } finally {
    savingTags.value = false
  }
}

const nameInitial = computed(() => (profile.value?.realName || 'U').charAt(0))
const skillTags = computed(() => {
  if (!profile.value?.skillTags) return []
  return profile.value.skillTags.split(',').filter(Boolean)
})

async function loadProfile() {
  failed.value = false
  try {
    const [profileData, achData, regData] = await Promise.all([
      get('/api/profile/radar'),
      get('/api/achievements'),
      get('/api/registrations/my', { pageNum: 1, pageSize: 50 }),
    ])
    profile.value = profileData
    achievements.value = achData || []
    records.value = regData.list || []
  } catch {
    failed.value = true
  }
}

onMounted(loadProfile)
</script>

<template>
  <div class="profile-container">
    <RouterLink to="/activities" class="back-link">‹ 返回</RouterLink>
    <div class="profile-grid">
      <div v-if="!profile && !failed" class="spinner" />
      <template v-else-if="failed">
        <p style="color: var(--text-secondary)">加载失败，请稍后重试</p>
      </template>
      <template v-else>
        <div>
          <div class="user-card">
            <div class="avatar-circle">{{ nameInitial }}</div>
            <div class="name">{{ profile.realName || '' }}</div>
            <div class="username">@{{ getUsername() }}</div>
            <div class="info-row"><span class="icon">✉</span> {{ profile.email || '未设置' }}</div>
            <div class="info-row"><span class="icon">📞</span> {{ profile.phone || '未设置' }}</div>
            <div class="info-row"><span class="icon">🕐</span> 累计服务 {{ profile.totalHours || 0 }} 小时</div>
            <!-- 擅长领域（可编辑） -->
            <div class="skill-section">
              <div class="skill-section-header">
                <span class="label">擅长领域</span>
                <button v-if="!editingTags" class="btn-tag-edit" @click="openTagEdit">✏️ 编辑</button>
              </div>
              <!-- 只读模式 -->
              <template v-if="!editingTags">
                <div v-if="skillTags.length" class="skill-tags">
                  <span v-for="t in skillTags" :key="t" class="skill-tag">{{ t }}</span>
                </div>
                <p v-else style="font-size:13px;color:var(--text-secondary);margin:4px 0 0">暂无，点击编辑添加</p>
              </template>
              <!-- 编辑模式 -->
              <template v-else>
                <div class="skill-tags edit-mode">
                  <span v-for="t in editTagList" :key="t" class="skill-tag editable">
                    {{ t }}<button class="rm-tag" @click="removeEditTag(t)">✕</button>
                  </span>
                </div>
                <div class="preset-chips">
                  <span
                    v-for="pt in PRESET_TAGS"
                    :key="pt"
                    class="preset-chip"
                    :class="{ active: editTagList.includes(pt) }"
                    @click="addPresetTag(pt)"
                  >{{ pt }}</span>
                </div>
                <div class="tag-input-row">
                  <input v-model="newTagInput" class="tag-input" placeholder="自定义标签…" @keyup.enter="addCustomTag" />
                  <button class="btn-add-tag" @click="addCustomTag">添加</button>
                </div>
                <div class="tag-actions">
                  <button class="btn btn-primary" style="padding:6px 18px;font-size:13px" :disabled="savingTags" @click="saveTags">保存</button>
                  <button class="btn btn-outline" style="padding:6px 14px;font-size:13px" @click="cancelTagEdit">取消</button>
                </div>
              </template>
            </div>
          </div>

          <div class="badge-wall-card">
            <h3>🏅 勋章墙</h3>
            <div class="badge-wall">
              <p v-if="achievements.length === 0" style="color: var(--text-secondary); font-size: 14px">
                暂无勋章数据
              </p>
              <div
                v-for="a in achievements"
                v-else-if="achievements.length > 0"
                :key="a.name"
                class="badge-item"
                :title="a.description || ''"
              >
                <div class="badge-icon" :class="a.unlocked ? 'unlocked' : 'locked'">★</div>
                <span class="badge-name">{{ a.name }}</span>
              </div>
            </div>
          </div>
        </div>

        <div>
          <div class="radar-card">
            <h3>能力雷达图</h3>
            <img v-if="profile.radarImageBase64" :src="profile.radarImageBase64" alt="能力雷达图" />
            <div v-else class="radar-placeholder">
              雷达图服务暂不可用<br />
              <small>维度：累计时长(h) · 活动次数 · 类型多样性 · 平均评分 · 准时率(%)</small>
            </div>
          </div>

          <div class="records-card">
            <h3>我的报名记录</h3>
            <p v-if="records.length === 0" style="color: var(--text-secondary); font-size: 14px">
              暂无报名记录
            </p>
            <div v-for="r in records" v-else-if="records.length > 0" :key="r.id" class="record-item">
              <div class="rec-left">
                <div class="rec-title">{{ r.positionName || r.activityTitle || '未知岗位' }}</div>
                <div class="rec-sub">{{ r.activityTitle || '' }}</div>
                <div class="rec-date">📅 {{ formatTime(r.createdAt) }}</div>
                <div v-if="r.durationHours != null" class="rec-duration">
                  🕐 服务时长：{{ r.durationHours }} 小时
                </div>
                <div v-if="r.score != null" class="score-row">
                  ⭐ 评分：{{ r.score }} 分{{ r.remark ? '　' + r.remark : '' }}
                </div>
              </div>
              <span
                class="badge"
                :class="r.activityStatus === 3 && r.status === 3 ? 'badge-ended' : registrationStatusInfo(r.status).cls"
              >{{ r.activityStatus === 3 && r.status === 3 ? '已结束' : registrationStatusInfo(r.status).text }}</span>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.profile-container {
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
  margin-bottom: 20px;
  font-size: 14px;
}
.back-link:hover {
  color: var(--primary);
}
.profile-grid {
  display: grid;
  grid-template-columns: 380px 1fr;
  gap: 24px;
}
.user-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px 24px;
  box-shadow: var(--shadow-sm);
  text-align: center;
}
.avatar-circle {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: linear-gradient(135deg, #818cf8, #a78bfa);
  margin: 0 auto 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  color: #fff;
  font-weight: 600;
}
.user-card .name {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 4px;
}
.user-card .username {
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 16px;
}
.user-card .info-row {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: var(--text-primary);
  margin: 8px 0;
  justify-content: flex-start;
  padding-left: 24px;
}
.user-card .info-row .icon {
  font-size: 16px;
  color: var(--primary);
  width: 20px;
  text-align: center;
}
.skill-section {
  margin-top: 20px;
  text-align: left;
  padding-left: 24px;
  padding-right: 24px;
}
.skill-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.skill-section .label {
  font-size: 13px;
  color: var(--text-secondary);
}
.btn-tag-edit {
  background: none;
  border: none;
  cursor: pointer;
  color: var(--primary);
  font-size: 12px;
  padding: 2px 6px;
}
.skill-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.skill-tag {
  background: #eff6ff;
  color: var(--primary);
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.skill-tag.editable {
  padding-right: 6px;
}
.rm-tag {
  background: none;
  border: none;
  cursor: pointer;
  color: #93c5fd;
  font-size: 11px;
  padding: 0;
  line-height: 1;
}
.rm-tag:hover { color: var(--danger); }
.preset-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}
.preset-chip {
  padding: 3px 10px;
  border-radius: 10px;
  font-size: 12px;
  cursor: pointer;
  border: 1px solid var(--border);
  color: var(--text-secondary);
  background: #f9fafb;
  transition: all 0.15s;
}
.preset-chip.active,
.preset-chip:hover {
  border-color: var(--primary);
  color: var(--primary);
  background: #eff6ff;
}
.tag-input-row {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}
.tag-input {
  flex: 1;
  padding: 6px 10px;
  border: 1px solid var(--border);
  border-radius: 6px;
  font-size: 13px;
  outline: none;
}
.tag-input:focus { border-color: var(--primary); }
.btn-add-tag {
  padding: 6px 14px;
  background: var(--primary);
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
}
.tag-actions {
  display: flex;
  gap: 10px;
  margin-top: 12px;
}
.radar-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: var(--shadow-sm);
}
.radar-card h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
}
.radar-card img {
  width: 100%;
  max-width: 400px;
  display: block;
  margin: 0 auto;
}
.radar-placeholder {
  text-align: center;
  color: var(--text-secondary);
  padding: 60px 0;
}
.badge-wall-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: var(--shadow-sm);
  margin-top: 24px;
}
.badge-wall-card h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
}
.badge-wall {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
}
.badge-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  width: 80px;
}
.badge-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}
.badge-icon.unlocked {
  background: linear-gradient(135deg, #f59e0b, #f97316);
  color: #fff;
}
.badge-icon.locked {
  background: #e2e8f0;
  color: #94a3b8;
  filter: grayscale(1);
}
.badge-name {
  font-size: 12px;
  color: var(--text-primary);
  text-align: center;
}
.records-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: var(--shadow-sm);
  margin-top: 24px;
}
.records-card h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
}
.record-item {
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 16px 20px;
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.rec-left {
  flex: 1;
}
.rec-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 4px;
}
.rec-sub {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}
.rec-date {
  font-size: 13px;
  color: var(--text-secondary);
}
.rec-duration {
  margin-top: 4px;
  font-size: 13px;
  color: var(--text-secondary);
}
.score-row {
  margin-top: 8px;
  font-size: 13px;
  color: #f59e0b;
}
@media (max-width: 768px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>
