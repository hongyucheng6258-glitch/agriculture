import Vue from 'vue';
import VueRouter from 'vue-router';

Vue.use(VueRouter);

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    name: 'MainLayout',
    component: () => import('@/views/layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue'),
        meta: { title: '首页概览' }
      },
      {
        path: 'ai-assistant',
        name: 'AiAssistant',
        component: () => import('@/views/ai/AiAssistant.vue'),
        meta: { title: '智慧助手' }
      },
      {
        path: 'cage',
        name: 'Cage',
        component: () => import('@/views/cage/CageManage.vue'),
        meta: { title: '网箱管理' }
      },
      {
        path: 'water-quality',
        name: 'WaterQuality',
        component: () => import('@/views/environment/WaterQuality.vue'),
        meta: { title: '水质监测' }
      },
      {
        path: 'weather',
        name: 'Weather',
        component: () => import('@/views/environment/Weather.vue'),
        meta: { title: '气象监测' }
      },
      {
        path: 'simulate',
        name: 'Simulate',
        component: () => import('@/views/environment/DataSimulate.vue'),
        meta: { title: '模拟数据' }
      },
      {
        path: 'feeding',
        name: 'Feeding',
        component: () => import('@/views/breeding/FeedingManage.vue'),
        meta: { title: '投喂管理' }
      },
      {
        path: 'disease',
        name: 'Disease',
        component: () => import('@/views/breeding/DiseaseManage.vue'),
        meta: { title: '病害管理' }
      },
      {
        path: 'staff',
        name: 'Staff',
        component: () => import('@/views/staff/StaffManage.vue'),
        meta: { title: '人员管理' }
      },
      {
        path: 'alert/list',
        name: 'AlertList',
        component: () => import('@/views/alert/AlertList.vue'),
        meta: { title: '告警列表' }
      },
      {
        path: 'alert/threshold',
        name: 'AlertThreshold',
        component: () => import('@/views/alert/AlertThreshold.vue'),
        meta: { title: '告警阈值' }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/statistics/Statistics.vue'),
        meta: { title: '数据统计' }
      },
      {
        path: 'trace',
        name: 'Trace',
        component: () => import('@/views/trace/TraceManage.vue'),
        meta: { title: '溯源管理' }
      },
      {
        path: 'trace/query',
        name: 'TraceQuery',
        component: () => import('@/views/trace/TraceQuery.vue'),
        meta: { title: '溯源查询' }
      },
      {
        path: 'stock',
        name: 'Stock',
        component: () => import('@/views/stock/FeedStock.vue'),
        meta: { title: '饲料库存' }
      },
      {
        path: 'screen',
        name: 'Screen',
        component: () => import('@/views/screen/VisualScreen.vue'),
        meta: { title: '大屏展示' }
      },
      {
        path: 'backup',
        name: 'Backup',
        component: () => import('@/views/backup/DataBackup.vue'),
        meta: { title: '数据备份' }
      },
      {
          path: 'user/manage',
          name: 'UserManage',
          component: () => import('@/views/user/UserManage.vue'),
          meta: { title: '用户管理' }
        },
        {
          path: 'user/info',
          name: 'UserInfo',
          component: () => import('@/views/user/UserInfo.vue'),
          meta: { title: '用户信息' }
        }
    ]
  }
];

const router = new VueRouter({
  mode: 'hash',
  base: process.env.BASE_URL,
  routes
});

// 路由守卫：检查登录状态和权限
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 智慧渔业管理系统` : '智慧渔业管理系统';

  if (to.path === '/login') {
    next();
    return;
  }

  const token = localStorage.getItem('token');
  if (!token || token === 'undefined' || token === 'null') {
    next('/login');
    return;
  }

  // 管理员页面权限检查
  const adminPages = [
    '/user/manage',
    '/simulate',
    '/alert/threshold',
    '/trace',
    '/backup'
  ];
  const userStr = localStorage.getItem('user');
  const user = userStr ? JSON.parse(userStr) : null;
  const isAdmin = user && user.role === 'admin';

  if (adminPages.includes(to.path) && !isAdmin) {
    // 普通用户访问管理员页面时，返回首页，不打印错误
    next('/dashboard');
    return;
  }

  next();
});

export default router;
