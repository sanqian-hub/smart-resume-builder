import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useRuntimeLoader } from '../composables/useRuntimeLoader'

const routes = [
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('../views/resume/ResumeList.vue'),
        meta: { title: '我的简历' },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { title: '个人资料' },
      },
      {
        path: 'edit/:id?',
        name: 'ResumeEdit',
        component: () => import('../views/resume/ResumeEdit.vue'),
        meta: { title: '编辑简历' },
      },
      {
        path: 'preview/:id',
        name: 'ResumePreview',
        component: () => import('../views/resume/ResumePreview.vue'),
        meta: { title: '简历预览' },
      },
    ],
  },
  {
    path: '/share/:shareKey',
    name: 'ResumeShareView',
    component: () => import('../views/resume/ResumeShareView.vue'),
    meta: { title: '分享简历', public: true },
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录', guest: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { title: '注册', guest: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

const { hide: hideRuntimeLoader } = useRuntimeLoader()

router.beforeEach(async (to, from, next) => {
  document.title = `${to.meta.title || '智能简历'} - 智能简历`

  const isGuestResumeEditEntry = to.name === 'ResumeEdit' && !to.params.id
  const isHomeEntry = to.name === 'Home'

  if (to.meta.public) {
    next()
    return
  }

  if (isHomeEntry) {
    const hasLocalUser = !!localStorage.getItem('user')
    const userStore = useUserStore()
    if (hasLocalUser) {
      await userStore.init()
    }
    next()
    return
  }

  if (isGuestResumeEditEntry) {
    next()
    return
  }

  const hasLocalUser = !!localStorage.getItem('user')
  const userStore = useUserStore()

  if (to.meta.guest && hasLocalUser) {
    next('/')
  } else if (!to.meta.guest && !hasLocalUser) {
    next('/login')
  } else if (!to.meta.guest && hasLocalUser) {
    const ok = await userStore.init()
    if (!ok) {
      next('/login')
    } else {
      next()
    }
  } else {
    next()
  }
})

router.afterEach((to) => {
  if (to.name !== 'Home') {
    hideRuntimeLoader()
  }
})

export default router
