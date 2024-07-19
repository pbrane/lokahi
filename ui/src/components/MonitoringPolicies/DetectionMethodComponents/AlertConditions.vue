<template>
     <div class="container">
      <p class="subtitle">Set Alert Conditions</p>
      <table class="data-table" aria-label="Events Table">
        <thead>
          <tr>
            <th>Enable</th>
            <th>Name</th>
            <th>Alert Severity</th>
            <th>Condition</th>
            <th>Threshold</th>
            <th>Expression</th>
          </tr>
        </thead>
        <TransitionGroup name="data-table" tag="tbody">
          <tr v-for="(alert,index) in alertConditionsThresholdMetric"
            :key="alert.id">
            <td class="toggle-wrapper">
              <FeatherListSwitch
               class="basic-switch"
               v-model="alert.enabled"
               @update:modelValue="(e:any) => changeAlertConditionThresholdMetric(e,alert.id,'enabled')"
               :disabled="isDefault"  />
            </td>
            <td>
              <div class="conditions-name-input">
                <FeatherInput
                  type="text"
                  v-model="alert.name"
                  label=""
                  :readonly="isDefault"
                  />
              </div>
            </td>
            <td>
              <div class="alert-severity">
              <PillColor
               :item="showSeverity(alert.severity)"
               :isIcon="isIcon">
                <template #actions>
                  <FeatherIcon
                  :icon="alertSeverities[index]?.icon"
                  :class="alert.severity"/>
                </template>
               </PillColor>
               </div>
            </td>
            <td>
              <div class="alert-conditions">
                <BasicSelect
                  :list="conditionsOptions"
                  @item-selected="(e:any) => {
                    const filterCondition = conditionsOptions?.find(conditions => conditions.id === e)
                    changeAlertConditionThresholdMetric(filterCondition?.value,alert.id,'condition')}"
                  :selectedId="selectedAlertConditionsThresholdMetric[index]?.id || alert.condition"
                  :disabled="isDefault"
                  />
              </div>
            </td>
            <td>
              <div class="threshold-metric-input">
                <FeatherInput
                  type="number"
                  v-model.number="alert.threshold"
                  label=""
                  :readonly="isDefault"
                />
              </div>
            </td>
            <td>
            <div class="expression-input">
              <FeatherInput
                label=""
                type="text"
                v-model.number="alert.expression"
                :readonly="isDefault"
                />
              </div>
            </td>
          </tr>
        </TransitionGroup>
      </table>
    </div>
</template>
<script  setup lang="ts">
import { useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'
import WifiNoConnection from '@featherds/icon/notification/WifiNoConnection'
import Error from '@featherds/icon/notification/Error'
import Warning from '@featherds/icon/notification/Warning'
import { Severity, ThresholdMetric } from '@/types/graphql'
import PillColor from '@/components/Common/PillColor.vue'
import { Comparators, CreateEditMode } from '@/types'
import { ComparatorSigns, ComparatorText } from '../monitoringPolicies.constants'
import { cloneDeep } from 'lodash'

const isIcon = ref<boolean>(true)
const monitoringPoliciesStore = useMonitoringPoliciesStore()
const alertConditionsThresholdMetric = ref<ThresholdMetric[]>([])
const selectedAlertConditionsThresholdMetric = ref<{ id: any; value: string; name: string; }[]>([])


const getDummyAlertConditionThresholdMetrics = () => {
  const dummyAlertConditionThresholdMetrics = [
    { id: 1, condition: '>', severity: 'CRITICAL', enabled: false, threshold: 0, expression: '', name: '' },
    { id: 2, condition: '>', severity: 'MAJOR', enabled: false, threshold: 0, expression: '', name: '' },
    { id: 3, condition: '>', severity: 'MINOR', enabled: false, threshold: 0, expression: '', name: '' },
    { id: 4, condition: '>', severity: 'WARNING', enabled: false, threshold: 0, expression: '', name: '' }
  ]
  return cloneDeep(dummyAlertConditionThresholdMetrics)
}

const conditionsOptions = [
  { id: 1, value: ComparatorSigns[Comparators.GT], name: ComparatorText[Comparators.GT] },
  { id: 2, value: ComparatorSigns[Comparators.EQ], name: ComparatorText[Comparators.EQ] },
  { id: 3, value: ComparatorSigns[Comparators.LT], name: ComparatorText[Comparators.LT] },
  { id: 4, value: ComparatorSigns[Comparators.LTE], name: ComparatorText[Comparators.LTE] },
  { id: 5, value: ComparatorSigns[Comparators.GTE], name: ComparatorText[Comparators.GTE] }
]

const alertSeverities = reactive([
  { id: Severity.Critical, icon: Error },
  { id: Severity.Major, icon: WifiNoConnection },
  { id: Severity.Minor, icon: WifiNoConnection },
  { id: Severity.Warning, icon: Warning }
])

const filterAndPushConditions = (condition: string) => {
  const filterAlertConditions = conditionsOptions.find(option => option.value === condition)
  if (filterAlertConditions) {
    selectedAlertConditionsThresholdMetric.value.push(filterAlertConditions)
  }
}

onMounted(() => {
  if (monitoringPoliciesStore.selectedRule?.alertConditions && monitoringPoliciesStore.ruleEditMode === CreateEditMode.Create) {
    monitoringPoliciesStore.clearAlertConditions()
    alertConditionsThresholdMetric.value = getDummyAlertConditionThresholdMetrics()
  } else if (monitoringPoliciesStore.ruleEditMode === CreateEditMode.Edit) {
    if (monitoringPoliciesStore.selectedRule?.alertConditions && monitoringPoliciesStore.selectedRule?.alertConditions?.length > 0) {
      monitoringPoliciesStore.selectedRule?.alertConditions.forEach((alertCondition: any) => {
        if (alertCondition?.thresholdMetric) {
          filterAndPushConditions(alertCondition.thresholdMetric?.condition)
          const condition = {
            id: alertCondition.thresholdMetric?.id,
            enabled: alertCondition.thresholdMetric?.enabled,
            condition: alertCondition.thresholdMetric?.condition,
            threshold: alertCondition.thresholdMetric?.threshold,
            expression: alertCondition.thresholdMetric?.expression,
            name: alertCondition.thresholdMetric?.name,
            severity: alertCondition.severity
          }
          alertConditionsThresholdMetric.value.push(condition)
        }
      })
      if (!(alertConditionsThresholdMetric.value.length > 0)) {
        alertConditionsThresholdMetric.value = getDummyAlertConditionThresholdMetrics()
      }
    }
  }
})

watch([alertConditionsThresholdMetric, monitoringPoliciesStore.eventTriggerThresholdMetrics], () => {
  if (monitoringPoliciesStore?.selectedRule) {
    if (!monitoringPoliciesStore.selectedRule.alertConditions) {
      monitoringPoliciesStore.selectedRule.alertConditions = []
    }
    alertConditionsThresholdMetric.value?.forEach((alertCondition, index) => {
      if (monitoringPoliciesStore.selectedRule?.alertConditions) {
        monitoringPoliciesStore.selectedRule.alertConditions[index] = {
          ...monitoringPoliciesStore.selectedRule.alertConditions[index],
          triggerEvent: monitoringPoliciesStore.eventTriggerThresholdMetrics || null,
          severity: alertCondition.severity,
          thresholdMetric: alertCondition
        }
      }
    })
  }
}, { deep: true })

const isDefault = computed(() => {
  return monitoringPoliciesStore.selectedPolicy?.isDefault
})

const showSeverity = (value: any) => {
  return { style: value as string }
}

const changeAlertConditionThresholdMetric = (value: any, id: number, key: keyof ThresholdMetric) => {
  const index = alertConditionsThresholdMetric?.value.findIndex((condition: any) => condition.id === id)
  if (index === -1) {
    return
  }
  const conditionToUpdate = {...alertConditionsThresholdMetric.value[index]}
  conditionToUpdate[key] = value
  alertConditionsThresholdMetric.value[index] = conditionToUpdate
}
</script>
<style lang="scss" scoped>
@use '@featherds/styles/mixins/typography';
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/lib/grid';
@use '@featherds/table/scss/table';
@use '@/styles/vars.scss';
@use "@/styles/_transitionDataTable";
@use '@/styles/_severities.scss';

.container {
  display: block;
  overflow-x: hidden;

  .subtitle {
    margin-bottom: 0.75rem;
    @include typography.subtitle1;
    color: var(--feather-cleared);
  }

  table {
    width: 100%;
    @include table.table;
    padding: 0px !important;
    border: 1px solid var(variables.$border-on-surface);
    border-radius: 4px;

    thead {
      background: var(variables.$background);
      border: 1px solid var(variables.$border-on-surface);
    }

    tr {
      height: auto !important;

      td {
        .Major {
          rotate: 180deg;
        }
        .alert-conditions,
        .threshold-metric-input,
        .conditions-name-input,
        .expression-input {
          margin-top: 0.8em;
        }
        .expression-input,
        .conditions-name-input,
        .alert-severity {
          width: 100%;
        }
        .threshold-metric-input {
          width: 100%;
          .subtitle {
            align-self: baseline;
            margin-top: 0.70rem;
            color: var(--feather-cleared);
           }
          }
          .threshold-metric-input:last-child,
          .conditions-name-input:last-child,
          .expression-input:last-child {
            :deep(.label-border) {
               width:0px !important;
            }
        }
      }
    }
  .toggle-wrapper {
    :deep(li) {
      display: flex;
    }

    :deep(.feather-list-item) {
      padding: 0px !important;

      &:deep(hover) {
        background: none !important;
      }
    }
   }
  }
}
</style>
