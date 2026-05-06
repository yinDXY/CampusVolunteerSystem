/** 格式化 ISO 时间为 "YYYY-MM-DD HH:mm" */
export function formatTime(isoStr) {
  if (!isoStr) return '-'
  const d = new Date(isoStr)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/** 格式化 ISO 时间为 "YYYY-MM-DD" */
export function formatDate(isoStr) {
  if (!isoStr) return '-'
  return isoStr.substring(0, 10)
}

/** 活动状态码 → 文本 + CSS 类名 */
export function activityStatusInfo(status) {
  const map = {
    0: { text: '草稿', cls: 'badge-draft' },
    1: { text: '报名中', cls: 'badge-signing' },
    2: { text: '进行中', cls: 'badge-ongoing' },
    3: { text: '已结束', cls: 'badge-ended' },
    4: { text: '已取消', cls: 'badge-cancelled' },
  }
  return map[status] || { text: '未知', cls: 'badge-draft' }
}

/** 报名状态码 → 文本 + CSS 类名 */
export function registrationStatusInfo(status) {
  const map = {
    0: { text: '待审核', cls: 'badge-pending' },
    1: { text: '已通过', cls: 'badge-approved' },
    2: { text: '已拒绝', cls: 'badge-rejected' },
    3: { text: '已派岗', cls: 'badge-signing' },
    4: { text: '已取消', cls: 'badge-cancelled' },
  }
  return map[status] || { text: '未知', cls: 'badge-draft' }
}

export function getCategoryTag(title, desc) {
  const text = (title || '') + (desc || '')
  if (/环境|清洁|环保|植树/.test(text)) return '环境保护'
  if (/敬老|老人|助残|陪伴|孤寡/.test(text)) return '敬老助残'
  if (/教育|辅导|图书|阅读|课业|支教/.test(text)) return '教育辅导'
  if (/医疗|救护|健康|义诊/.test(text)) return '医疗救护'
  if (/文化|宣传|演出|艺术/.test(text)) return '文化宣传'
  if (/应急|救援|防灾|交通/.test(text)) return '应急救援'
  return '志愿服务'
}

export function getCategoryLabel(title, desc) {
  const text = (title || '') + (desc || '')
  if (/环境|清洁|环保|植树/.test(text)) return '环境保护'
  if (/敬老|老人|助残|陪伴|孤寡/.test(text)) return '敬老助残'
  if (/教育|辅导|图书|阅读|课业|支教/.test(text)) return '教育辅导'
  if (/医疗|救护|健康|义诊/.test(text)) return '医疗救护'
  if (/文化|宣传|演出|艺术/.test(text)) return '文化宣传'
  if (/应急|救援|防灾|交通|安全/.test(text)) return '文化宣传'
  return '志愿服务'
}

export const GRADIENTS = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
]

export const DETAIL_GRADIENTS = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
]
