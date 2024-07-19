import mount from '../mountWithPiniaVillus'
import featherInputFocusDirective from '@/directives/v-focus'
import { MonitoringPolicy } from '@/types/policies'
import { useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'
import MonitoringPolicyBasicInformationEditAddForm from '@/components/MonitoringPolicies/MonitoringPolicyBasicInformationEditAddForm.vue'

const mockMonitoringPolicy: MonitoringPolicy = {
  name: 'SelectedMonitoringPolicy',
  id: 1,
  memo: 'Memo Value',
  notifyByEmail: false,
  notifyByPagerDuty: false,
  notifyByWebhooks: false,
  tags: [ 'default' ],
  rules: [],
  notifyInstruction: 'string',
  enabled: true,
  isDefault: false
}

const wrapper = mount({
  component: MonitoringPolicyBasicInformationEditAddForm,
  shallow: false,
  stubActions: false,
  global: {
    directives: {
      focus: featherInputFocusDirective
    }
  }
})

describe('Test for MonitoringPolicyBasicInformationEditAddForm', () => {
  let store
  beforeEach(() => {
    store = useMonitoringPoliciesStore()
    store.selectedPolicy = mockMonitoringPolicy
  })

  test('Renders policy form when selected policy exists', async () => {
    const CheckPolicyFormOnUndefined = wrapper.find('.policy-form')
    expect(CheckPolicyFormOnUndefined).toBeTruthy()
  })

  test('Policy name input reflects selected policy name', async () => {
    await wrapper.vm.$nextTick()
    const policyNameInput = wrapper.findComponent('[data-test="policy-name-input"]')
    expect(policyNameInput.exists()).toBe(true)
    expect(policyNameInput.vm.modelValue).toBe(mockMonitoringPolicy.name)
  })

  test('Policy description textarea reflects selected policy memo', async () => {
    await wrapper.vm.$nextTick()
    const policyDescriptionInput = wrapper.findComponent('[data-test="policy-Description"]')
    expect(policyDescriptionInput.exists()).toBe(true)
    expect(policyDescriptionInput.vm.modelValue).toBe(mockMonitoringPolicy.memo)
  })

  test('formattedTags computed property returns correctly formatted tags', async () => {
    await wrapper.vm.$nextTick()
    const policyTagInput = wrapper.findComponent('[data-test="policy-Tag"]')
    expect(policyTagInput.exists()).toBe(true)
    expect(policyTagInput.vm.preselectedItems).toStrictEqual([
      {
        'id': 'default',
        'name': 'default'
      }
    ])
  })
})
