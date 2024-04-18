import mountWithPiniaVillus from '../mountWithPiniaVillus'
import InventoryFilter from '@/components/Inventory/InventoryFilter.vue'

let wrapper: any

describe('InventoryFilter.vue', () => {
  beforeAll(() => {
    wrapper = mountWithPiniaVillus({
      component: InventoryFilter,
      shallow: true,
      props: {
        state: '',
        nodes: []
      }
    })
  })
  afterAll(() => {
    wrapper.unmount()
  })

  describe('Required components', () => {
    const requiredComponents = [
      'tag-manager-ctrl'
    ]

    it.each(requiredComponents)('should have "%s" component', (item) => {
      expect(wrapper.get(`[data-test="${item}"]`).exists()).toBe(true)
    })
  })
})
