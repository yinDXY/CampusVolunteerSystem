<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { get, del } from '../../lib/request.js'
import { showToast, showConfirm } from '../../lib/ui.js'
import { debounce } from '../../utils/debounce.js'
import { activityStatusInfo, formatDate, getCategoryLabel } from '../../utils/display.js'
import PaginationBar from '../../components/PaginationBar.vue'

const router = useRouter()
const keyword = ref('')
const statusFilter = ref('')
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const loadError = ref(false)

const loadDebounced = debounce(() => {
  pageNum.value = 1
  load()
}, 400)

async function load() {
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
  load()
}

async function deleteActivity(id, title) {
  const ok = await showConfirm('确认删除', `确定要删除活动「${title}」吗？此操作不可恢复。`)
  if (!ok) return
  try {
    await del(`/api/activities/${id}`)
    showToast('删除成功', 'success')
    load()
  } catch {
    /* */
  }
}

function onStatusChange() {
  pageNum.value = 1
  load()
}

onMounted(load)
</script>

<template>
  <div class="admin-body-inner">
    <div class="page-header">
      <div>
        <div class="page-title">活动管理</div>
        <div class="page-subtitle">管理所有志愿活动</div>
      </div>
      <button
        type="button"
        class="btn btn-primary"
        style="padding: 10px 22px; font-size: 14px"
        @click="router.push('/admin/activities/new')"
      >
        ＋ 发布新活动
      </button>
    </div>

    <div class="toolbar">
      <div class="toolbar-left">
        <div class="search-input-wrap">
          <span class="search-icon">🔍</span>
          <input
            v-model="keyword"
            type="text"
            placeholder="搜索活动名称..."
            @input="loadDebounced"
          />
        </div>
        <select v-model="statusFilter" class="filter-select" @change="onStatusChange">
          <option value="">全部状态</option>
          <option value="0">草稿</option>
          <option value="1">报名中</option>
          <option value="2">进行中</option>
          <option value="3">已结束</option>
          <option value="4">已取消</option>
        </select>
      </div>
    </div>

    <p class="total-hint">{{ loadError ? '加载失败' : `找到 ${total} 条活动记录` }}</p>

    <table class="activity-table">
      <thead>
        <tr>
          <th>活动名称</th>
          <th>类别</th>
          <th>状态</th>
          <th>时间</th>
          <th>报名情况</th>
          <th>地点</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="loadError">
          <td colspan="7" class="empty-state">加载失败，请稍后重试</td>
        </tr>
        <tr v-else-if="list.length === 0">
          <td colspan="7" class="empty-state">暂无活动数据</td>
        </tr>
        <tr v-for="a in list" v-else-if="list.length > 0" :key="a.id">
          <td class="act-title-cell">
            <div class="act-title">{{ a.title }}</div>
            <div class="act-org">{{ a.organizer || '' }}</div>
          </td>
          <td>
            <span class="category-badge">{{ getCategoryLabel(a.title, a.description) }}</span>
          </td>
          <td>
            <span class="badge" :class="activityStatusInfo(a.status).cls">{{
              activityStatusInfo(a.status).text
            }}</span>
          </td>
          <td style="font-size: 13px; color: var(--text-secondary)">
            {{ formatDate(a.startTime)
            }}<template v-if="a.endTime && a.endTime !== a.startTime"
              ><br />至 {{ formatDate(a.endTime) }}</template
            >
          </td>
          <td class="quota-cell">
            <div class="quota-bar-wrap">
              <span style="font-size: 13px; font-weight: 600; white-space: nowrap"
                >{{ a.signedCount || 0 }}/{{ a.totalQuota || 0 }}</span
              >
              <div class="quota-bar">
                <div
                  class="quota-bar-fill"
                  :class="{
                    full: (a.totalQuota || 0) > 0 && (a.signedCount || 0) >= (a.totalQuota || 0),
                  }"
                  :style="{
                    width:
                      (a.totalQuota || 0) > 0
                        ? Math.min(100, Math.round(((a.signedCount || 0) / (a.totalQuota || 1)) * 100)) + '%'
                        : '0%',
                  }"
                />
              </div>
              <span class="quota-text">{{
                (a.totalQuota || 0) > 0
                  ? Math.min(100, Math.round(((a.signedCount || 0) / (a.totalQuota || 1)) * 100))
                  : 0
              }}%</span>
            </div>
          </td>
          <td style="font-size: 13px; color: var(--text-secondary)">{{ a.location || '—' }}</td>
          <td>
            <div class="ops-btns">
              <RouterLink class="btn-icon view" title="查看" :to="'/activities/' + a.id">👁</RouterLink>
              <button
                type="button"
                class="btn-icon edit"
                title="编辑"
                @click="router.push('/admin/activities/' + a.id + '/edit')"
              >
                ✏
              </button>
              <button type="button" class="btn-icon delete" title="删除" @click="deleteActivity(a.id, a.title)">
                🗑
              </button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>

    <PaginationBar
      style="margin-top: 20px"
      :total="total"
      :page-num="pageNum"
      :page-size="pageSize"
      @update:page-num="setPage"
    />
  </div>
</template>

<style scoped>
.admin-body-inner {
  max-width: 1400px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
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
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}
.toolbar-left {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}
.filter-select {
  padding: 9px 14px;
  border: 1px solid var(--border);
  border-radius: 8px;
  font-size: 14px;
  color: var(--text-primary);
  background: #fff;
  cursor: pointer;
  outline: none;
  transition: border-color 0.15s;
}
.filter-select:focus {
  border-color: var(--primary);
}
.search-input-wrap {
  position: relative;
}
.search-input-wrap input {
  padding: 9px 14px 9px 36px;
  border: 1px solid var(--border);
  border-radius: 8px;
  font-size: 14px;
  width: 240px;
  outline: none;
  transition: border-color 0.15s;
}
.search-input-wrap input:focus {
  border-color: var(--primary);
}
.search-input-wrap .search-icon {
  position: absolute;
  left: 11px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-muted);
  font-size: 14px;
}
.activity-table {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}
.activity-table th {
  background: #f8fafc;
  padding: 13px 18px;
  text-align: left;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--border);
}
.activity-table td {
  padding: 14px 18px;
  font-size: 14px;
  color: var(--text-primary);
  border-bottom: 1px solid #f1f5f9;
  vertical-align: middle;
}
.activity-table tr:last-child td {
  border-bottom: none;
}
.activity-table tr:hover td {
  background: #f8fafc;
}
.act-title-cell .act-title {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 2px;
}
.act-title-cell .act-org {
  font-size: 12px;
  color: var(--text-secondary);
}
.category-badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  background: var(--primary-light);
  color: var(--primary);
}
.quota-cell .quota-bar-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}
.quota-bar {
  width: 80px;
  height: 4px;
  background: #e9ecef;
  border-radius: 2px;
  overflow: hidden;
}
.quota-bar-fill {
  height: 100%;
  background: var(--primary);
  border-radius: 2px;
}
.quota-bar-fill.full {
  background: var(--success);
}
.quota-text {
  font-size: 12px;
  color: var(--text-secondary);
  white-space: nowrap;
}
.ops-btns {
  display: flex;
  gap: 6px;
  align-items: center;
}
.btn-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  transition: background 0.15s;
  text-decoration: none;
}
.btn-icon.view {
  background: var(--primary-light);
  color: var(--primary);
}
.btn-icon.view:hover {
  background: #dbeafe;
}
.btn-icon.edit {
  background: var(--success-bg);
  color: var(--success);
}
.btn-icon.edit:hover {
  background: #bbf7d0;
}
.btn-icon.delete {
  background: var(--danger-bg);
  color: var(--danger);
}
.btn-icon.delete:hover {
  background: #fecaca;
}
.total-hint {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 12px;
}
.empty-state {
  text-align: center;
  padding: 60px 0;
  color: var(--text-secondary);
  font-size: 14px;
}
</style>
