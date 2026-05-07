<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { get } from '../../lib/request.js'

const yearSelect = ref(new Date().getFullYear())
const yearOptions = ref([])
const totalActivities = ref('-')
const totalVolunteers = ref('-')
const totalHours = ref('-')
const avgScore = ref('-')
const leaderboard = ref([])
const lbEmpty = ref(false)

let heatChartInstance = null
let trendChartInstance = null

onMounted(() => {
  const cur = new Date().getFullYear()
  for (let y = cur; y >= cur - 5; y--) {
    yearOptions.value.push(y)
  }
  yearSelect.value = cur
  init()
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  heatChartInstance?.dispose()
  trendChartInstance?.dispose()
})

function onResize() {
  heatChartInstance?.resize()
  trendChartInstance?.resize()
}

watch(yearSelect, () => {
  loadTrend()
})

async function init() {
  await Promise.all([loadLeaderboard(), loadHeat(), loadTrend()])
  await nextTick()
  if (heatChartInstance) heatChartInstance.resize()
  if (trendChartInstance) trendChartInstance.resize()
}

async function loadLeaderboard() {
  try {
    const list = (await get('/api/stats/leaderboard')) || []
    leaderboard.value = list.slice(0, 10)
    lbEmpty.value = !list.length
    if (list.length) {
      totalVolunteers.value = list.length
      totalHours.value = list.reduce((s, v) => s + (v.totalHours || 0), 0)
    }
  } catch {
    lbEmpty.value = true
    leaderboard.value = []
  }
}

async function loadHeat() {
  try {
    const list = (await get('/api/stats/activity-heat')) || []
    if (list.length) {
      totalActivities.value = list.length
      const totalSigned = list.reduce((s, a) => s + (a.signedCount || 0), 0)
      const totalCheckin = list.reduce((s, a) => s + (a.checkinCount || 0), 0)
      if (totalSigned > 0) {
        avgScore.value = ((totalCheckin / totalSigned) * 100).toFixed(1) + '%'
      } else {
        avgScore.value = '-'
      }
    }
    renderHeatChart(list)
  } catch {
    /* */
  }
}

function renderHeatChart(list) {
  if (!list.length) return
  const dom = document.getElementById('heatChart')
  if (!dom) return
  heatChartInstance?.dispose()
  heatChartInstance = echarts.init(dom)
  const top = list.slice(0, 10)
  const titles = top.map((a) => {
    const t = a.title || ''
    return t.length > 6 ? t.slice(0, 6) + '…' : t
  })
  heatChartInstance.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: '#fff',
      borderColor: '#e5e7eb',
      borderRadius: 8,
      textStyle: { color: '#374151', fontSize: 13 },
    },
    legend: {
      bottom: 0,
      textStyle: { color: '#6b7280', fontSize: 12 },
    },
    grid: { left: 40, right: 20, top: 16, bottom: 50, containLabel: true },
    xAxis: {
      type: 'category',
      data: titles,
      axisLabel: { fontSize: 11, color: '#6b7280', rotate: 20 },
      axisLine: { lineStyle: { color: '#e5e7eb' } },
    },
    yAxis: {
      type: 'value',
      axisLabel: { fontSize: 12, color: '#6b7280' },
      splitLine: { lineStyle: { color: '#f3f4f6', type: 'dashed' } },
    },
    series: [
      {
        name: '活动数量',
        type: 'bar',
        data: top.map((a) => a.checkinCount || 0),
        itemStyle: { color: '#3b82f6', borderRadius: [4, 4, 0, 0] },
        barMaxWidth: 32,
      },
      {
        name: '参与人数',
        type: 'bar',
        data: top.map((a) => a.signedCount || 0),
        itemStyle: { color: '#8b5cf6', borderRadius: [4, 4, 0, 0] },
        barMaxWidth: 32,
      },
    ],
  })
}

async function loadTrend() {
  const year = yearSelect.value
  try {
    const list = (await get(`/api/stats/trend?year=${year}`)) || []
    renderTrendChart(list)
  } catch {
    /* */
  }
}

function renderTrendChart(rawList) {
  const dom = document.getElementById('trendChart')
  if (!dom) return
  if (!trendChartInstance) trendChartInstance = echarts.init(dom)
  const monthData = Array.from({ length: 12 }, (_, i) => {
    const found = rawList.find((m) => m.month === i + 1)
    return {
      month: i + 1 + '月',
      activityCount: found ? found.activityCount : 0,
      participantCount: found ? found.participantCount : 0,
    }
  })
  trendChartInstance.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#e5e7eb',
      borderRadius: 8,
      textStyle: { color: '#374151', fontSize: 13 },
    },
    legend: {
      bottom: 0,
      textStyle: { color: '#6b7280', fontSize: 12 },
    },
    grid: { left: 40, right: 20, top: 16, bottom: 50, containLabel: true },
    xAxis: {
      type: 'category',
      data: monthData.map((m) => m.month),
      axisLabel: { fontSize: 12, color: '#6b7280' },
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      boundaryGap: false,
    },
    yAxis: {
      type: 'value',
      axisLabel: { fontSize: 12, color: '#6b7280' },
      splitLine: { lineStyle: { color: '#f3f4f6', type: 'dashed' } },
    },
    series: [
      {
        name: '活动数量',
        type: 'line',
        data: monthData.map((m) => m.activityCount),
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { width: 2, color: '#3b82f6' },
        itemStyle: { color: '#3b82f6' },
      },
      {
        name: '参与人数',
        type: 'line',
        data: monthData.map((m) => m.participantCount),
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { width: 2, color: '#8b5cf6' },
        itemStyle: { color: '#8b5cf6' },
      },
    ],
  })
}
</script>

<template>
  <div>
    <div class="page-header">
      <div>
        <h1 class="page-title">数据统计</h1>
        <p class="page-subtitle">查看志愿服务数据分析和趋势</p>
      </div>
      <select v-model.number="yearSelect" class="year-select">
        <option v-for="y in yearOptions" :key="y" :value="y">{{ y }}年</option>
      </select>
    </div>

    <div class="summary-grid">
      <div class="summary-card">
        <div class="card-top">
          <div class="card-icon blue">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="22"
              height="22"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
              <line x1="16" y1="2" x2="16" y2="6" />
              <line x1="8" y1="2" x2="8" y2="6" />
              <line x1="3" y1="10" x2="21" y2="10" />
            </svg>
          </div>
          <div class="trend-icon">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <polyline points="23 6 13.5 15.5 8.5 10.5 1 18" />
              <polyline points="17 6 23 6 23 12" />
            </svg>
          </div>
        </div>
        <div class="card-value">{{ totalActivities }}</div>
        <div class="card-label">总活动数</div>
      </div>
      <div class="summary-card">
        <div class="card-top">
          <div class="card-icon green">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="22"
              height="22"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
              <circle cx="9" cy="7" r="4" />
              <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
              <path d="M16 3.13a4 4 0 0 1 0 7.75" />
            </svg>
          </div>
          <div class="trend-icon">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <polyline points="23 6 13.5 15.5 8.5 10.5 1 18" />
              <polyline points="17 6 23 6 23 12" />
            </svg>
          </div>
        </div>
        <div class="card-value">{{ totalVolunteers }}</div>
        <div class="card-label">参与志愿者</div>
      </div>
      <div class="summary-card">
        <div class="card-top">
          <div class="card-icon purple">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="22"
              height="22"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <polyline points="23 6 13.5 15.5 8.5 10.5 1 18" />
              <polyline points="17 6 23 6 23 12" />
            </svg>
          </div>
          <div class="trend-icon">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <polyline points="23 6 13.5 15.5 8.5 10.5 1 18" />
              <polyline points="17 6 23 6 23 12" />
            </svg>
          </div>
        </div>
        <div class="card-value">{{ totalHours }}</div>
        <div class="card-label">累计服务时长</div>
      </div>
      <div class="summary-card">
        <div class="card-top">
          <div class="card-icon yellow">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="22"
              height="22"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <circle cx="12" cy="8" r="7" />
              <polyline points="8.21 13.89 7 23 12 20 17 23 15.79 13.88" />
            </svg>
          </div>
          <div class="trend-icon">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <polyline points="23 6 13.5 15.5 8.5 10.5 1 18" />
              <polyline points="17 6 23 6 23 12" />
            </svg>
          </div>
        </div>
        <div class="card-value">{{ avgScore }}</div>
        <div class="card-label">签到完成率</div>
      </div>
    </div>

    <div class="two-col">
      <div class="panel">
        <h2>志愿者时长排行榜</h2>
        <div v-if="lbEmpty" class="panel-empty">暂无排行数据</div>
        <div v-else class="lb-list">
          <div v-for="(v, i) in leaderboard" :key="i" class="lb-item">
            <div class="lb-left">
              <div class="lb-rank">{{ v.rankNo || i + 1 }}</div>
              <div>
                <div class="lb-name">{{ v.realName || '-' }}</div>
                <div class="lb-sub">参与 {{ v.activityCount || 0 }} 次活动</div>
              </div>
            </div>
            <div class="lb-right">
              <div class="lb-hours">{{ v.totalHours != null ? v.totalHours : 0 }}</div>
              <div class="lb-unit">小时</div>
            </div>
          </div>
        </div>
      </div>
      <div class="panel">
        <h2>活动热度统计</h2>
        <div id="heatChart" class="chart-box" />
      </div>
    </div>

    <div class="panel">
      <h2>月度参与趋势</h2>
      <div id="trendChart" class="chart-box" />
    </div>
  </div>
</template>

<style scoped>
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
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
.year-select {
  padding: 8px 14px;
  border: 1px solid var(--border);
  border-radius: 8px;
  font-size: 14px;
  color: var(--text-primary);
  background: #fff;
  cursor: pointer;
  outline: none;
}
.year-select:focus {
  border-color: var(--primary);
}
.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}
@media (max-width: 900px) {
  .summary-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
.summary-card {
  background: #fff;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  padding: 20px 24px;
}
.summary-card .card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}
.summary-card .card-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.summary-card .card-icon svg {
  width: 22px;
  height: 22px;
}
.summary-card .card-icon.blue {
  background: #dbeafe;
}
.summary-card .card-icon.blue svg {
  color: #2563eb;
}
.summary-card .card-icon.green {
  background: #dcfce7;
}
.summary-card .card-icon.green svg {
  color: #16a34a;
}
.summary-card .card-icon.purple {
  background: #f3e8ff;
}
.summary-card .card-icon.purple svg {
  color: #9333ea;
}
.summary-card .card-icon.yellow {
  background: #fef9c3;
}
.summary-card .card-icon.yellow svg {
  color: #ca8a04;
}
.trend-icon svg {
  width: 20px;
  height: 20px;
  color: #16a34a;
}
.summary-card .card-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 2px;
}
.summary-card .card-label {
  font-size: 13px;
  color: var(--text-secondary);
}
.two-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
}
@media (max-width: 900px) {
  .two-col {
    grid-template-columns: 1fr;
  }
}
.panel {
  background: #fff;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  padding: 24px;
}
.panel h2 {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 20px;
}
.lb-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.lb-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: #f8fafc;
  border-radius: 10px;
  transition: background 0.15s;
}
.lb-item:hover {
  background: #f1f5f9;
}
.lb-left {
  display: flex;
  align-items: center;
  gap: 14px;
}
.lb-rank {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  flex-shrink: 0;
  background: #e0e7ff;
  color: var(--primary);
}
.lb-item:nth-child(1) .lb-rank {
  background: #fef9c3;
  color: #b45309;
}
.lb-item:nth-child(2) .lb-rank {
  background: #f1f5f9;
  color: #475569;
}
.lb-item:nth-child(3) .lb-rank {
  background: #ffedd5;
  color: #c2410c;
}
.lb-name {
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);
}
.lb-sub {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 1px;
}
.lb-right {
  text-align: right;
}
.lb-hours {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-primary);
}
.lb-unit {
  font-size: 12px;
  color: var(--text-muted);
}
.chart-box {
  width: 100%;
  height: 380px;
}
.panel-empty {
  text-align: center;
  padding: 48px 24px;
  color: var(--text-muted);
  font-size: 14px;
}
</style>
