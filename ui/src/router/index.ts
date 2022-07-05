import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from '@/containers/Dashboard.vue'
import Appliances from '@/containers/Appliances.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Dashboard',
      component: Dashboard
    },
    {
      path: '/appliances',
      name: 'Appliances',
      component: Appliances
    },
    {
      path: '/devices',
      name: 'Devices',
      component: () => import('@/components/Appliances/Devices/Devices.vue')
    },
    {
      path: '/:pathMatch(.*)*', // catch other paths and redirect
      redirect: '/'
    }
  ]
})

export default router
