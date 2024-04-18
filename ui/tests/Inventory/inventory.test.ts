import mountWithPiniaVillus from '../mountWithPiniaVillus'
import Inventory from '@/containers/Inventory.vue'

let wrapper: any

describe('Inventory page', () => {
  afterAll(() => {
    wrapper.unmount()
  })

  it('should have the required components', () => {
    wrapper = mountWithPiniaVillus({
      component: Inventory,
      shallow: true
    })

    const pageHeader = wrapper.getComponent('[data-test="page-header"]')
    expect(pageHeader.exists()).toBe(true)

    const featherTabContainer = wrapper.getComponent('[data-test="tab-container"]')
    expect(featherTabContainer.exists()).toBe(true)
  })
})
