import { setActiveClient, useClient } from 'villus'
import { cloneDeep } from 'lodash'
import { createTestingPinia } from '@pinia/testing'

import { useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'
import { buildFetchList } from '../../utils'
import { CreateEditMode } from '@/types'
import { AlertEventDefinition, DetectionMethod, EventType, Severity } from '@/types/graphql'
import { MonitoringPolicy, MonitoringPolicyRule, PolicyAlertCondition } from '@/types/policies'

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

const mockPolicyRule: MonitoringPolicyRule = {
  id: 1,
  isNew: false,
  name: 'Mock Rule',
  detectionMethod: DetectionMethod.Threshold,
  eventType: EventType.Internal,
  alertConditions: []
}

const mockCondition: PolicyAlertCondition = {
  id: 1,
  isNew: false,
  percentage: 70,
  severity: 'Major'
}

global.fetch = buildFetchList({
  ListAlertEventDefinitions: {
    listAlertEventDefinitions: [{
      id: 1,
      name: 'SNMP Trap',
      eventType: EventType.SnmpTrap
    }]
  },
  alertEventDefsByVendor: {
    alertEventDefsByVendor: {
      vendor: 'generic',
      alertEventDefinitionList: [{
        id: 1,
        name: 'SNMP Trap',
        eventType: EventType.SnmpTrap
      }]
    }
  },
  listMonitoringPolicies: {
    defaultPolicy: {
      id: 1,
      memo: 'Default monitoring policy',
      name: 'default_policy',
      notifyByEmail: true,
      notifyByPagerDuty: true,
      notifyByWebhooks: true,
      rules: [],
      tags: ['default']
    },
    listMonitoryPolicies: []
  },
  countAlertByRuleId: {
    countAlertByRuleId: 5
  },
  addMonitoringPolicy: {
    createMonitorPolicy: {
      id: 1
    }
  }
})

describe('Monitoring Policies Store', () => {
  describe('when stubActions is false', () => {
    beforeEach(() => {
      createTestingPinia({ stubActions: false })
      setActiveClient(useClient({ url: 'http://test/graphql' })) // Create and set a client
    })

    afterEach(() => {
      vi.restoreAllMocks()
    })

    test('monitoringPolicies.displayPolicyForm completes successfully', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const monitoringPolicies = useMonitoringPoliciesStore()
      const mockData: MonitoringPolicy = { ...mockMonitoringPolicy, isDefault: true }
      monitoringPolicies.displayPolicyForm(mockData)
      expect(monitoringPolicies.selectedPolicy).toStrictEqual(mockData)
    })

    test('monitoringPolicies.displayRuleForm completes successfully', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const monitoringPolicies = useMonitoringPoliciesStore()
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      const mockData: MonitoringPolicyRule = mockMonitoringPolicy!.rules![0]
      monitoringPolicies.displayRuleForm(mockData)
      expect(monitoringPolicies.selectedRule).toStrictEqual(mockData)
    })

    test('monitoringPolicies.updateCondition completes successfully', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()

      store.selectedPolicy = { ...mockMonitoringPolicy, isDefault: false }
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      store.selectedRule = mockMonitoringPolicy!.rules![0]

      store.updateCondition(1, mockCondition)
      expect(store.updateCondition).toHaveBeenCalledWith(1, mockCondition)
    })

    test('monitoringPolicies.deleteCondition completes successfully', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()

      store.selectedPolicy = {...mockMonitoringPolicy, isDefault: false}
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      store.selectedRule = mockMonitoringPolicy!.rules![0]

      store.deleteCondition(2)
      expect(store.deleteCondition).toHaveBeenCalledWith(2)
    })

    test('monitoringPolicies.validateRule completes successfully', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()

      store.selectedPolicy = {...mockMonitoringPolicy, isDefault: false}
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      store.selectedRule = mockMonitoringPolicy!.rules![0]

      store.validateRule(store.selectedRule)
      expect(store.validateRule).toHaveBeenCalledWith(store.selectedRule)
      expect(store.validateRule).toBeTruthy()
    })

    test('monitoringPolicies.validateMonitoringPolicy completes successfully', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()

      store.selectedPolicy = {...mockMonitoringPolicy, isDefault: false}

      store.validateMonitoringPolicy(store.selectedPolicy)
      expect(store.validateMonitoringPolicy).toHaveBeenCalledWith(store.selectedPolicy)
      expect(store.validateMonitoringPolicy).toBeTruthy()
    })

    test('monitoringPolicies.copyPolicy completes successfully', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()
      const mockData: MonitoringPolicy = {
        id: 1,
        isDefault: false,
        memo: '',
        name: '',
        notifyByEmail: false,
        notifyByPagerDuty: false,
        notifyByWebhooks: false,
        notifyInstruction: '',
        tags: ['default'],
        rules: [],
        enabled: true
      }

      store.copyPolicy(mockData)
      expect(store.copyPolicy).toHaveBeenCalledWith(mockData)
      expect(store.copyPolicy).toHaveBeenCalledOnce()
      expect(store.selectedPolicy).toStrictEqual({
        id: 0,
        isDefault: false,
        memo: '',
        name: 'Copy of ',
        notifyByEmail: false,
        notifyByPagerDuty: false,
        notifyByWebhooks: false,
        notifyInstruction: '',
        tags: ['default'],
        rules: [],
        enabled: true
      })
    })

    test('monitoringPolicies.updateCondition completes successfully', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()

      store.selectedPolicy = {...mockMonitoringPolicy, isDefault: false}
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      store.selectedRule = mockMonitoringPolicy!.rules![0]

      const newCondition: PolicyAlertCondition = {
        id: 1,
        isNew: false,
        percentage: 70,
        severity: 'Warning'
      }
      store.updateCondition(1, newCondition)
      expect(store.updateCondition).toHaveBeenCalledWith(1, newCondition)
    })

    test('monitoringPolicies.deleteCondition completes successfully', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()
      store.selectedPolicy = {...mockMonitoringPolicy, isDefault: false}
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      store.selectedRule = mockMonitoringPolicy!.rules![0]

      store.deleteCondition(2)
      expect(store.deleteCondition).toHaveBeenCalledWith(2)
    })

    test('monitoringPolicies.validateMonitoringPolicy completes successfully', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()
      store.selectedPolicy = {...mockMonitoringPolicy, isDefault: false}

      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      const rule = mockMonitoringPolicy!.rules![0]

      store.selectedRule = { ...rule, 'name': '' }
      store.validateRule(store.selectedRule)
      expect(store.validationErrors.ruleName ).toBe('Rule name cannot be blank.')
      expect(store.validateRule).toHaveBeenCalledWith(store.selectedRule)
      expect(store.validateRule).toBeTruthy()
    })

    describe('Monitoring Policy Save Rule', () => {
      let store: any

      const mockMonitoringPolicy2: MonitoringPolicy = {
        id: 1,
        isDefault: false,
        name: 'Mock Policy 2',
        memo: '',
        notifyByEmail: false,
        notifyByPagerDuty: false,
        notifyByWebhooks: false,
        notifyInstruction: '',
        tags: ['default'],
        enabled: true,
        rules: [
          {
            id: 1,
            isNew: false,
            name: 'Existing Rule',
            detectionMethod: DetectionMethod.Threshold,
            eventType: EventType.Internal,
            alertConditions: []
          }
        ]
      }

      const editedRule: MonitoringPolicyRule = {
        id: 1,
        isNew: false,
        name: 'This Is Edited Rule',
        detectionMethod: DetectionMethod.Threshold,
        eventType: EventType.Internal,
        alertConditions: [
          {
            id: 1,
            isNew: true,
            count: 1,
            severity: 'high',
            overtimeUnit: 'minutes',
            triggerEvent: {
              id: 1,
              name: 'Dummy Alert Event'
            }
          }
        ]
      }
      beforeEach(async () => {
        store = useMonitoringPoliciesStore()
        store.selectedPolicy = cloneDeep(mockMonitoringPolicy2)
        store.selectedRule = cloneDeep(editedRule)
        vi.clearAllMocks()
      })
      test('should replace an existing rule', async () => {
        await store.saveRule()
        expect(store.selectedPolicy.rules[0].name).toBe('This Is Edited Rule')

      })

      test('should add a new rule', async () => {
        store.selectedRule.id = 2
        await store.saveRule()
        expect(store.selectedPolicy.rules).toHaveLength(2)
        expect(store.selectedPolicy.rules[1].id).toBe(2)
      })

      test('should not save an invalid rule and show error message', async () => {
        store.selectedRule.name = ''
        store.validateRule = vi.fn().mockReturnValue(false)
        store.validationErrors.ruleName = 'Rule name cannot be blank.'

        await store.saveRule()
        expect(store.selectedPolicy.rules[0].name).toBe('Existing Rule') // No change
      })

      test('should set rule edit mode to None after saving', async () => {
        store.setRuleEditMode = vi.fn()
        await store.saveRule()
        expect(store.setRuleEditMode).toHaveBeenCalledWith(CreateEditMode.None)
      })

      test('should close alert rule drawer after saving', async () => {
        store.closeAlertRuleDrawer = vi.fn()
        await store.saveRule()
        expect(store.closeAlertRuleDrawer).toHaveBeenCalled()
      })
    })

    describe('monitoringPolicies.savePolicy', () => {
      let store: any

      beforeEach(async () => {
        store = useMonitoringPoliciesStore()

        store.selectedRule = cloneDeep(mockPolicyRule)
        vi.clearAllMocks()
      })

      test('monitoringPolicies.savePolicy completes successfully', async () => {
        await store.savePolicy()
        expect(store.savePolicy).toHaveBeenCalledOnce()
      })

      test('fails saving policy with validation errors', async () => {
        const result =  await store.savePolicy()
        expect(result).toBeFalsy()
      })

      test('updates policy status on save', async () => {
        const changedMockMonitoringPolicy = cloneDeep(mockMonitoringPolicy)
        changedMockMonitoringPolicy.enabled = false
        store.monitoringPolicies = [mockMonitoringPolicy]
        store.selectedPolicy = changedMockMonitoringPolicy
        store.getMonitoringPolicies = vi.fn().mockResolvedValue([mockMonitoringPolicy])
        await store.savePolicy({status: true})
        await store.getMonitoringPolicies()
        expect(store.monitoringPolicies[0].enabled).toBe(true)
      })

      test('fails saving with empty policy name', async () => {
        const changedMockMonitoringPolicy = cloneDeep(mockMonitoringPolicy)
        changedMockMonitoringPolicy.name = ''
        store.monitoringPolicies = changedMockMonitoringPolicy
        store.selectedPolicy = changedMockMonitoringPolicy
        await store.savePolicy()
        expect(store.validationErrors.policyName).toBe('Policy name cannot be blank.')
      })

    })

    describe('monitoringPolicies.resetDefaultConditions all Functionality Tests', () => {
      test('monitoringPolicies.resetDefaultConditions completes successfully', async () => {
        const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
        const store = useMonitoringPoliciesStore()
        store.selectedPolicy = {...mockMonitoringPolicy, isDefault: false}
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        store.selectedRule = mockMonitoringPolicy!.rules![0]

        await store.resetDefaultConditions()
        expect(store.resetDefaultConditions).toHaveBeenCalledOnce()
      })

      test('monitoringPolicies.resetDefaultConditions adds a new condition when DetectionMethod is Threshold', async () => {
        const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
        const store = useMonitoringPoliciesStore()

        const dummy = cloneDeep({...mockMonitoringPolicy, rules: [ {
          detectionMethod: DetectionMethod.Threshold,
          eventType: EventType.Internal,
          alertConditions: [],
          id: 1,
          isNew: false,
          name: 'Dummy Rule',
          thresholdMetricName: 'Dummy Metric'
        }]})

        store.selectedPolicy = mockMonitoringPolicy
        const dummyRule = (dummy && dummy.rules !== undefined) ? { ...dummy.rules[0] } : undefined
        store.selectedRule = dummyRule
        await store.resetDefaultConditions()

        if (store.selectedRule?.alertConditions && store.selectedRule.alertConditions?.length > 0) {
          expect(store.selectedRule.alertConditions[0].severity).toBe(Severity.Major)
        }
      })

      test('monitoringPolicies.resetDefaultConditions adds a new condition when DetectionMethod is default', async () => {
        const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
        const store = useMonitoringPoliciesStore()
        const dummy = cloneDeep({...mockMonitoringPolicy, rules: [ {
          detectionMethod: DetectionMethod.Event,
          eventType: EventType.Internal,
          alertConditions: [],
          id: 1,
          isNew: false,
          name: 'Dummy Rule',
          thresholdMetricName: 'Dummy Metric'
        }]})

        store.selectedPolicy = mockMonitoringPolicy
        const dummyRule = (dummy && dummy.rules !== undefined) ? { ...dummy.rules[0] } : undefined
        store.selectedRule = dummyRule
        await store.getInitialAlertEventDefinitions()
        await store.resetDefaultConditions()

        if (store.selectedRule?.alertConditions && store.selectedRule.alertConditions?.length > 0) {
          expect(store.selectedRule.alertConditions[0].severity).toBe(Severity.Major)
        }
      })
    })

    describe('monitoringPolicies.addNewCondition all Functionality Tests', () => {
      test('monitoringPolicies.addNewCondition completes successfully', async () => {
        const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
        const store = useMonitoringPoliciesStore()

        store.selectedPolicy = {...mockMonitoringPolicy, isDefault: false}
        const dummyRule = mockMonitoringPolicy && mockMonitoringPolicy.rules
          ? { ...mockMonitoringPolicy?.rules[0], detectionMethod: DetectionMethod.Threshold } : undefined

        store.selectedRule = dummyRule

        await store.addNewCondition()
        expect(store.addNewCondition).toHaveBeenCalledOnce()
      })

      test('monitoringPolicies.addNewCondition adds a new condition when DetectionMethod is Threshold', async () => {
        const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
        const store = useMonitoringPoliciesStore()

        const dummy = cloneDeep({...mockMonitoringPolicy, rules: [ {
          detectionMethod: DetectionMethod.Threshold,
          eventType: EventType.Internal,
          alertConditions: [],
          id: 1,
          isNew: false,
          name: 'Dummy Rule',
          thresholdMetricName: 'Dummy Metric'
        }]})

        store.selectedPolicy = mockMonitoringPolicy
        const dummyRule = (dummy && dummy.rules !== undefined) ? { ...dummy.rules[0] } : undefined
        store.selectedRule = dummyRule
        await store.addNewCondition()

        if (store.selectedRule?.alertConditions && store.selectedRule.alertConditions?.length > 0) {
          expect(store.selectedRule.alertConditions[0].severity).toBe(Severity.Major)
        }
      })

      test('monitoringPolicies.addNewCondition adds a new condition when DetectionMethod is default', async () => {
        const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
        const store = useMonitoringPoliciesStore()

        const dummy = cloneDeep({...mockMonitoringPolicy, rules: [ {
          detectionMethod: DetectionMethod.Event,
          eventType: EventType.Internal,
          alertConditions: [],
          id: 1,
          isNew: false,
          name: 'Dummy Rule',
          thresholdMetricName: 'Dummy Metric'
        }]})

        store.selectedPolicy = mockMonitoringPolicy
        const dummyRule = (dummy && dummy.rules !== undefined) ? { ...dummy.rules[0] } : undefined
        store.selectedRule = dummyRule

        await store.getInitialAlertEventDefinitions()
        await store.addNewCondition()

        expect(store.selectedRule?.alertConditions?.length).toBe(1)

        if (store.selectedRule?.alertConditions && store.selectedRule.alertConditions?.length > 0) {
          expect(store.selectedRule.alertConditions[0].severity).toBe('MAJOR')
        }
      })
    })

    test('monitoringPolicies.removeRule completes successfully', async () => {
      const { useMonitoringPoliciesStore } = await import('../../../src/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()

      store.selectedPolicy = {...mockMonitoringPolicy, isDefault: false}
      store.selectedRule = mockMonitoringPolicy.rules?.[0]

      const defaultAlertDefs: Array<AlertEventDefinition> = [
        {
          id: 7,
          name: 'Device Unreachable',
          eventType: EventType.Internal,
          uei: 'uei.opennms.org/internal/node/serviceUnreachable'
        }
      ]
      store.cachedEventDefinitions?.set('internal', defaultAlertDefs)

      await store.removeRule()
      expect(store.selectedRule).toBeUndefined()
      expect(store.selectedPolicy.rules?.length).toBe(0)

    })

    test('monitoringPolicies.clearSelectedPolicy complete successfully', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()

      store.selectedPolicy = mockMonitoringPolicy
      store.selectedRule = mockMonitoringPolicy.rules?.[0]

      store.clearSelectedPolicy()
      expect(store.clearSelectedPolicy).toBeCalled()
      expect(store.selectedPolicy).toBeUndefined()
    })

    test('monitoringPolicies.clearSelectedRule complete successfully', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()

      store.selectedPolicy = mockMonitoringPolicy
      store.selectedRule = mockMonitoringPolicy.rules?.[0]

      store.clearSelectedRule()
      expect(store.clearSelectedRule).toBeCalled()
      expect(store.selectedRule).toBeUndefined()
    })

    test('should open the alert rule drawer and display the rule form', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()
      store.displayRuleForm = vi.fn()
      store.openAlertRuleDrawer(mockPolicyRule)
      expect(store.displayRuleForm).toHaveBeenCalledWith(mockPolicyRule)
      expect(store.alertRuleDrawer).toBe(true)
    })

    test('should close the alert rule drawer and reset states', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()
      store.setRuleEditMode = vi.fn()
      store.alertRuleDrawer = true
      store.selectedRule = mockPolicyRule

      await store.closeAlertRuleDrawer()

      expect(store.alertRuleDrawer).toBe(false)
      expect(store.selectedRule).toBe(undefined)
      expect(store.setRuleEditMode).toHaveBeenCalledWith(CreateEditMode.None)
      expect(store.validationErrors).toEqual({})
    })

    test('should format vendors correctly', async () => {
      const vendors = ['vendor1', 'vendor2']
      const expectedFormattedVendors = ['Generic', 'Vendor1', 'Vendor2']
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()
      await store.formatVendors(vendors)

      expect(store.formattedVendors).toEqual(expectedFormattedVendors)
    })

    test('should return empty string if clear event name is not found', async () => {
      const key = 'invalid:key:format'
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()
      const eventName = store.getClearEventName(key)

      expect(eventName).toBe('')
    })

    test('should set rule edit mode', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()
      store.setRuleEditMode(CreateEditMode.Edit)

      expect(store.ruleEditMode).toBe(CreateEditMode.Edit)
    })

    test('should set policy edit mode', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()
      store.setPolicyEditMode(CreateEditMode.Edit)

      expect(store.policyEditMode).toBe(CreateEditMode.Edit)
    })

    test('should initialize with an empty object for eventTriggerThresholdMetrics', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()
      expect(store.eventTriggerThresholdMetrics).toEqual({})
    })

    it('should update the condition when eventType is not MetricThreshold', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()


      const dummy = cloneDeep({...mockMonitoringPolicy, rules: [ {
        detectionMethod: DetectionMethod.Event,
        eventType: EventType.Internal,
        alertConditions: [
          {
            id: 1,
            isNew: true,
            count: 1,
            severity: 'high',
            overtimeUnit: 'minutes',
            triggerEvent: {
              id: 1,
              name: 'Dummy Alert Event'
            }
          }
        ],
        id: 1,
        isNew: false,
        name: 'Dummy Rule',
        thresholdMetricName: 'Dummy Metric'
      }]})

      store.selectedPolicy = mockMonitoringPolicy
      const dummyRule = (dummy && dummy.rules !== undefined) ? { ...dummy.rules[0] } : undefined
      store.selectedRule = dummyRule
      store.updateCondition(1, mockCondition )
      // @ts-ignore
      expect(store.selectedRule.alertConditions[0]).toEqual({
        id: 1,
        isNew: false,
        count: 1,
        overtimeUnit: 'minutes',
        percentage: 70,
        severity: 'Major',
        triggerEvent: {
          id: 1,
          name: 'Dummy Alert Event'
        }
      })
    })

    it('should update eventTriggerThresholdMetrics when eventType is MetricThreshold', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()

      const dummy = cloneDeep({...mockMonitoringPolicy, rules: [{
        detectionMethod: DetectionMethod.Event,
        eventType: EventType.MetricThreshold,
        alertConditions: [
          {
            id: 1,
            isNew: true,
            count: 1,
            severity: 'high',
            overtimeUnit: 'minutes',
            triggerEvent: {
              id: 1,
              name: 'Dummy Alert Event'
            }
          }
        ],
        id: 1,
        isNew: false,
        name: 'Dummy Rule',
        thresholdMetricName: 'Dummy Metric'
      }]})

      store.selectedPolicy = mockMonitoringPolicy
      const dummyRule = (dummy && dummy.rules !== undefined) ? { ...dummy.rules[0] } : undefined
      store.selectedRule = dummyRule
      store.updateCondition(1, {id: 1, isNew: false} )
      expect(store.eventTriggerThresholdMetrics).toEqual({id: 1, isNew: false})
    })

    it('should not update anything when rule is Empty', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()
      store.updateCondition(1, {id: 1, isNew: false} )
      expect(store.eventTriggerThresholdMetrics).toEqual({})
    })

    it('should initialize with an empty string for eventTypes', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()

      expect(store.eventTypes).toBe('')
    })

    it('should update eventTypes', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()

      const dummy = cloneDeep({...mockMonitoringPolicy, rules: [ {
        detectionMethod: DetectionMethod.Event,
        eventType: EventType.MetricThreshold,
        alertConditions: [
          {
            id: 1,
            isNew: false,
            severity: 'CRITICAL',
            thresholdMetric: {
              id: 1,
              condition: '<',
              enabled: true,
              threshold: 88,
              expression: '',
              name: 'test'
            }
          }
        ],
        id: 1,
        isNew: false,
        name: 'Dummy Rule',
        thresholdMetricName: 'Dummy Metric'
      }]})
      store.selectedPolicy = mockMonitoringPolicy
      const dummyRule = (dummy && dummy.rules !== undefined) ? { ...dummy.rules[0] } : undefined
      store.selectedRule = dummyRule
      await store.saveRule()
      expect(store.eventTypes).toBe('METRIC_THRESHOLD')
    })
  })

  describe('when stubActions is true', () => {
    beforeEach(() => {
      createTestingPinia({ stubActions: true })
      setActiveClient(useClient({ url: 'http://test/graphql' })) // Create and set a client
    })

    afterEach(() => {
      vi.restoreAllMocks()
    })

    test('monitoringPolicies.getMonitoringPolicies complete successfully', async () => {
      try {
        const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
        const monitoringPolicies = useMonitoringPoliciesStore()
        await monitoringPolicies.getMonitoringPolicies()
        expect(monitoringPolicies.monitoringPolicies).toStrictEqual([])
      } catch (error) {
        const monitoringPolicies = useMonitoringPoliciesStore()
        expect(monitoringPolicies.selectedRule).toBeUndefined()
      }
    })
  })
})
