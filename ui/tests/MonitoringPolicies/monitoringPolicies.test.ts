import mount from '../mountWithPiniaVillus'
import MonitoringPolicies from '@/containers/MonitoringPolicies.vue'
import featherInputFocusDirective from '@/directives/v-focus'
import { useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'
import MonitoringPoliciesDetailPanel from '@/components/MonitoringPolicies/MonitoringPoliciesDetailPanel.vue'
import MonitoringPoliciesTable from '@/components/MonitoringPolicies/MonitoringPoliciesTable.vue'
import { Policy } from '@/types/policies'


const mockMonitoringPolicy: Policy = {
    name: '',
    memo: '',
    notifyByEmail: false,
    notifyByPagerDuty: false,
    notifyByWebhooks: false,
    tags: [ 'default' ],
    rules: []
}

const wrapper = mount({
  component: MonitoringPolicies,
  shallow: false,
  stubActions: false,
  global: {
    directives: {
      focus: featherInputFocusDirective
    }
  }
})

describe('Monitoring Policies', () => {
  test('The Monitoring Policies page container mounts correctly', () => {
    expect(wrapper).toBeTruthy()
  })
  test('Test if the create-policy-btn exists' , () => {
    expect(wrapper.get('[data-test="create-policy-btn"]')).toBeTruthy()
  })
  test('Test if the MonitoringPoliciesTable component exists' , () => {
    expect(wrapper.get('[data-test="MonitoringPoliciesTable"]')).toBeTruthy()
  })
  test('Test if the MonitoringPoliciesDetailPanel component exists' , () => { 
    expect(wrapper.find('[data-test="MonitoringPoliciesDetailPanel"]')).toBeTruthy()
  })
  test('The store populates with a selected policy when "New Policy" is clicked.', async () => {
    const store =useMonitoringPoliciesStore()
    const newPolicyBtn = wrapper.get('[data-test="create-policy-btn"]')
    expect(store.selectedPolicy).toBe(undefined)
    await newPolicyBtn.trigger('click')
    expect(store.displayPolicyForm).toHaveBeenCalledTimes(1)
    expect(store.selectedPolicy).toStrictEqual(mockMonitoringPolicy)
  })
  test('renders detail panel when a policy is selected', async () => {
    const table = wrapper.findComponent(MonitoringPoliciesTable)
    await table.trigger('policy-selected')
    wrapper.findComponent(MonitoringPoliciesDetailPanel)
    expect(wrapper.findComponent(MonitoringPoliciesDetailPanel)).toBeTruthy()
  })
})
