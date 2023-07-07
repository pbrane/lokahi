import { MonitorPolicy, PolicyRule, AlertCondition, AlertEventDefinition } from './graphql'

export interface Policy extends MonitorPolicy {
  rules: Rule[]
  isDefault?: boolean
}

export interface Rule extends PolicyRule {
  detectionMethod?: string
  metricName?: string
  alertConditions: Condition[]
}

interface IObjectKeys {
  [key: string]: string | number
}

export interface ThresholdCondition extends IObjectKeys {
  id: number
  level: string
  percentage: number
  forAny: number
  durationUnit: string
  duringLast: number
  periodUnit: string
  severity: string
  triggerEvent: AlertEventDefinition
}

export type EventCondition = AlertCondition & IObjectKeys
export type Condition = ThresholdCondition | EventCondition
