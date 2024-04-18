import { createRouter, createWebHistory } from 'vue-router'
import mountWithPiniaVillus from '../mountWithPiniaVillus'
import NodeStatus from '@/containers/NodeStatus.vue'

let wrapper: any
let router: any

describe('Node Status page', () => {
  afterAll(async () => {
    if (wrapper) {
      await wrapper.unmount()
    }
  })

  beforeEach(async () => {
    router = createRouter({
      history: createWebHistory(),
      routes: [
        {
          path: '/node-status/:id',
          name: 'Node Status',
          component: NodeStatus
        }
      ]
    })

    router.push('/node-status/1')
    await router.isReady()

    wrapper = mountWithPiniaVillus({
      component: NodeStatus,
      global: {
        plugins: [router]
      },
      shallow: true
    })
  })

  it('should have the required components', async () => {
    const pageHeader = await wrapper.get('[data-test="title"]')
    expect(pageHeader.exists()).toBe(true)
  })
})
