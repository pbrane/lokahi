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
  EventDefsByVendorRequest,
  EventType,
  ManagedObjectType,
  MonitorPolicy,
  PolicyRule,
  Severity,
  TimeRangeUnit,
  AlertEventDefinition,
  CountAffectedNodesByMonitoringPolicyVariables
} from '@/types/graphql'
import { useAlertEventDefinitionQueries } from '@/store/Queries/alertEventDefinitionQueries'
import router from '@/router'
import { CreateEditMode } from '@/types'

const { showSnackbar } = useSnackbar()

type TState = {
  selectedPolicy?: Policy
  selectedRule?: PolicyRule
  monitoringPolicies: MonitorPolicy[]
  numOfAlertsForPolicy: number
  numOfAlertsForRule: number
  validationErrors: any
  alertRuleDrawer: boolean
  vendors?: string[]
  formattedVendors?: string[]
  eventDefinitions?: AlertEventDefinition[]
  affectedNodesByMonitoringPolicyCount?: Map<number, number>
  cachedEventDefinitions?: Map<string, Array<AlertEventDefinition>>
  ruleEditMode: CreateEditMode,
  policyEditMode: CreateEditMode
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

function getDefaultEventCondition(alertDefs: Array<AlertEventDefinition>): AlertCondition {
  if (alertDefs?.length) {
    return {
      id: new Date().getTime(),
      count: 1,
      severity: Severity.Major,
      overtimeUnit: Unknowns.UNKNOWN_UNIT,
      triggerEvent: alertDefs[0]
    }
  } else {
    throw Error('Can\'t load alertEventDefinitions')
  }
}

export function getDefaultRule(alertDefs: Array<AlertEventDefinition>): PolicyRule {
  return {
    id: new Date().getTime(),
    name: '',
    componentType: ManagedObjectType.Node,
    detectionMethod: DetectionMethod.Event,
    eventType: EventType.Internal,
    alertConditions: [getDefaultEventCondition(alertDefs)],
    vendor: 'generic'
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
    alertRuleDrawer: false,
    vendors: [] as string[],
    formattedVendors: [] as string[],
    eventDefinitions: [] as AlertEventDefinition[],
    affectedNodesByMonitoringPolicyCount: new Map(),
    cachedEventDefinitions: new Map(),
    ruleEditMode: CreateEditMode.None,
    policyEditMode: CreateEditMode.None
  }),
  actions: {
    // used for initial population of policies
    async getMonitoringPolicies() {
      const queries = useMonitoringPoliciesQueries()
      await queries.listMonitoringPolicies().then(() => {
        this.monitoringPolicies = queries.monitoringPolicies
        queries.monitoringPolicies.forEach(async (policy) => {
          const request: CountAffectedNodesByMonitoringPolicyVariables = {
            id: policy.id
          }
          const count = await queries.getCountForAffectedNodeByMonitoringPolicy(request)
          this.affectedNodesByMonitoringPolicyCount?.set(policy.id, count ?? 0)
        })
      })
      // we are setting this to true until the back end supports enable/disable.
      //  Then this component can just display the status.
      //  Once back end adds ability to enable/disable, then we will add another issue to implement it here and elsewhere.

    },
    loadVendors() {
      const queries = useMonitoringPoliciesQueries()
      queries.listVendors().then((res) => {
        const sortedResponse = res?.sort()
        this.vendors = sortedResponse
        this.formatVendors(sortedResponse?.length ? sortedResponse : [])
      })
    },
    displayPolicyForm(policy?: Policy) {
      this.selectedPolicy = policy ? cloneDeep(policy) : cloneDeep(defaultPolicy)
    },
    async getInitialAlertEventDefinitions() {
      const queries = useAlertEventDefinitionQueries()
      const request: EventDefsByVendorRequest = {
        eventType: EventType.SnmpTrap,
        vendor: 'generic'
      }
      const alertDefsByVendor = await queries.listAlertEventDefinitionsByVendor(request)
      this.cachedEventDefinitions?.set('generic', alertDefsByVendor ?? [])

      const alertDefs = await queries.listAlertEventDefinitions(EventType.Internal)
      this.cachedEventDefinitions?.set('internal', alertDefs.value?.listAlertEventDefinitions ?? [])
    },
    clearSelectedPolicy() {
      this.selectedPolicy = undefined
    },
    clearSelectedRule() {
      this.selectedRule = undefined
    },
    displayRuleForm(rule?: PolicyRule) {
      this.selectedRule = rule ? cloneDeep(rule) : cloneDeep(getDefaultRule(this.cachedEventDefinitions?.get('internal') ?? []))
    },
    resetDefaultConditions() {
      if (!this.selectedRule) {
        return
      }

      // detection method THRESHOLD
      if (this.selectedRule.detectionMethod === DetectionMethod.Threshold) {
        return (this.selectedRule.alertConditions = [getDefaultThresholdCondition()])
      }

      // detection method EVENT
      return (this.selectedRule.alertConditions = [getDefaultEventCondition(this.cachedEventDefinitions?.get('internal') ?? [])])
    },
    addNewCondition() {
      if (!this.selectedRule) {
        return
      }

      // detection method THRESHOLD
      if (this.selectedRule.detectionMethod === DetectionMethod.Threshold) {
        return this.selectedRule.alertConditions?.push(getDefaultThresholdCondition())
      }

      // detection method EVENT
      return this.selectedRule.alertConditions?.push(getDefaultEventCondition(this.cachedEventDefinitions?.get('internal') ?? []))
    },
    updateCondition(id: string, condition: Condition) {
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      this.selectedRule!.alertConditions?.map((currentCondition: AlertCondition) => {
        if (currentCondition.id === id) {
          // this.refactorClearEvent(currentCondition.triggerEvent ?? {})
          return { ...currentCondition, ...condition } as AlertCondition
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
      const vendor = this.vendors?.find((x) => x.toLowerCase().indexOf(rule.vendor?.trim().toLowerCase() || '') > -1)
      if (!name) {
        this.validationErrors.ruleName = 'Rule name cannot be blank.'
        isValid = false
      } else if (!vendor) {
        this.validationErrors.ruleVendor = 'Vendor cannot be blank.'
      } else {
        if (this.selectedPolicy?.rules?.some((r) => r.id !== rule.id && name === r.name?.toLowerCase())) {
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
        if (this.monitoringPolicies.some((p) => p.id !== policy.id && name === p.name?.toLowerCase())) {
          this.validationErrors.policyName = 'Duplicate policy name.'
          isValid = false
        }
      }
      return isValid
    },
    async saveRule() {
      if (this.selectedRule && !this.validateRule(this.selectedRule)) {
        showSnackbar({
          msg: this.validationErrors.ruleName,
          error: true
        })
        return
      }

      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      const existingItemIndex = this.selectedPolicy!.rules?.findIndex((rule) => rule.id === this.selectedRule!.id) ?? -1

      if (existingItemIndex !== -1) {
        // replace existing rule
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        this.selectedPolicy!.rules?.splice(existingItemIndex, 1, this.selectedRule!)
      } else {
        // add new rule
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        this.selectedPolicy!.rules?.push(this.selectedRule!)
      }

      this.setRuleEditMode(CreateEditMode.None)

      showSnackbar({ msg: 'Rule successfully applied to the policy.' })
      this.closeAlertRuleDrawer()
    },
    async savePolicy({ status, isCopy = false, clearSelected = false }: { status?: boolean; isCopy?: boolean; clearSelected?: boolean } = {}) {
      const { addMonitoringPolicy, error } = useMonitoringPoliciesMutations()

      if (status !== undefined) {
        const updateStatus = { ...this.selectedPolicy, enabled: status }
        const { isDefault: _, ...policy } = updateStatus

        await addMonitoringPolicy({ policy })

        if (!error.value) {
          const policyToUpdate = this.monitoringPolicies?.find(policy => policy?.id === this.selectedPolicy?.id)
          if (policyToUpdate) {
            policyToUpdate.enabled = status
          }
          showSnackbar({ msg: 'Policy status has been updated successfully.' })
        }
        return !error.value
      }

      if (!this.selectedPolicy || !this.validateMonitoringPolicy(this.selectedPolicy)) {
        if (this.validationErrors.policyName) {
          showSnackbar({
            msg: this.validationErrors.policyName,
            error: true
          })
        }
        return false
      }

      // modify payload to comply with current BE format
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      const policy = cloneDeep(this.selectedPolicy!)

      policy.rules = policy.rules?.map((rule) => {
        rule.alertConditions = rule.alertConditions?.map((condition) => {
          if (!policy.id) delete condition.id // don't send generated ids
          delete condition.alertMessage
          delete condition.clearEvent
          return condition
        })
        if (!policy.id) {
          delete rule.id // don't send generated ids
        }

        if (policy.isDefault) {
          delete policy.isDefault // for updating default (tags only)
        }
        delete rule.vendor

        return rule
      })

      if (isCopy) {
        delete policy.isDefault
        delete policy.id // Clear the ID to create a new policy using copy
      }

      policy.enabled = true
      await addMonitoringPolicy({ policy })

      if (!error.value) {

        if (clearSelected) {
          this.clearSelectedPolicy()
          this.clearSelectedRule()
        }

        this.validationErrors = {}
        this.setPolicyEditMode(CreateEditMode.None)
        await this.getMonitoringPolicies()
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
      const ruleIndex = this.selectedPolicy!.rules?.findIndex((rule) => rule.id === this.selectedRule!.id) ?? -1

      if (ruleIndex !== -1) {
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        this.selectedPolicy!.rules?.splice(ruleIndex, 1)
      }

      this.selectedRule = undefined
      this.getMonitoringPolicies()
    },
    async countAlertsForRule() {
      const { getAlertCountByRuleId } = useMonitoringPoliciesQueries()
      const count = await getAlertCountByRuleId(this.selectedRule?.id)
      this.numOfAlertsForRule = count
    },
    async removePolicy() {
      const { deleteMonitoringPolicy } = useMonitoringPoliciesMutations()
      await deleteMonitoringPolicy({ id: this.selectedPolicy?.id })
      this.monitoringPolicies = []
      this.selectedRule = undefined
      this.selectedPolicy = undefined
      this.getMonitoringPolicies()
      this.clearSelectedPolicy()
    },
    async countAlerts() {
      const { getAlertCountByPolicyId } = useMonitoringPoliciesQueries()
      const count = await getAlertCountByPolicyId(this.selectedPolicy?.id)
      this.numOfAlertsForPolicy = count
    },
    openAlertRuleDrawer(rule?: PolicyRule) {
      this.displayRuleForm(rule)
      this.alertRuleDrawer = true
    },
    async closeAlertRuleDrawer() {
      this.alertRuleDrawer = false
      this.selectedRule = undefined
      this.setRuleEditMode(CreateEditMode.None)
      this.validationErrors = {}
    },
    formatVendors(vendors: string[]) {
      let uniqueItems = Array.from(new Set(vendors.map(s => s.charAt(0).toUpperCase() + s.slice(1).toLowerCase()))).sort();
      uniqueItems = uniqueItems.filter(s => s !== "Generic");
      this.formattedVendors = ['Generic', ...uniqueItems]
    },
    getClearEventName(key: string) {
      if (key) {
        const splitsOnColon = key.split(':')
        const filteredUei = splitsOnColon.find((item) => item.startsWith('uei')) || ''
        if (filteredUei) {
          const splitsOnSlash = filteredUei.split('/')
          return splitsOnSlash.pop()
        }
      }
      return ''
    },
    setRuleEditMode(mode: CreateEditMode) {
      this.ruleEditMode = mode
    },
    setPolicyEditMode(mode: CreateEditMode) {
      this.policyEditMode = mode
    },
    extractVendorFromEventUei(uei: string) {
      if (uei) {
        const splitsOnColon = uei.split(':')
        const filteredUei = splitsOnColon.find((item) => item.startsWith('uei')) || ''
        if (filteredUei) {
          const splitsOnSlash = filteredUei.split('/')
          return splitsOnSlash.find((item) => this.vendors?.includes(item))
        }
      }
      return ''
    },
    async listAlertEventDefinitionsByVendor(eventType: EventType, vendor: string) {
      const queries = useAlertEventDefinitionQueries()
      if (eventType === EventType.SnmpTrap && vendor && this.selectedRule) {
        const filteredVendor = this.vendors?.find((x) => x.toLowerCase().indexOf(vendor.toLowerCase()) > -1)
        this.selectedRule.vendor = filteredVendor

        if (this.cachedEventDefinitions?.has(vendor)) {
          this.eventDefinitions = this.cachedEventDefinitions.get(vendor)
        } else {
          if (filteredVendor) {
            const request: EventDefsByVendorRequest = {
              eventType: eventType,
              vendor: filteredVendor
            }

            const alertDefs = await queries.listAlertEventDefinitionsByVendor(request)
            this.cachedEventDefinitions?.set(filteredVendor, alertDefs ?? [])
            this.eventDefinitions = alertDefs
          }
        }
      }
      if (this.eventDefinitions && this.eventDefinitions.length > 0) {
        this.selectedRule?.alertConditions?.map((item) => {
          item.triggerEvent = this.eventDefinitions?.[0]
          return item
        })
      } else {
        this.selectedRule?.alertConditions?.map((item) => {
          item.triggerEvent = undefined
          item.clearEvent = undefined
          return item
        })
      }
    }
  }
})
