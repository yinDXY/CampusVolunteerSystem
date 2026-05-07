/**
 * api.js — fetch 封装（自动注入 Authorization 头、统一错误处理）
 *
 * 用法：
 *   import { get, post, put, patch, del } from './api.js';
 *   const data = await get('/api/activities', { pageNum: 1, pageSize: 10 });
 */

const BASE_URL = 'http://localhost:8080';

/* ---------- 核心请求方法 ---------- */

/**
 * 通用请求
 * @param {string} url      - 接口路径（如 /api/auth/login）
 * @param {object} options
 * @param {string} options.method  - HTTP 方法
 * @param {object} [options.body]  - 请求体（POST/PUT/PATCH 自动 JSON 序列化）
 * @param {object} [options.params]- Query 参数（GET 专用）
 * @returns {Promise<any>}  - 成功时返回 data 字段；失败时抛出 { code, msg }
 */
async function request(url, options = {}) {
    const { method = 'GET', body, params } = options;

    // 拼接 Query 参数
    if (params) {
        const qs = new URLSearchParams();
        for (const [k, v] of Object.entries(params)) {
            if (v !== undefined && v !== null && v !== '') {
                qs.append(k, v);
            }
        }
        const queryStr = qs.toString();
        if (queryStr) url += (url.includes('?') ? '&' : '?') + queryStr;
    }

    // 构建 Headers
    const headers = {};
    const token = localStorage.getItem('token');
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }
    if (body !== undefined) {
        headers['Content-Type'] = 'application/json';
    }

    // 发起请求
    let res;
    try {
        res = await fetch(BASE_URL + url, {
            method,
            headers,
            body: body !== undefined ? JSON.stringify(body) : undefined,
        });
    } catch (err) {
        showToast('网络异常，请检查网络连接', 'error');
        throw { code: -1, msg: '网络异常' };
    }

    // 401 → 跳转登录
    if (res.status === 401) {
        localStorage.removeItem('token');
        localStorage.removeItem('userId');
        localStorage.removeItem('role');
        localStorage.removeItem('username');
        localStorage.removeItem('realName');
        showToast('登录已过期，请重新登录', 'error');
        setTimeout(() => {
            window.location.href = getBasePath() + 'login.html';
        }, 1000);
        throw { code: 401, msg: '未登录或Token已过期' };
    }

    // 解析 JSON
    const result = await res.json();

    // 业务错误
    if (result.code !== 200) {
        showToast(result.msg || '操作失败', 'error');
        throw { code: result.code, msg: result.msg };
    }

    return result.data;
}

/* ---------- 便捷方法 ---------- */

function get(url, params) {
    return request(url, { method: 'GET', params });
}

function post(url, body) {
    return request(url, { method: 'POST', body });
}

function put(url, body) {
    return request(url, { method: 'PUT', body });
}

function patch(url, body) {
    return request(url, { method: 'PATCH', body });
}

function del(url) {
    return request(url, { method: 'DELETE' });
}

/* ---------- Toast 提示 ---------- */

function showToast(msg, type = 'info', duration = 3000) {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = 'toast toast-' + type;
    toast.textContent = msg;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transition = 'opacity .25s';
        setTimeout(() => toast.remove(), 250);
    }, duration);
}

/* ---------- 辅助：根据页面深度计算基础路径 ---------- */

function getBasePath() {
    // 如果当前在 admin/ 子目录，返回 '../'，否则返回 ''
    const path = window.location.pathname;
    if (path.includes('/admin/')) {
        return '../';
    }
    return '';
}

/* ---------- 确认弹窗 ---------- */

function showConfirm(title, message) {
    return new Promise((resolve) => {
        // 移除已有弹窗
        const old = document.querySelector('.modal-overlay');
        if (old) old.remove();

        const overlay = document.createElement('div');
        overlay.className = 'modal-overlay show';
        overlay.innerHTML = `
            <div class="modal">
                <h3>${escapeHtml(title)}</h3>
                <p>${escapeHtml(message)}</p>
                <div class="modal-actions">
                    <button class="btn btn-outline" id="modal-cancel">取消</button>
                    <button class="btn btn-primary" id="modal-confirm">确认</button>
                </div>
            </div>
        `;
        document.body.appendChild(overlay);

        overlay.querySelector('#modal-cancel').onclick = () => { overlay.remove(); resolve(false); };
        overlay.querySelector('#modal-confirm').onclick = () => { overlay.remove(); resolve(true); };
        overlay.addEventListener('click', (e) => { if (e.target === overlay) { overlay.remove(); resolve(false); } });
    });
}

/* ---------- HTML 转义 ---------- */

function escapeHtml(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

/* ---------- 格式化工具 ---------- */

/** 格式化 ISO 时间为 "YYYY-MM-DD HH:mm" */
function formatTime(isoStr) {
    if (!isoStr) return '-';
    const d = new Date(isoStr);
    const pad = n => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

/** 格式化 ISO 时间为 "YYYY-MM-DD" */
function formatDate(isoStr) {
    if (!isoStr) return '-';
    return isoStr.substring(0, 10);
}

/** 活动状态码 → 文本 + CSS 类名 */
function activityStatusInfo(status) {
    const map = {
        0: { text: '草稿',   cls: 'badge-draft' },
        1: { text: '报名中', cls: 'badge-signing' },
        2: { text: '进行中', cls: 'badge-ongoing' },
        3: { text: '已结束', cls: 'badge-ended' },
        4: { text: '已取消', cls: 'badge-cancelled' },
    };
    return map[status] || { text: '未知', cls: 'badge-draft' };
}

/** 报名状态码 → 文本 + CSS 类名 */
function registrationStatusInfo(status) {
    const map = {
        0: { text: '待审核', cls: 'badge-pending' },
        1: { text: '已通过', cls: 'badge-approved' },
        2: { text: '已拒绝', cls: 'badge-rejected' },
        3: { text: '已派岗', cls: 'badge-signing' },
        4: { text: '已取消', cls: 'badge-cancelled' },
    };
    return map[status] || { text: '未知', cls: 'badge-draft' };
}
