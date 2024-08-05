import MonitoringPoliciesRules from '@/components/MonitoringPolicies/MonitoringPoliciesRules.vue'
import mount from '../mountWithPiniaVillus'
import featherInputFocusDirective from '@/directives/v-focus'
import { MonitoringPolicy } from '@/types/policies'
import MonitoringPoliciesAlertsRulesTable from '@/components/MonitoringPolicies/MonitoringPoliciesAlertsRulesTable.vue'
import { useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'
import { DetectionMethod, EventType } from '@/types/graphql'
import { CreateEditMode } from '@/types'
import MonitoringPoliciesCreateAlertRules from '@/components/MonitoringPolicies/MonitoringPoliciesCreateAlertRules.vue'
import { cloneDeep } from 'lodash'

const mockMonitoringPolicy: MonitoringPolicy = {
  id: 1,
  isDefault: false,
  memo: 'Dummy memo',
  name: 'Dummy Policy',
  notifyByEmail: true,
  notifyByPagerDuty: false,
  notifyByWebhooks: true,
  notifyInstruction: 'Notify immediately',
  rules: [
    {
      alertConditions: [
        {
          clearEvent: {
            __typename: 'AlertEventDefinition',
            clearKey: 'dummy_clear_key',
            eventType: EventType.Internal,
            id: 1,
            name: 'Dummy Alert Event',
            reductionKey: 'dummy_reduction_key',
            uei: 'dummy_uei'
          },
          count: 5,
          id: 1,
          isNew: false,
          overtime: 10,
          overtimeUnit: 'minutes',
          severity: 'high',
          triggerEvent: {
            __typename: 'AlertEventDefinition',
            clearKey: 'dummy_clear_key',
            eventType: EventType.Internal,
            id: 1,
            name: 'Dummy Alert Event',
            reductionKey: 'dummy_reduction_key',
            uei: 'dummy_uei'
          }
        }
      ],
      detectionMethod: DetectionMethod.Threshold,
      eventType: EventType.Internal,
      id: 1,
      isNew: false,
      name: 'Dummy Rule',
      thresholdMetricName: 'Dummy Metric'
    }
  ],
  tags: ['tag1', 'tag2'],
  enabled: true
}

const wrapper = mount({
  component: MonitoringPoliciesRules,
  shallow: false,
  stubActions: false,
  global: {
    directives: {
      focus: featherInputFocusDirective
    }
  }
})

describe('Monitoring Policies Rules', () => {
  test('renders the MonitoringPoliciesRules component', () => {
    expect(wrapper).toBeTruthy()
  })

  test('renders MonitoringPoliciesCreateAlertRules when isCheckAlertRule is true', async () => {
    const isCheckAlertRuleCondition = await wrapper.vm.isCheckAlertRule
    const MonitoringPoliciesCreateAlertRulesCheck = wrapper.findComponent(MonitoringPoliciesCreateAlertRules)
    expect(MonitoringPoliciesCreateAlertRulesCheck).toBeTruthy()
    expect(isCheckAlertRuleCondition).toBe(true)
  })

  test('savePolicy button is disabled with empty policy name', async () => {
    const savePolicyEnableDisableComputedValue = await wrapper.vm.savePolicyEnableDisable
    expect(savePolicyEnableDisableComputedValue).toBe(true)
  })

  test('clicking cancel button resets store state after successful save', async () => {
    const store = useMonitoringPoliciesStore()
    const handleCancelbtn = await wrapper.get('[data-test="handleCancel-btn"]')
    await handleCancelbtn.trigger('click')
    expect(store.clearSelectedPolicy).toBeCalled()
    expect(store.clearSelectedRule).toBeCalled()
    expect(store.setPolicyEditMode).toHaveBeenCalledWith(CreateEditMode.None)
    expect(store.setRuleEditMode).toHaveBeenCalledWith(CreateEditMode.None)
  })

  test('savePolicy successfully saves a valid policy', async () => {
    nextTick(() => {
      wrapper.vm.savePolicy()
    })
    const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
    const store = useMonitoringPoliciesStore()
    store.selectedPolicy = cloneDeep(mockMonitoringPolicy)
    const savePolicyEnableDisableComputedValue = await wrapper.vm.savePolicyEnableDisable
    expect(savePolicyEnableDisableComputedValue).toBe(false)
  })

  test('renders MonitoringPoliciesAlertsRulesTable when policy has rules', async () => {
    const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
    const store = useMonitoringPoliciesStore()
    store.selectedPolicy = cloneDeep(mockMonitoringPolicy)

    await nextTick()
    const isCheckAlertRuleCondition = await wrapper.vm.isCheckAlertRule
    const MonitoringPoliciesAlertsRulesTableCheck = wrapper.findComponent(MonitoringPoliciesAlertsRulesTable)
    expect(MonitoringPoliciesAlertsRulesTableCheck).toBeTruthy()
    expect(isCheckAlertRuleCondition).toBe(false)
  })
})
