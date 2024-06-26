import { cloneDeep } from 'lodash'
import { defineStore } from 'pinia'
import router from '@/router'
import { Unknowns } from '@/components/MonitoringPolicies/monitoringPolicies.constants'
import { createAndDownloadBlobFile, createId } from '@/components/utils'
import useSnackbar from '@/composables/useSnackbar'
import {
  mapMonitoringPolicyFromServer,
  mapToMonitorPolicyInput
} from '@/mappers/policies.mapper'
import { useAlertEventDefinitionQueries } from '@/store/Queries/alertEventDefinitionQueries'
import { CreateEditMode } from '@/types'
import {
  AlertEventDefinition,
  CountAffectedNodesByMonitoringPolicyVariables,
  DetectionMethod,
  DownloadCSVMonitoringPoliciesVariables,
  EventDefsByVendorRequest,
  EventType,
  ManagedObjectType,
  MonitorPolicyInput,
  Severity
} from '@/types/graphql'
import {
  PolicyAlertCondition,
  MonitoringPolicy,
  MonitoringPolicyRule
} from '@/types/policies'
import { useMonitoringPoliciesMutations } from '../Mutations/monitoringPoliciesMutations'
import { useMonitoringPoliciesQueries } from '../Queries/monitoringPoliciesQueries'

const { showSnackbar } = useSnackbar()

type TState = {
  selectedPolicy?: MonitoringPolicy
  selectedRule?: MonitoringPolicyRule,
  monitoringPolicies: MonitoringPolicy[]
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
  policyEditMode: CreateEditMode,
  cachedAffectedAlertsByRule?: Map<number, number>,
}

const defaultPolicy: MonitoringPolicy = {
  id: 0,
  name: '',
  memo: '',
  enabled: false,
  isDefault: false,
  notifyByEmail: false,
  notifyByPagerDuty: false,
  notifyByWebhooks: false,
  notifyInstruction: '',
  tags: ['default'],
  rules: []
}

const getDefaultEventCondition = (alertDefs: Array<AlertEventDefinition>): PolicyAlertCondition => {
  return {
    id: createId(),
    count: 1,
    severity: Severity.Major,
    overtimeUnit: Unknowns.UNKNOWN_UNIT,
    triggerEvent: alertDefs && alertDefs.length > 0 ? alertDefs[0] : undefined,
    isNew: true
  }
}

export const getDefaultRule = (alertDefs: Array<AlertEventDefinition>): MonitoringPolicyRule => {
  return {
    id: createId(),
    name: '',
    componentType: ManagedObjectType.Node,
    detectionMethod: DetectionMethod.Event,
    eventType: EventType.Internal,
    alertConditions: [getDefaultEventCondition(alertDefs)],
    vendor: 'generic',
    isNew: true
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
    policyEditMode: CreateEditMode.None,
    cachedAffectedAlertsByRule: new Map()
  }),
  actions: {
    // used for initial population of policies
    async getMonitoringPolicies() {
      const queries = useMonitoringPoliciesQueries()
      await queries.listMonitoringPolicies().then(() => {
        this.monitoringPolicies = queries.monitoringPolicies.map(mapMonitoringPolicyFromServer)

        this.monitoringPolicies.forEach(async (policy) => {
          if (policy.id) {
            const request: CountAffectedNodesByMonitoringPolicyVariables = {
              id: policy.id
            }
            const count = await queries.getCountForAffectedNodeByMonitoringPolicy(request)
            this.affectedNodesByMonitoringPolicyCount?.set(policy.id, count ?? 0)
          }
        })
      })
    },
    loadVendors() {
      const queries = useMonitoringPoliciesQueries()
      queries.listVendors().then((res) => {
        const sortedResponse = res?.sort()
        this.vendors = sortedResponse
        this.formatVendors(sortedResponse?.length ? sortedResponse : [])
      })
    },
    displayPolicyForm(policy?: MonitoringPolicy) {
      this.selectedPolicy = policy ? cloneDeep(policy) : cloneDeep(defaultPolicy)

      // set newly created policies enabled to true
      if (!policy) {
        this.selectedPolicy.enabled = true
      }
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
    displayRuleForm(rule?: MonitoringPolicyRule) {
      this.selectedRule = rule ? cloneDeep(rule) : cloneDeep(getDefaultRule(this.cachedEventDefinitions?.get('internal') ?? []))
    },
    resetDefaultConditions() {
      if (!this.selectedRule) {
        return
      }

      // detection method THRESHOLD
      // if (this.selectedRule.detectionMethod === DetectionMethod.Threshold) {
      //   return (this.selectedRule.alertConditions = [getDefaultThresholdCondition()])
      // }

      // detection method EVENT
      // return (this.selectedRule.alertConditions = [getDefaultEventCondition(this.cachedEventDefinitions?.get('internal') ?? [])])

      this.selectedRule.alertConditions = [getDefaultEventCondition([])]
    },
    addNewCondition() {
      if (!this.selectedRule) {
        return
      }

      // detection method THRESHOLD
      // if (this.selectedRule.detectionMethod === DetectionMethod.Threshold) {
      //   return this.selectedRule.alertConditions?.push(getDefaultThresholdCondition())
      // }

      // detection method EVENT
      return this.selectedRule.alertConditions?.push(getDefaultEventCondition(this.cachedEventDefinitions?.get('internal') ?? []))
    },
    updateCondition(id: number, condition: PolicyAlertCondition) {
      if (this.selectedRule && this.selectedRule?.alertConditions) {
        const index = this.selectedRule.alertConditions.findIndex(c => c.id === id)

        if (index !== undefined && index >= 0) {
          this.selectedRule.alertConditions[index] = {
            ...this.selectedRule.alertConditions[index],
            ...condition
          }
        }
      }
    },
    deleteCondition(id: number) {
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      this.selectedRule!.alertConditions = this.selectedRule!.alertConditions?.filter(
        (c: PolicyAlertCondition) => c.id !== id
      )
    },
    validateRule(rule: MonitoringPolicyRule) {
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
    validateMonitoringPolicy(policy: MonitoringPolicy) {
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
      if (this.selectedRule?.eventType === EventType.MetricThreshold || this.selectedRule?.eventType === EventType.Syslog) {
        showSnackbar({
          msg: 'Cannot yet save Alert Rule for Syslog and Metric Threshold event types.',
          error: true
        })
        return
      } else {
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
      }
    },
    // if 'status' is defined, it overwrites the current 'enabled' status
    async savePolicy({ status }: { status?: boolean } = {}) {
      if (!this.selectedPolicy || !this.validateMonitoringPolicy(this.selectedPolicy)) {
        if (this.validationErrors.policyName) {
          showSnackbar({
            msg: this.validationErrors.policyName,
            error: true
          })
        }
        return false
      }

      const { addMonitoringPolicy, error } = useMonitoringPoliciesMutations()
      const policyInput: MonitorPolicyInput = mapToMonitorPolicyInput(this.selectedPolicy)

      if (status !== undefined) {
        policyInput.enabled = status
      }

      await addMonitoringPolicy({ policy: policyInput })

      if (!error.value) {
        this.clearSelectedPolicy()
        this.clearSelectedRule()
        this.validationErrors = {}
        this.setPolicyEditMode(CreateEditMode.None)
        this.cachedAffectedAlertsByRule = new Map()
        await this.getMonitoringPolicies()
        showSnackbar({ msg: 'Policy successfully applied.' })
      }

      return !error.value
    },
    copyPolicy(policy: MonitoringPolicy) {
      const copiedPolicy = cloneDeep(policy)
      copiedPolicy.name = `Copy of ${copiedPolicy.name}`
      copiedPolicy.id = 0
      copiedPolicy.isDefault = false

      copiedPolicy.rules?.map((rule) => {
        rule.id = createId()
        rule.isNew = true
        rule.alertConditions?.map((condition) => {
          condition.id = createId()
          condition.isNew = true
          return condition
        })
        return rule
      })
      this.displayPolicyForm(copiedPolicy)
      router.push('/monitoring-policies/0')
    },
    copyRule(rule: MonitoringPolicyRule) {
      const copiedRule = cloneDeep(rule)
      copiedRule.name = `Copy of ${rule.name}`
      copiedRule.id = createId()
      copiedRule.isNew = true
      copiedRule.alertConditions?.map((condition) => {
        condition.id = createId()
        condition.isNew = true
      })
      this.selectedRule = copiedRule
      this.saveRule()
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

      if (this.selectedRule?.id) {
        this.cachedAffectedAlertsByRule?.delete(this.selectedRule?.id)
      }

      this.selectedRule = undefined
      this.getMonitoringPolicies()
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
    openAlertRuleDrawer(rule?: MonitoringPolicyRule) {
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
      let uniqueItems = Array.from(new Set(vendors.map(s => s.charAt(0).toUpperCase() + s.slice(1).toLowerCase()))).sort()
      uniqueItems = uniqueItems.filter(s => s !== 'Generic')
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
    cacheAlertsByRuleId() {
      if (this.selectedPolicy?.rules) {
        const { getAlertCountByRuleId } = useMonitoringPoliciesQueries()

        this.selectedPolicy.rules.forEach(async (rule) => {
          if (!this.cachedAffectedAlertsByRule?.has(rule.id)) {
            getAlertCountByRuleId(rule.id).then((res) => {
              this.cachedAffectedAlertsByRule?.set(rule.id, res)
            })
          }
        })
      }
    },
    async downloadMonitoringPoliciesCSV(request: DownloadCSVMonitoringPoliciesVariables) {
      const queries = useMonitoringPoliciesQueries()
      const bytes = await queries.downloadMonitoringPolices(request)
      const fileName = `Monitoring Policies - page - ${request.page + 1}.csv`
      createAndDownloadBlobFile(bytes, fileName)
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
