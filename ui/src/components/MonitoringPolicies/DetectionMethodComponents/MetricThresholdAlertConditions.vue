<template>
    <div class="alert-conditions">
        <div>
            <h2>Specify Threshold Alert Conditions</h2>
            <p>Alert conditions are an additional set of parameters specific to the chosen detection method.</p>
            <p>These conditions help to determine what alerts will be triggered and their assigned severity.</p>
        </div>
        <div class="component-type">
          <h2>Select a Component Type</h2>
          <FeatherRadioGroup
            v-model="radioModel"
            :label="''"
            class="text-radio"
            @update:modelValue="(e: any) => onChecked && onChecked(e)"
          >
          <FeatherRadio
            :value="node"
            :data-test="`text-radio-button-${node}`"
          >
          <p>Node</p>
         </FeatherRadio>
         <FeatherRadio
            :value="interFace"
            :data-test="`text-radio-button-${interFace}`"
          >
          <p>Interface</p>
         </FeatherRadio>
        </FeatherRadioGroup>
      </div>
      <div class="metric-type">
            <h4>Select a Threshold Metric</h4>
            <div class="select-metric">
              <FeatherSelect
                label="Select Metric"
                :options="thresholdMetricTypeOptions"
                text-prop="name"
                :disabled="monitoringPoliciesStore.selectedPolicy?.isDefault"
                @update:model-value=""
              />
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
import { EventType } from '@/types/graphql'

const monitoringPoliciesStore = useMonitoringPoliciesStore()
const node = ref<boolean>(false)
const interFace = ref<boolean>(true)
const radioModel = ref<boolean>()
const isShow = ref(false)

const timePeriodOptions = [
  { id: 1, first: '3 minutes', last: '10 minutes'}
]
const notifyOptions = [
  { id: 1, name: 'Critical' },
  { id: 2, name: 'Major' }
]

const thresholdMetricTypeOptions = [
  { id: 1, name: 'Bandwidth Utilization' }
]

const onChecked = (e: boolean) => {
  console.log('checked', e)
}

const selectEventType = (eventType: EventType) => {
  monitoringPoliciesStore.selectedRule!.eventType = eventType
}

</script>

<style lang="scss" scoped>

.alert-conditions {
  .component-type {
    padding-top: 1rem;
    :deep(.feather-radio-group) {
      display: flex;
      justify-content: center;
      align-items: flex-start;
      flex-direction: column;
      row-gap: 5px;
    }
    h2{
    color: var(--feather-cleared);
  }
 }

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
  .any-time , .last-time {
    width: 25%;
  }
  .subtitle-last-time , .subtitle-last-any {
    align-self: baseline;
    color: var(--feather-cleared);
    margin: 10px 0px;
  }
}

 .metric-type {
     h4{
        color: var(--feather-cleared);
    }
    .select-metric {
      width: 40%;
      margin-top: 10px;
    }
 }
}

</style>
