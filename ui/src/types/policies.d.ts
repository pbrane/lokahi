import { AlertEventDefinition } from './graphql'

/**
 * Not currently used; use PolicyAlertCondition instead.
 */
export interface ThresholdCondition {
  id: number
  level: string
  percentage: number
  forAny: number
  durationUnit: string
  duringLast: number
  periodUnit: string
  severity: string
  triggerEvent?: AlertEventDefinition
}

// export type Condition = ThresholdCondition | AlertCondition

export interface MonitorPolicyFilters {
  sortAscending: boolean
  sortBy?: string
}

/**
 * An Alert Condition for a PolicyRule of a MonitorPolicy.
 * This data structure can be used for SNMP Traps as well as Metric Threshold event types.
 * Some fields only apply to certain EventTypes.
 *
 * This is a superset of graphql.ts/AlertCondition but also contains Metric Threshold properties and some fields used by the UI.
 *
 * For Metric Threshold events, the metric is specified in PolicyRule.thresholdMetricName.
 * triggerEvent/clearEvent apply to Internal, SNMP Trap event types.
 *
 * @property alertMessage An alert message template for creating alert messages when this condition is triggered
 * @property alertRegex
 * @property clearEvent The clear event definition, for SNMP Traps and similar
 * @property comparator The comparator to use to compare the count or percentage to the metric data. Values include 'EQ', 'GT', 'LTE', etc.
 * @property count An integer value representing a count of an item (e.g. an SNMP Trap item defined in triggerEvent) that triggers a threshold
 * @property id Database ID. If sent in a mutation, a value here means to update an existing condition,
 *   undefined means to add this as a new condition
 * @property isNew Whether this condition is being added. Used by the front end only
 * @property overtime For time period thresholds like "3 times within the last 10 minutes",
 *   the 'count' is 3, 'overtime' is 10, 'overtimeUnit' is 'minutes'
 * @property overtimeUnit A time period unit, e.g. 'hours', 'minutes', 'seconds', 'days'
 * @property percentage A floating point value 0-100 representing a percentage of an item that triggers a threshold. Either count or
 *   percentage should be used, but not both
 * @property severity The severity level of the alert, e.g. 'critical', 'major', 'minor', etc.
 * @property triggerEvent The trigger event definition, for SNMP Traps and similar
 * @property unitLabel A text label for a unit; for example "%" if this is a percentage.
 */
export interface PolicyAlertCondition {
  id: number
  alertMessage?: string
  alertRegex?: string
  clearEvent?: AlertEventDefinition
  comparator?: string
  count?: number // integer
  isNew: boolean
  overtime?: number // integer
  overtimeUnit?: string
  percentage?: number // float
  severity?: string
  triggerEvent?: AlertEventDefinition
  unitLabel?: string
  thresholdMetric?: ThresholdMetricCondition
}

export interface ThresholdMetricCondition {
  id?: number
  enabled?: boolean
  severity?: string
  condition?: string
  threshold?: number
  name?: string
  expression?: string
}
/**
 * Similar to graphql.ts/PolicyRule.
 */
export interface MonitoringPolicyRule {
  alertConditions?: PolicyAlertCondition[]
  detectionMethod?: DetectionMethod
  eventType?: EventType
  id: number
  isNew: boolean
  name?: string
  thresholdMetricName?: string
  vendor?: string
}

/**
 * Similar to graphql.ts/MonitorPolicy.
 */
export interface MonitoringPolicy {
  id: number
  memo: string
  name: string
  notifyByEmail: boolean
  notifyByPagerDuty: boolean
  notifyByWebhooks: boolean
  notifyInstruction?: string
  rules?: MonitoringPolicyRule[]
  tags?: string[]
  enabled: boolean
  isDefault: boolean
}
