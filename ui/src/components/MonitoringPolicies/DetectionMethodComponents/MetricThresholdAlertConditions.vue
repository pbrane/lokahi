<template>
  <div class="alert-conditions">
    <div>
      <h2>Specify Threshold Alert Conditions</h2>
      <p>Alert conditions are an additional set of parameters specific to the chosen detection method.</p>
      <p>These conditions help to determine what alerts will be triggered and their assigned severity.</p>
    </div>
  <div class="row">
    <div class="col">
      <h5>Select a Threshold Metric</h5>
      <div class="select-metric">
        <BasicSelect
          label="Select Metric"
          :list="thresholdMetricTypeOptions"
          :selectedId="selectedId"
          @item-selected="handleMetricChange"
          :disabled="monitoringPoliciesStore.selectedPolicy?.isDefault"
        />
      </div>
  </div>
    <div class="col">
      <h5 class="subtitle">Select Trigger and Clear Events</h5>
      <div class="event-trigger">
        <FeatherSelect
          label="Trigger Event"
          :options="eventDefinitionOptions"
          text-prop="name"
          :disabled="monitoringPoliciesStore.selectedPolicy?.isDefault"
          v-model="eventTrigger"
          @update:model-value="(e: any) => onUpdateTriggerEvent(e as unknown as AlertEventDefinition)"
          class="trigger-event-input"
        >
          <template #pre>
            <FeatherIcon :icon="Search" />
          </template>
        </FeatherSelect>
      </div>
    </div>
  </div>
  <span class="subtitle" v-if="isShow">Time period</span>
  <div class="time-period" v-if="isShow">
    <p class="subtitle-last-any">Any</p>
      <div class="any-time">
        <BasicSelect
          :list="timePeriodOptions"
          @item-selected="selectEventType"
          selectedId="1"
          :disabled="monitoringPoliciesStore.selectedPolicy?.isDefault"
        />
      </div>
      <p class="subtitle-last-time">during the last</p>
      <div class="last-time">
        <BasicSelect
          :list="timePeriodOptions"
          @item-selected="selectEventType"
          selectedId="1"
          :disabled="monitoringPoliciesStore.selectedPolicy?.isDefault"
        />
      </div>
    </div>
    <AlertConditions/>
    <div class="notify" v-if="isShow">
      <FeatherSelect
        label="Notify As"
        :options="notifyOptions"
        text-prop="name"
        :disabled="monitoringPoliciesStore.selectedPolicy?.isDefault"
        @update:model-value.stop=""
      />
    </div>
  </div>
</template>

<script setup lang="ts">

import { useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'
import { AlertEventDefinition, EventType } from '@/types/graphql'
import { ThresholdMetricList } from '../monitoringPolicies.constants'
import { CreateEditMode, ThresholdMetricNames } from '../../../types/index'
import Search from '@featherds/icon/action/Search'
import { ISelectItemType } from '@featherds/select'

const eventDefinitionOptions = ref([] as ISelectItemType[])
const eventTrigger = ref()
const monitoringPoliciesStore = useMonitoringPoliciesStore()
const isShow = ref(false)
const selectedId = ref()

const timePeriodOptions = [
  { id: 1, first: '3 minutes', last: '10 minutes'}
]
const notifyOptions = [
  { id: 1, name: 'Critical' },
  { id: 2, name: 'Major' }
]
const thresholdMetricTypeOptions = [
  { id: 1, value: ThresholdMetricNames.NetworkInboundUtilization, name: ThresholdMetricList[ThresholdMetricNames.NetworkInboundUtilization] },
  { id: 2, value: ThresholdMetricNames.NetworkOutboundUtilization, name: ThresholdMetricList[ThresholdMetricNames.NetworkOutboundUtilization] },
  { id: 3, value: ThresholdMetricNames.CPU_USAGE, name: ThresholdMetricList[ThresholdMetricNames.CPU_USAGE] }
]

const selectThresholdName = () => {
  if (monitoringPoliciesStore.selectedRule?.thresholdMetricName) {
    const selected = thresholdMetricTypeOptions?.find((item) => item.value === monitoringPoliciesStore.selectedRule?.thresholdMetricName)
    selectedId.value = selected?.id
  } else {
    if (monitoringPoliciesStore.selectedRule) {
      monitoringPoliciesStore.selectedRule.thresholdMetricName =  ThresholdMetricNames.NetworkInboundUtilization
      selectedId.value = 1
    }
  }
}

const getEventTriggerThresholdMetric = () => {
  const { cachedEventDefinitions } = monitoringPoliciesStore
  const metricThresholdDefinitions = cachedEventDefinitions?.get('metricThreshold') ?? []
  eventDefinitionOptions.value = metricThresholdDefinitions
  if (metricThresholdDefinitions.length > 0) {
    eventTrigger.value = metricThresholdDefinitions[0]
    monitoringPoliciesStore.eventTriggerThresholdMetrics = metricThresholdDefinitions[0]
  }
}

const selectEvenetTriggerThresholdMetrics = () => {
  const { selectedRule, cachedEventDefinitions} = monitoringPoliciesStore
  if ( selectedRule?.alertConditions && selectedRule?.alertConditions?.length > 0) {
    const firstAlertCondition = selectedRule.alertConditions[0]
    eventTrigger.value = firstAlertCondition?.triggerEvent
    monitoringPoliciesStore.eventTriggerThresholdMetrics = firstAlertCondition?.triggerEvent as AlertEventDefinition
    eventDefinitionOptions.value =  cachedEventDefinitions?.get('metricThreshold') ?? []
  }
}
onMounted(() => {
  if (monitoringPoliciesStore.ruleEditMode === CreateEditMode.Create) {
    getEventTriggerThresholdMetric()
  } else {
    selectEvenetTriggerThresholdMetrics()
  }
  selectThresholdName()
})

const selectEventType = (eventType: EventType) => {
  // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
  monitoringPoliciesStore.selectedRule!.eventType = eventType
}

const handleMetricChange = (id: number) => {
  if (monitoringPoliciesStore?.selectedRule) {
    const thresholdMetric = thresholdMetricTypeOptions.find(value => value.id === id)
    if (thresholdMetric) {
      monitoringPoliciesStore.selectedRule.thresholdMetricName = thresholdMetric.value
      selectedId.value = id
    }
  }
}

const onUpdateTriggerEvent = (e: any) => {
  monitoringPoliciesStore.updateCondition(e.id, e)
  eventTrigger.value = e
}
</script>

<style lang="scss" scoped>
.alert-conditions {
  .notify {
    margin-top: 1em;
    width: 25%;
  }

  .time-period {
    width: 100%;
    display: flex;
    justify-content: flex-start;
    align-items: center;
    margin-top: 10px;
    column-gap: 10px;

    .any-time, .last-time {
      width: 25%;
    }
    .subtitle-last-time, .subtitle-last-any {
      align-self: baseline;
      color: var(--feather-cleared);
      margin: 10px 0px;
    }
  }

  .row {
    width: 100%;
    display: flex;
    justify-content: space-around;
    align-items: center;
    gap:4%;
    .col {
      width: 50%;
    }
    h5 {
      color: var(--feather-cleared);
    }
    .select-metric, .event-trigger {
      margin-top: 10px;
    }
  }
}
</style>
