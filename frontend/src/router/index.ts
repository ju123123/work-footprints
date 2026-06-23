import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: () => import('../views/LoginView.vue') },
    { path: '/', component: () => import('../views/HomeView.vue') },
    { path: '/inbox', component: () => import('../views/InboxView.vue') },
    { path: '/records', component: () => import('../views/RecordsView.vue') },
    { path: '/reports', component: () => import('../views/ReportsView.vue') },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.path !== '/login' && !auth.token) {
    return '/login'
  }
  if (to.path === '/login' && auth.token) {
    return '/'
  }
})

export default router

