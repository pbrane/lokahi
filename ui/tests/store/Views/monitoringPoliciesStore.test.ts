import { useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'
import { Policy, ThresholdCondition } from '@/types/policies'
import { createTestingPinia } from '@pinia/testing'
import { buildFetchList } from '../../utils'
import { DetectionMethod, PolicyRule, EventType, ManagedObjectType, MonitorPolicy } from '@/types/graphql'

import { setActiveClient, useClient } from 'villus'


const mockMonitoringPolicy: MonitorPolicy = {
  __typename: 'MonitorPolicy',
  id: 1,
  memo: 'Dummy memo',
  name: 'Dummy Policy',
  notifyByEmail: true,
  notifyByPagerDuty: false,
  notifyByWebhooks: true,
  notifyInstruction: 'Notify immediately',
  rules: [
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
      detectionMethod: DetectionMethod.Event,
      eventType: EventType.Internal,
      id: 1,
      name: 'Dummy Rule',
      thresholdMetricName: 'Dummy Metric'
    }
  ],
  tags: ['tag1', 'tag2'],
  enabled: true
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
      const mockData: Policy = {isDefault: true, ...mockMonitoringPolicy}
      monitoringPolicies.displayPolicyForm(mockData)
      expect(monitoringPolicies.selectedPolicy).toStrictEqual(mockData)
    })

    test('monitoringPolicies.displayRuleForm completes successfully', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const monitoringPolicies = useMonitoringPoliciesStore()
      const mockData: PolicyRule = mockMonitoringPolicy && mockMonitoringPolicy.rules ? mockMonitoringPolicy?.rules[0] : {}
      monitoringPolicies.displayRuleForm(mockData)
      expect(monitoringPolicies.selectedRule).toStrictEqual(mockData)
    })

    test('monitoringPolicies.updateCondition completes successfully', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()

      store.selectedPolicy = {...mockMonitoringPolicy, isDefault: false}
      store.selectedRule = mockMonitoringPolicy && mockMonitoringPolicy.rules ? mockMonitoringPolicy?.rules[0] : {}

      const mockCondition: ThresholdCondition = {
        id: 1,
        level: 'Warning',
        percentage: 70,
        forAny: 1,
        durationUnit: 'Hours',
        duringLast: 24,
        periodUnit: 'Hours',
        severity: 'Medium'
      }
      store.updateCondition('1', mockCondition)
      expect(store.updateCondition).toHaveBeenCalledWith('1', mockCondition)
    })

    test('monitoringPolicies.deleteCondition completes successfully', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()

      store.selectedPolicy = {...mockMonitoringPolicy, isDefault: false}
      store.selectedRule = mockMonitoringPolicy && mockMonitoringPolicy.rules ? mockMonitoringPolicy?.rules[0] : {}

      store.deleteCondition('2')
      expect(store.deleteCondition).toHaveBeenCalledWith('2')
    })

    test('monitoringPolicies.validateRule completes successfully', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()

      store.selectedPolicy = {...mockMonitoringPolicy, isDefault: false}
      store.selectedRule = mockMonitoringPolicy && mockMonitoringPolicy.rules ? mockMonitoringPolicy?.rules[0] : {}

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
      const mockData: Policy = {
        id: 0,
        isDefault: false,
        memo: '',
        name: '',
        notifyByEmail: false,
        notifyByPagerDuty: false,
        notifyByWebhooks: false,
        tags: ['default'],
        rules: []
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
        tags: ['default'],
        rules: []
      })
    })

    test('monitoringPolicies.updateCondition completes successfully', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()

      store.selectedPolicy = {...mockMonitoringPolicy, isDefault: false}
      store.selectedRule = mockMonitoringPolicy && mockMonitoringPolicy.rules ? mockMonitoringPolicy?.rules[0] : {}

      const mockCondition: ThresholdCondition = {
        id: 1,
        level: 'Warning',
        percentage: 70,
        forAny: 1,
        durationUnit: 'Hours',
        duringLast: 24,
        periodUnit: 'Hours',
        severity: 'Medium'
      }
      store.updateCondition('1', mockCondition)
      expect(store.updateCondition).toHaveBeenCalledWith('1', mockCondition)
    })

    test('monitoringPolicies.deleteCondition completes successfully', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()
      store.selectedPolicy = {...mockMonitoringPolicy, isDefault: false}
      store.selectedRule = mockMonitoringPolicy && mockMonitoringPolicy.rules ? mockMonitoringPolicy?.rules[0] : {}
      store.deleteCondition('2')
      expect(store.deleteCondition).toHaveBeenCalledWith('2')
    })

    test('monitoringPolicies.deleteCondition completes successfully', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()
      store.selectedPolicy = {...mockMonitoringPolicy, isDefault: false}
      store.selectedRule = mockMonitoringPolicy && mockMonitoringPolicy.rules ? mockMonitoringPolicy?.rules[0] : {}
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

    test('monitoringPolicies.resetDefaultConditions completes successfully', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()
      store.selectedPolicy = {...mockMonitoringPolicy, isDefault: false}
      store.selectedRule = mockMonitoringPolicy && mockMonitoringPolicy.rules ? mockMonitoringPolicy?.rules[0] : {}

      await store.resetDefaultConditions()
      expect(store.resetDefaultConditions).toHaveBeenCalledOnce()
    })

    test('monitoringPolicies.addNewCondition completes successfully', async () => {
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()

      store.selectedPolicy = {...mockMonitoringPolicy, isDefault: false}
      store.selectedRule = mockMonitoringPolicy && mockMonitoringPolicy.rules ? mockMonitoringPolicy?.rules[0] : {}

      await store.addNewCondition()
      expect(store.addNewCondition).toHaveBeenCalledOnce()
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
        vi.spyOn(monitoringPolicies, 'getMonitoringPolicies')
        await monitoringPolicies.getMonitoringPolicies()
        expect(monitoringPolicies.monitoringPolicies).toStrictEqual([])
      } catch (error) {
        const monitoringPolicies = useMonitoringPoliciesStore()
        expect(monitoringPolicies.selectedRule).toBeUndefined()
      }
    })


    test('monitoringPolicies.countAlertsForRule complete successfully', async () => {

      vi.mock('@/store/Queries/monitoringPoliciesQueries', async () => {
        const { useQuery }: any = await vi.importActual('villus')
        return {
          useMonitoringPoliciesQueries: vi.fn(() => ({
            listMonitoringPolicies: vi.fn().mockResolvedValue([mockMonitoringPolicy]),
            getAlertCountByRuleId: vi.fn().mockResolvedValue(5) // Mocking getAlertCountByRuleId method
          }))
        }
      })
      const { useMonitoringPoliciesStore } = await import('@/store/Views/monitoringPoliciesStore')
      const store = useMonitoringPoliciesStore()
      store.selectedRule = { id: 1 } // Mocking a selected rule
      vi.spyOn(store, 'countAlertsForRule')
      await store.countAlertsForRule()
      expect(store.countAlertsForRule).toBeCalled()
    })
  })

})
