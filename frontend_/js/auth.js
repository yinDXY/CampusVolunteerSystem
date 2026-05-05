/**
 * auth.js — 登录态管理 + 角色检测 + 导航栏渲染
 *
 * 依赖：api.js（需先引入）
 *
 * 用法：
 *   <script src="../js/api.js"></script>
 *   <script src="../js/auth.js"></script>
 *   <script> requireAuth(); </script>           // 要求已登录
 *   <script> requireAuth(1); </script>           // 要求 role >= 1（管理员）
 */

/* ---------- Token / 用户信息存取 ---------- */

function getToken()    { return localStorage.getItem('token'); }
function getUserId()   { return Number(localStorage.getItem('userId')) || 0; }
function getRole()     { return Number(localStorage.getItem('role')) || 0; }
function getUsername()  { return localStorage.getItem('username') || ''; }
function getRealName()  { return localStorage.getItem('realName') || ''; }

function setAuth(loginData) {
    localStorage.setItem('token',    loginData.token);
    localStorage.setItem('userId',   loginData.userId);
    localStorage.setItem('role',     loginData.role);
    localStorage.setItem('username', loginData.username);
    localStorage.setItem('realName', loginData.realName);
}

function clearAuth() {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('role');
    localStorage.removeItem('username');
    localStorage.removeItem('realName');
}

function isLoggedIn() {
    return !!getToken();
}

/* ---------- 登出 ---------- */

function logout() {
    clearAuth();
    window.location.href = getBasePath() + 'login.html';
}

/* ---------- 权限校验 ---------- */

/**
 * 页面顶部调用，校验登录态及最低角色要求
 * @param {number} [minRole=0] - 最低角色：0=志愿者，1=活动管理员，2=超管
 */
function requireAuth(minRole = 0) {
    if (!isLoggedIn()) {
        window.location.href = getBasePath() + 'login.html';
        return;
    }
    if (getRole() < minRole) {
        showToast('无权限访问此页面', 'error');
        // 志愿者回活动大厅，管理员回管理首页
        setTimeout(() => {
            window.location.href = getBasePath() + (getRole() === 0 ? 'activity-list.html' : 'admin/activities.html');
        }, 1000);
    }
}

/* ---------- 角色文本 ---------- */

function roleName(role) {
    const map = { 0: '志愿者', 1: '活动管理员', 2: '系统管理员' };
    return map[role] || '未知';
}

/* ---------- 导航栏渲染 ---------- */

/**
 * 在页面顶部渲染导航栏
 * @param {string} [activePage] - 当前高亮的导航项键名
 */
function renderNavbar(activePage) {
    const role = getRole();
    const base = getBasePath();
    const name = getRealName() || getUsername();

    // 导航链接
    let navLinks = `
        <a href="${base}activity-list.html" class="${activePage === 'activities' ? 'active' : ''}">活动大厅</a>
    `;
    if (role < 1) {
        navLinks += `<a href="${base}profile.html" class="${activePage === 'profile' ? 'active' : ''}">个人中心</a>`;
    }
    if (role >= 1) {
        navLinks += `<a href="${base}admin/activities.html" class="${activePage === 'admin' ? 'active' : ''}">○ 管理后台</a>`;
    }

    const navbar = document.createElement('nav');
    navbar.className = 'navbar';
    navbar.innerHTML = `
        <a class="navbar-brand" href="${base}activity-list.html">
            <span class="navbar-logo">志</span>
            志愿服务管理系统
        </a>
        <div class="navbar-nav">
            ${navLinks}
        </div>
        <div class="navbar-right">
            <span>⛁ <span class="user-name">${escapeHtml(roleName(role))} · ${escapeHtml(name)}</span></span>
            <a href="javascript:void(0)" onclick="logout()" style="color:var(--text-secondary)">↪ 退出</a>
        </div>
    `;
    document.body.prepend(navbar);
}

/* ---------- 管理员侧边栏渲染 ---------- */

/**
 * 渲染管理员侧边栏导航
 * @param {string} activePage - 当前高亮项
 */
function renderSidebar(activePage) {
    const items = [
        { key: 'activities',    label: '活动管理', icon: '📋', href: 'activities.html' },
        { key: 'registrations', label: '报名审核', icon: '📝', href: 'registrations.html' },
        { key: 'dispatch',      label: '智能派岗', icon: '🔀', href: 'dispatch.html' },
        { key: 'checkin',       label: '签到管理', icon: '📱', href: 'checkin.html' },
        { key: 'stats',         label: '数据统计', icon: '📊', href: 'stats.html' },
    ];

    const sidebar = document.createElement('aside');
    sidebar.className = 'sidebar';
    sidebar.innerHTML = `
        <div class="sidebar-nav">
            ${items.map(item => `
                <a href="${item.href}" class="${activePage === item.key ? 'active' : ''}">
                    <span class="icon">${item.icon}</span>
                    ${item.label}
                </a>
            `).join('')}
        </div>
    `;
    document.body.appendChild(sidebar);
}

/* ---------- 分页渲染 ---------- */

/**
 * 渲染分页控件
 * @param {HTMLElement} container - 容器元素
 * @param {number} total         - 数据总条数
 * @param {number} pageNum       - 当前页码
 * @param {number} pageSize      - 每页条数
 * @param {function} onPageChange - 翻页回调 (pageNum) => {}
 */
function renderPagination(container, total, pageNum, pageSize, onPageChange) {
    const totalPages = Math.ceil(total / pageSize) || 1;
    container.innerHTML = '';

    if (totalPages <= 1) return;

    const prev = document.createElement('button');
    prev.textContent = '‹';
    prev.disabled = pageNum <= 1;
    prev.onclick = () => onPageChange(pageNum - 1);
    container.appendChild(prev);

    // 显示页码（最多显示 7 个按钮）
    const pages = [];
    if (totalPages <= 7) {
        for (let i = 1; i <= totalPages; i++) pages.push(i);
    } else {
        pages.push(1);
        if (pageNum > 3) pages.push('...');
        for (let i = Math.max(2, pageNum - 1); i <= Math.min(totalPages - 1, pageNum + 1); i++) {
            pages.push(i);
        }
        if (pageNum < totalPages - 2) pages.push('...');
        pages.push(totalPages);
    }

    pages.forEach(p => {
        const btn = document.createElement('button');
        btn.textContent = p;
        if (p === '...') {
            btn.disabled = true;
        } else {
            if (p === pageNum) btn.className = 'active';
            btn.onclick = () => onPageChange(p);
        }
        container.appendChild(btn);
    });

    const next = document.createElement('button');
    next.textContent = '›';
    next.disabled = pageNum >= totalPages;
    next.onclick = () => onPageChange(pageNum + 1);
    container.appendChild(next);
}
