import mount from '../mountWithPiniaVillus'
import DashboardNetworkTraffic from '@/components/Dashboard/DashboardNetworkTraffic.vue'

let wrapper: any

describe('DashboardNetworkTraffic component', () => {
  beforeAll(() => {
    wrapper = mount({
      component: DashboardNetworkTraffic,
      shallow: false
    })
  })
  afterAll(() => {
    wrapper.unmount()
  })

  it('The DashboardNetworkTraffic component mounts correctly', () => {
    expect(wrapper).toBeTruthy()
  })
})
