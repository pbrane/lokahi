import mount from '../mountWithPiniaVillus'
import featherInputFocusDirective from '@/directives/v-focus'
import MonitoringPoliciesDetailPanel from '@/components/MonitoringPolicies/MonitoringPoliciesDetailPanel.vue'
import { Policy } from '@/types/policies'
import { useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'
import { CreateEditMode } from '@/types'
import router from '@/router'
import { tr } from 'date-fns/locale'
import { cloneDeep } from 'lodash'
import { DetectionMethod, EventType, ManagedObjectType } from '@/types/graphql'


const mockMonitoringPolicy: Policy = {
    name: 'SelectedMonitoringPolicy',
    id:'1',
    memo: '',
    notifyByEmail: false,
    notifyByPagerDuty: false,
    notifyByWebhooks: false,
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
    router : router
  }
})

describe('Monitoring Policy Detail Panel', () => {

  beforeEach(()=>{
    let store = useMonitoringPoliciesStore()
    store.selectedPolicy = mockMonitoringPolicy 
  })

  test('The Monitoring Policies page container mounts correctly', () => {
    expect(wrapper).toBeTruthy()
  })

  test('The Monitoring Policies page form-title should contain the correct text', () => {
    let titleText= wrapper.get('[data-test="form-title"]').text()
    expect(titleText).toBe('SelectedMonitoringPolicy')
  })

  test('The Edit button should emit the `onEdit` event when clicked', async () => {
    await wrapper.get('[data-test="Edit"]').trigger('click',mockMonitoringPolicy)
    let store = useMonitoringPoliciesStore()
    await router.isReady()
    expect(router.currentRoute.value.params.id).toBe(mockMonitoringPolicy.id)
    expect(store.policyEditMode).toBe(CreateEditMode.Edit)
  })
  
  test('The Copy button should call onCopy method', async () => {
    const copyButton = wrapper.get('[data-test="onCopy"]')
    await copyButton.trigger('click')
    let store = useMonitoringPoliciesStore()
    expect(store.copyPolicy).toBeCalledWith(mockMonitoringPolicy)
    expect(store.selectedPolicy?.id).toBeUndefined()
    expect(store.selectedPolicy?.name).toBe(`Copy of ${mockMonitoringPolicy.name}`)
  })

  test('The Close button should call onClose method', async () => {
    const closeButton = wrapper.findComponent('[data-test="onClose"]')
    await closeButton.trigger('click')
    let store = useMonitoringPoliciesStore()
    expect(store.clearSelectedPolicy).toHaveBeenCalledOnce()
  })

  test('The alert rules should be displayed correctly', () => {
    const rules = wrapper.findAll('.alert-rule')
      // @ts-ignore 
    expect(rules.length).toBe(mockMonitoringPolicy.rules.length)
      // @ts-ignore 
    rules.forEach((ruleWrapper, index) => {
      // @ts-ignore 
    expect(ruleWrapper.text()).toContain(mockMonitoringPolicy.rules[index].name)
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
    const mockMonitoringPolicyNew: Policy = cloneDeep(mockMonitoringPolicy)
    mockMonitoringPolicyNew.rules = [
    {
      __typename: 'PolicyRule',
      alertConditions: [
        {
          __typename: 'AlertCondition',
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
      componentType: ManagedObjectType.Node,
      detectionMethod: DetectionMethod.Threshold,
      eventType: EventType.Internal,
      id: 1,
      name: 'Dummy Rule',
      thresholdMetricName: 'Dummy Metric'
    }
    ]
    mockMonitoringPolicyNew.isDefault = false
    let store = useMonitoringPoliciesStore()
    store.selectedPolicy = mockMonitoringPolicyNew
    const saveButton = wrapper.getComponent({ name: 'ButtonWithSpinner' })
    await saveButton.trigger('click.prevent')
    expect(wrapper.vm.isSavePolicyDisabled).toBe(false)
  })
})
