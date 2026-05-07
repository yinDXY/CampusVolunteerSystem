import { createRouter, createWebHistory } from 'vue-router'
import { isLoggedIn, getRole } from '../lib/auth.js'
import { showToast } from '../lib/ui.js'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { title: '登录 - 志愿服务管理系统', guestOnly: true },
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('../views/RegisterView.vue'),
    meta: { title: '注册 - 志愿服务管理系统', guestOnly: true },
  },
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/activities',
      },
      {
        path: 'activities',
        name: 'activities',
        component: () => import('../views/ActivityListView.vue'),
        meta: { nav: 'activities', title: '活动大厅 - 志愿服务管理系统' },
      },
      {
        path: 'activities/:id',
        name: 'activity-detail',
        component: () => import('../views/ActivityDetailView.vue'),
        meta: { nav: 'activities', title: '活动详情 - 志愿服务管理系统' },
      },
      {
        path: 'my-activities',
        name: 'my-activities',
        component: () => import('../views/MyActivitiesView.vue'),
        meta: { nav: 'my-activities', title: '我的活动 - 志愿服务管理系统' },
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('../views/ProfileView.vue'),
        meta: { nav: 'profile', title: '个人中心 - 志愿服务管理系统' },
      },
    ],
  },
  {
    path: '/admin',
    component: () => import('../layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, minRole: 1 },
    children: [
      { path: '', redirect: '/admin/activities' },
      {
        path: 'activities',
        name: 'admin-activities',
        component: () => import('../views/admin/AdminActivitiesView.vue'),
        meta: { sidebar: 'activities', title: '活动管理 - 志愿服务管理系统' },
      },
      {
        path: 'activities/new',
        name: 'admin-activity-new',
        component: () => import('../views/admin/AdminActivityFormView.vue'),
        meta: { sidebar: 'activities', title: '活动编辑 - 志愿服务管理系统' },
      },
      {
        path: 'activities/:id/edit',
        name: 'admin-activity-edit',
        component: () => import('../views/admin/AdminActivityFormView.vue'),
        meta: { sidebar: 'activities', title: '活动编辑 - 志愿服务管理系统' },
      },
      {
        path: 'registrations',
        name: 'admin-registrations',
        component: () => import('../views/admin/AdminRegistrationsView.vue'),
        meta: { sidebar: 'registrations', title: '报名审核 - 志愿服务管理系统' },
      },
      {
        path: 'dispatch',
        name: 'admin-dispatch',
        component: () => import('../views/admin/AdminDispatchView.vue'),
        meta: { sidebar: 'dispatch', title: '智能派岗 - 志愿服务管理系统' },
      },
      {
        path: 'checkin',
        name: 'admin-checkin',
        component: () => import('../views/admin/AdminCheckinView.vue'),
        meta: { sidebar: 'checkin', title: '签到管理 - 志愿服务管理系统' },
      },
      {
        path: 'stats',
        name: 'admin-stats',
        component: () => import('../views/admin/AdminStatsView.vue'),
        meta: { sidebar: 'stats', title: '数据统计 - 志愿服务管理系统' },
      },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/activities' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  if (to.meta.title) {
    document.title = to.meta.title
  }

  if (to.meta.guestOnly && isLoggedIn()) {
    return getRole() >= 1 ? '/admin/activities' : '/activities'
  }

  if (to.meta.requiresAuth && !isLoggedIn()) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  const minRole = to.matched.find((r) => r.meta.minRole != null)?.meta.minRole
  if (minRole != null && isLoggedIn() && getRole() < minRole) {
    showToast('无权限访问此页面', 'error')
    return getRole() === 0 ? '/activities' : '/admin/activities'
  }

  return true
})

export default router
