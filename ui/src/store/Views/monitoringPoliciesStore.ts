import { defineStore } from 'pinia'
import { cloneDeep } from 'lodash'
import { Condition, Policy, ThresholdCondition } from '@/types/policies'
import { useMonitoringPoliciesMutations } from '../Mutations/monitoringPoliciesMutations'
import { useMonitoringPoliciesQueries } from '../Queries/monitoringPoliciesQueries'
import useSnackbar from '@/composables/useSnackbar'
import { ThresholdLevels, Unknowns } from '@/components/MonitoringPolicies/monitoringPolicies.constants'
import {
  AlertCondition,
  DetectionMethod,
  EventType,
  ManagedObjectType,
  MonitorPolicy,
  PolicyRule,
  Severity,
  TimeRangeUnit
} from '@/types/graphql'
import { useAlertEventDefinitionQueries } from '@/store/Queries/alertEventDefinitionQueries'
import router from '@/router'

const { showSnackbar } = useSnackbar()

type TState = {
  selectedPolicy?: Policy
  selectedRule?: PolicyRule
  monitoringPolicies: MonitorPolicy[],
  numOfAlertsForPolicy: number
  numOfAlertsForRule: number
  validationErrors: any,
  alertRuleDrawer: boolean
}

const defaultPolicy: Policy = {
  name: '',
  memo: '',
  notifyByEmail: false,
  notifyByPagerDuty: false,
  notifyByWebhooks: false,
  tags: ['default'],
  rules: []
}

function getDefaultThresholdCondition(): ThresholdCondition {
  return {
    id: new Date().getTime(),
    level: ThresholdLevels.ABOVE,
    percentage: 50,
    forAny: 5,
    durationUnit: TimeRangeUnit.Second,
    duringLast: 60,
    periodUnit: TimeRangeUnit.Second,
    severity: Severity.Critical
  }
}

async function getDefaultEventCondition(): Promise<AlertCondition> {
  const alertEventDefinitionQueries = useAlertEventDefinitionQueries()
  const alertEventDefinitions = await alertEventDefinitionQueries.listAlertEventDefinitions(EventType.SnmpTrap)

  if (alertEventDefinitions.value?.listAlertEventDefinitions?.length) {
    return {
      id: new Date().getTime(),
      count: 1,
      severity: Severity.Critical,
      overtimeUnit: Unknowns.UNKNOWN_UNIT,
      triggerEvent: alertEventDefinitions.value.listAlertEventDefinitions[0]
    }
  } else {
    throw Error('Can\'t load alertEventDefinitions')
  }
}

async function getDefaultRule(): Promise<PolicyRule> {
  return {
    id: new Date().getTime(),
    name: '',
    componentType: ManagedObjectType.Node,
    detectionMethod: DetectionMethod.Event,
    eventType: EventType.SnmpTrap,
    alertConditions: [await getDefaultEventCondition()]
  }
}

export const useMonitoringPoliciesStore = defineStore('monitoringPoliciesStore', {
  state: (): TState => ({
    selectedPolicy: undefined,
    selectedRule: undefined,
    monitoringPolicies: [],
    numOfAlertsForPolicy: 0,
    numOfAlertsForRule: 0,
    validationErrors: {},
    alertRuleDrawer: false
  }),
  actions: {
    // used for initial population of policies
    async getMonitoringPolicies() {
      const queries = useMonitoringPoliciesQueries()
      await queries.listMonitoringPolicies()
      this.monitoringPolicies = queries.monitoringPolicies

      // we are setting this to true until the back end supports enable/disable.
      //  Then this component can just display the status.
      //  Once back end adds ability to enable/disable, then we will add another issue to implement it here and elsewhere.

      for (let i = 0; i < this.monitoringPolicies.length; i++) {
        if (!('enabled' in this.monitoringPolicies[i])) {
          this.monitoringPolicies[i].enabled = true
        }
      }
    },
    displayPolicyForm(policy?: Policy) {
      this.selectedPolicy = policy ? cloneDeep(policy) : cloneDeep(defaultPolicy)
      this.selectedRule = undefined
    },
    clearSelectedPolicy() {
      this.selectedPolicy = undefined
    },
    async displayRuleForm(rule?: PolicyRule) {
      this.selectedRule = rule ? cloneDeep(rule) : await getDefaultRule()
    },
    async resetDefaultConditions() {
      if (!this.selectedRule) {
        return
      }

      // detection method THRESHOLD
      if (this.selectedRule.detectionMethod === DetectionMethod.Threshold) {
        return (this.selectedRule.alertConditions = [getDefaultThresholdCondition()])
      }

      // detection method EVENT
      return (this.selectedRule.alertConditions = [await getDefaultEventCondition()])
    },
    async addNewCondition() {
      if (!this.selectedRule) {
        return
      }

      // detection method THRESHOLD
      if (this.selectedRule.detectionMethod === DetectionMethod.Threshold) {
        return this.selectedRule.alertConditions?.push(getDefaultThresholdCondition())
      }

      // detection method EVENT
      return this.selectedRule.alertConditions?.push(await getDefaultEventCondition())
    },
    updateCondition(id: string, condition: Condition) {
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      this.selectedRule!.alertConditions?.map((currentCondition: AlertCondition) => {
        if (currentCondition.id === id) {
          return { ...currentCondition, ...condition }
        }
        return
      })
    },
    deleteCondition(id: string) {
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      this.selectedRule!.alertConditions = this.selectedRule!.alertConditions?.filter(
        (c: AlertCondition) => c.id !== id
      )
    },
    validateRule(rule: PolicyRule) {
      this.validationErrors.ruleName = ''

      let isValid = true
      const name = (rule.name?.trim() || '').toLowerCase()

      if (!name) {
        this.validationErrors.ruleName = 'Rule name cannot be blank.'
        isValid = false
      } else {
        if (this.selectedPolicy?.rules?.some(r => (r.id !== rule.id) && (name === r.name?.toLowerCase()))) {
          this.validationErrors.ruleName = 'Duplicate rule name.'
          isValid = false
        }
      }

      return isValid
    },
    validateMonitoringPolicy(policy: Policy) {
      this.validationErrors.policyName = ''

      let isValid = true
      const name = (policy.name?.trim() || '').toLowerCase()

      if (!name) {
        this.validationErrors.policyName = 'Policy name cannot be blank.'
        isValid = false
      } else {
        if (this.monitoringPolicies.some(p => (p.id !== policy.id) && (name === p.name?.toLowerCase()))) {
          this.validationErrors.policyName = 'Duplicate policy name.'
          isValid = false
        }
      }

      return isValid
    },
    async saveRule() {
      if (this.selectedRule && !this.validateRule(this.selectedRule)) {
        return
      }

      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      const existingItemIndex = this.selectedPolicy!.rules?.findIndex(rule => rule.id === this.selectedRule!.id) ?? -1

      if (existingItemIndex !== -1) {
        // replace existing rule
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        this.selectedPolicy!.rules?.splice(existingItemIndex, 1, this.selectedRule!)
      } else {
        // add new rule
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        this.selectedPolicy!.rules?.push(this.selectedRule!)
      }

      // this.selectedRule = await getDefaultRule()
      showSnackbar({ msg: 'Rule successfully applied to the policy.' })
      this.closeAlertRuleDrawer()
    },
    async savePolicy(isCopy = false) {
      const { addMonitoringPolicy, error } = useMonitoringPoliciesMutations()

      if (!this.selectedPolicy || !this.validateMonitoringPolicy(this.selectedPolicy)) {
        return false
      }

      // modify payload to comply with current BE format
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      const policy = cloneDeep(this.selectedPolicy!)

      policy.rules = policy.rules?.map((rule) => {
        rule.alertConditions = rule.alertConditions?.map((condition) => {
          if (!policy.id) delete condition.id // don't send generated ids
          return condition
        })
        if (!policy.id) {
          delete rule.id // don't send generated ids
        }

        if (policy.isDefault) {
          delete policy.isDefault // for updating default (tags only)
        }

        return rule
      })

      if (isCopy) {
        delete policy.isDefault
        delete policy.enabled
        delete policy.id // Clear the ID to create a new policy using copy
      }

      await addMonitoringPolicy({ policy })

      if (!error.value) {
        this.selectedPolicy = undefined
        this.selectedRule = undefined
        this.validationErrors = {}
        this.getMonitoringPolicies()
        showSnackbar({ msg: 'Policy successfully applied.' })
        if (isCopy) {
          router.push('/monitoring-policies-new/')
        }
      }

      return !error.value
    },
    copyPolicy(policy: Policy) {
      const copiedPolicy = cloneDeep(policy)
      copiedPolicy.name = `Copy of ${copiedPolicy.name}`
      copiedPolicy.id = 0
      copiedPolicy.isDefault = false
      this.displayPolicyForm(copiedPolicy)
      router.push('/monitoring-policies-new/0')
    },
    copyPolicyLegacy(policy: Policy) {
      const copiedPolicy = cloneDeep(policy)
      delete copiedPolicy.isDefault
      delete copiedPolicy.id
      delete copiedPolicy.name
      this.displayPolicyForm(copiedPolicy)
    },
    async removeRule() {
      const { deleteRule } = useMonitoringPoliciesMutations()
      await deleteRule({ id: this.selectedRule?.id })

      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      const ruleIndex = this.selectedPolicy!.rules?.findIndex(rule => rule.id === this.selectedRule!.id) ?? -1

      if (ruleIndex !== -1) {
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        this.selectedPolicy!.rules?.splice(ruleIndex, 1)
      }

      this.getMonitoringPolicies()
      this.selectedRule = undefined
    },
    async countAlertsForRule() {
      const { getAlertCountByRuleId } = useMonitoringPoliciesQueries()
      const count = await getAlertCountByRuleId(this.selectedRule?.id)
      this.numOfAlertsForRule = count
    },
    async removePolicy() {
      const { deleteMonitoringPolicy } = useMonitoringPoliciesMutations()
      await deleteMonitoringPolicy({ id: this.selectedPolicy?.id })
      this.getMonitoringPolicies()
      this.selectedRule = undefined
      this.selectedPolicy = undefined
      this.clearSelectedPolicy()
    },
    async countAlerts() {
      const { getAlertCountByPolicyId } = useMonitoringPoliciesQueries()
      const count = await getAlertCountByPolicyId(this.selectedPolicy?.id)
      this.numOfAlertsForPolicy = count
    },
    async openAlertRuleDrawer(rule?: PolicyRule) {
      await this.displayRuleForm(rule)
      this.alertRuleDrawer = true
    },
    async closeAlertRuleDrawer() {
      this.alertRuleDrawer = false
      this.selectedRule = undefined
      this.validationErrors = {}
    }
  }
})
