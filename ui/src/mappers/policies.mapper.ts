import { Comparators } from '@/types/index'
import {
  AlertCondition,
  AlertConditionInput,
  EventType,
  MonitorPolicy,
  MonitorPolicyInput,
  PolicyRule,
  PolicyRuleInput
} from '@/types/graphql'
import {
  MonitoringPolicy,
  MonitoringPolicyRule,
  PolicyAlertCondition
} from '@/types/policies'

// Mappings between GraphQL and Front End data types for Monitoring Policies related data
// FromServer: from GraphQL datatype to Front End data type
// ToServer: from Front End datatype to GraphQL datatype

export const mapPolicyAlertConditionFromServer = (condition: AlertCondition): PolicyAlertCondition => {
  return {
    id: condition.id || 0,
    alertMessage: condition.alertMessage,
    alertRegex: condition.alertRegex,
    clearEvent: condition.clearEvent,
    comparator: Comparators.EQ,
    count: condition.count,
    isNew: false,
    overtime: condition.overtime,
    overtimeUnit: condition.overtimeUnit,
    percentage: 0.0,
    severity: condition.severity,
    triggerEvent: condition.triggerEvent,
    unitLabel: '',
    thresholdMetric: condition.thresholdMetric
  } as PolicyAlertCondition
}

export const mapPolicyAlertConditionToServer = (condition: PolicyAlertCondition): AlertCondition => {
  return {
    id: condition.id || undefined,
    alertMessage: condition.alertMessage,
    alertRegex: condition.alertRegex,
    clearEvent: condition.clearEvent,
    count: condition.count,
    overtime: condition.overtime,
    overtimeUnit: condition.overtimeUnit,
    severity: condition.severity,
    triggerEvent: condition.triggerEvent,
    thresholdMetric: condition.thresholdMetric
  } as AlertCondition
}

export const mapMonitoringPolicyRuleFromServer = (rule: PolicyRule): MonitoringPolicyRule => {
  return {
    alertConditions: rule.alertConditions?.map(mapPolicyAlertConditionFromServer),
    detectionMethod: rule.detectionMethod,
    eventType: rule.eventType,
    id: rule.id,
    name: rule.name,
    thresholdMetricName: rule.thresholdMetricName,
    vendor: '',
    isNew: false
  } as MonitoringPolicyRule
}

export const mapMonitoringPolicyRuleToServer = (rule: MonitoringPolicyRule): PolicyRule => {
  return {
    alertConditions: rule.alertConditions?.map(mapPolicyAlertConditionToServer),
    detectionMethod: rule.detectionMethod,
    eventType: rule.eventType,
    id: rule.id,
    name: rule.name,
    thresholdMetricName: rule.thresholdMetricName
  } as PolicyRule
}

export const mapMonitoringPolicyFromServer = (policy: MonitorPolicy): MonitoringPolicy => {
  return {
    id: policy.id,
    memo: policy.memo || '',
    name: policy.name || '',
    notifyByEmail: policy.notifyByEmail || false,
    notifyByPagerDuty: policy.notifyByPagerDuty || false,
    notifyByWebhooks: policy.notifyByWebhooks || false,
    notifyInstruction: policy.notifyInstruction || '',
    rules: policy.rules?.map(mapMonitoringPolicyRuleFromServer),
    tags: policy.tags,
    enabled: policy.enabled || false,
    isDefault: policy.isDefault || false
  } as MonitoringPolicy
}

export const mapMonitoringPolicyToServer = (policy: MonitoringPolicy): MonitorPolicy => {
  return {
    id: policy.id,
    memo: policy.memo,
    name: policy.name,
    notifyByEmail: policy.notifyByEmail,
    notifyByPagerDuty: policy.notifyByPagerDuty,
    notifyByWebhooks: policy.notifyByWebhooks,
    notifyInstruction: policy.notifyInstruction,
    rules: policy.rules?.map(mapMonitoringPolicyRuleToServer),
    tags: policy.tags,
    enabled: policy.enabled,
    isDefault: policy.isDefault
  } as MonitorPolicy
}

export const mapToAlertConditionInput = (condition: PolicyAlertCondition, eventType: EventType): AlertConditionInput => {
  // don't send: clearEvent, alertRegex

  let alertConditionInput: AlertConditionInput = {
    clearEvent: condition.clearEvent,
    count: condition.count,
    overtime: condition.overtime,
    overtimeUnit: condition.overtimeUnit,
    severity: condition.severity,
    triggerEvent: condition.triggerEvent,
    thresholdMetric: condition.thresholdMetric
  } as AlertConditionInput

  if (eventType === EventType.Internal || EventType.SnmpTrap) {
    alertConditionInput = {
      ...alertConditionInput,
      alertMessage: condition.alertMessage
    }
  }

  if (!condition.isNew && condition.id) {
    alertConditionInput = {
      ...alertConditionInput,
      id: condition.id
    }
  }

  if (eventType === EventType.MetricThreshold) {
    delete alertConditionInput.thresholdMetric?.severity
  }

  return alertConditionInput
}

export const mapToPolicyRuleInput = (rule: MonitoringPolicyRule): PolicyRuleInput => {
  // don't send: vendor

  let policyRuleInput = {
    alertConditions: rule.alertConditions?.map((condition) => mapToAlertConditionInput(condition, rule.eventType)),
    detectionMethod: rule.detectionMethod,
    eventType: rule.eventType,
    name: rule.name,
    thresholdMetricName: rule.thresholdMetricName
  } as PolicyRuleInput

  if (!rule.isNew && rule.id) {
    policyRuleInput = {
      ...policyRuleInput,
      id: rule.id
    }
  }

  return policyRuleInput
}

export const mapToMonitorPolicyInput = (policy: MonitoringPolicy): MonitorPolicyInput => {
  // don't send: isDefault

  let monitorPolicyInput = {
    memo: policy.memo,
    name: policy.name,
    notifyByEmail: policy.notifyByEmail,
    notifyByPagerDuty: policy.notifyByPagerDuty,
    notifyByWebhooks: policy.notifyByWebhooks,
    notifyInstruction: policy.notifyInstruction,
    rules: policy.rules?.map(mapToPolicyRuleInput),
    tags: policy.tags,
    enabled: policy.enabled
  } as MonitorPolicyInput

  if (policy.id > 0) {
    monitorPolicyInput = {
      ...monitorPolicyInput,
      id: policy.id
    }
  }

  return monitorPolicyInput
}
