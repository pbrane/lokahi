import { Comparators, InventoryFilter, KeyValueStringType, ThresholdMetricNames } from '@/types/index'

export enum ThresholdMetrics {
  OVER_UTILIZATION = 'OVER_UTILIZATION',
  SATURATION = 'SATURATION',
  ERRORS = 'ERRORS'
}

export enum ThresholdLevels {
  ABOVE = 'ABOVE',
  EQUAL_TO = 'EQUAL_TO',
  BELOW = 'BELOW',
  NOT_EQUAL_TO = 'NOT_EQUAL_TO'
}

export enum Unknowns {
  UNKNOWN_EVENT = 'UNKNOWN_EVENT',
  UNKNOWN_UNIT = 'UNKNOWN_UNIT'
}

export const conditionLetters = ['a', 'b', 'c', 'd']

export const ComparatorSigns: KeyValueStringType = Object.freeze({
  [Comparators.EQ]: '==',
  [Comparators.NE]: '!=',
  [Comparators.GT]: '>',
  [Comparators.GTE]: '>=',
  [Comparators.LT]: '<',
  [Comparators.LTE]: '<='
})

export const ComparatorText: KeyValueStringType = Object.freeze({
  [Comparators.EQ]: 'equal to',
  [Comparators.NE]: 'not equal to',
  [Comparators.GT]: 'greater than',
  [Comparators.GTE]: 'greater than or equal to',
  [Comparators.LT]: 'less than',
  [Comparators.LTE]: 'less than or equal to'
})

export const ThresholdMetricList: KeyValueStringType = Object.freeze({
  [ThresholdMetricNames.NetworkInboundUtilization]: 'Network Inbound Utilization',
  [ThresholdMetricNames.NetworkOutboundUtilization]: 'Network Outbound Utilization',
  [ThresholdMetricNames.CPU_USAGE]: 'Cpu Utilization'
})

export const InventoryComparator: KeyValueStringType = Object.freeze({
  [InventoryFilter.TAGS]: 'tags',
  [InventoryFilter.LABELS]: 'labels'
})

