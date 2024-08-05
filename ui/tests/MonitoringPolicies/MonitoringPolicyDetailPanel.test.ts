import mount from '../mountWithPiniaVillus'
import featherInputFocusDirective from '@/directives/v-focus'
import MonitoringPoliciesDetailPanel from '@/components/MonitoringPolicies/MonitoringPoliciesDetailPanel.vue'
import { MonitoringPolicy } from '@/types/policies'
import { useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'
import { CreateEditMode } from '@/types'
import router from '@/router'
import { cloneDeep } from 'lodash'
import { DetectionMethod, EventType } from '@/types/graphql'

const mockMonitoringPolicy: MonitoringPolicy = {
  id: 1,
  name: 'SelectedMonitoringPolicy',
  memo: '',
  isDefault: false,
  enabled: true,
  notifyByEmail: false,
  notifyByPagerDuty: false,
  notifyByWebhooks: false,
  notifyInstruction: '',
  tags: [ 'default' ],
  rules: []
}

const wrapper = mount({
  component: MonitoringPoliciesDetailPanel,
  shallow: false,
  stubActions: false,
  global: {
    directives: {
      focus: featherInputFocusDirective
    },
    router: router
  }
})

describe('Monitoring Policy Detail Panel', () => {
  beforeEach(() => {
    const store = useMonitoringPoliciesStore()
    store.selectedPolicy = mockMonitoringPolicy
  })

  test('The Monitoring Policies page container mounts correctly', () => {
    expect(wrapper).toBeTruthy()
  })

  test('The Monitoring Policies page form-title should contain the correct text', () => {
    const titleText = wrapper.get('[data-test="form-title"]').text()
    expect(titleText).toBe('SelectedMonitoringPolicy')
  })

  test('The Edit button should emit the `onEdit` event when clicked', async () => {
    await wrapper.get('[data-test="Edit"]').trigger('click', mockMonitoringPolicy)
    const store = useMonitoringPoliciesStore()
    await router.isReady()
    expect(router.currentRoute.value.params.id).toBe(mockMonitoringPolicy.id?.toString())
    expect(store.policyEditMode).toBe(CreateEditMode.Edit)
  })

  test('The Copy button should call onCopy method', async () => {
    const copyButton = wrapper.get('[data-test="onCopy"]')
    await copyButton.trigger('click')
    const store = useMonitoringPoliciesStore()
    expect(store.copyPolicy).toBeCalledWith(mockMonitoringPolicy)
    expect(store.selectedPolicy?.id).toBe(0)
    expect(store.selectedPolicy?.name).toBe(`Copy of ${mockMonitoringPolicy.name}`)
  })

  test('The Close button should call onClose method', async () => {
    const closeButton = wrapper.findComponent('[data-test="onClose"]')
    await closeButton.trigger('click')
    const store = useMonitoringPoliciesStore()
    expect(store.clearSelectedPolicy).toHaveBeenCalledOnce()
  })

  test('The alert rules should be displayed correctly', () => {
    const rules = wrapper.findAll('.alert-rule')
    // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
    expect(rules.length).toBe(mockMonitoringPolicy.rules!.length)

    rules.forEach((ruleWrapper: any, index: number) => {
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      expect(ruleWrapper.text()).toContain(mockMonitoringPolicy.rules![index].name)
    })
  })

  test('The Delete button should open the confirmation modal', async () => {
    const deleteButton = wrapper.find('.delete')
    await deleteButton.trigger('click')
    expect(wrapper.vm.isVisible).toBe(true)
  })

  test('The Save button should be disabled when the form is invalid', async () => {
    const saveButton = wrapper.getComponent({ name: 'ButtonWithSpinner' })
    await saveButton.trigger('click.prevent')
    expect(wrapper.vm.isSavePolicyDisabled).toBe(true)
  })

  test('The Save button should be enabled when the form is valid', async () => {
    const mockMonitoringPolicyNew: MonitoringPolicy = cloneDeep(mockMonitoringPolicy)

    mockMonitoringPolicyNew.rules = [
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
    ]

    mockMonitoringPolicyNew.isDefault = false

    const store = useMonitoringPoliciesStore()
    store.selectedPolicy = mockMonitoringPolicyNew
    const saveButton = wrapper.getComponent({ name: 'ButtonWithSpinner' })
    await saveButton.trigger('click.prevent')
    expect(wrapper.vm.isSavePolicyDisabled).toBe(false)
  })
})
