import { MonitorPolicy, AlertCondition, AlertEventDefinition } from './graphql'

export interface Policy extends MonitorPolicy {
  isDefault?: boolean
}

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

export type Condition = ThresholdCondition | AlertCondition

export interface MonitorPolicyFilters {
  sortAscending: boolean
  sortBy?: string
}