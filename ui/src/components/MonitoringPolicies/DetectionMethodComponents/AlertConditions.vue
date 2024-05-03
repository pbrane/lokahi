<template>
     <div class="container">
      <p class="subtitle">Set Alert Conditions</p>
      <table class="data-table" aria-label="Events Table" data-test="data-table">
        <thead>
          <tr>
            <th>Enable</th>
            <th>Alert Severity</th>
            <th>Condition</th>
            <th>Threshold</th>
          </tr>
        </thead>
        <TransitionGroup name="data-table" tag="tbody">
          <tr data-test="data-item" v-for="alert of alertConditions" :key="alert.id">
            <td class="toggle-wrapper">
              <FeatherListSwitch class="basic-switch" v-model="alert.isEnabled" />
            </td>
            <td>
              <PillColor :item="showSeverity(alert.alertSeverity)" :isIcon="isIcon">
                <template #actions>
                  <FeatherIcon :icon="alert?.icon" :class="alert.alertSeverity"/>
                </template>
               </PillColor>
            </td>
            <td>
              <div class="alert-conditions">
                <BasicSelect
                  :list="alert?.conditionsOptions"
                  @item-selected="selectCondition"
                  selectedId="1"
                  :disabled="monitoringPoliciesStore.selectedPolicy?.isDefault"
                />
              </div>
            </td>
            <td>
              <div class="threshold-metric-input">
                <FeatherInput
                  v-model.trim="threshold"
                  :label="alert.thresholdValue"
                  v-focus
                  data-test="rule-name-input"
                  :readonly="monitoringPoliciesStore.selectedPolicy?.isDefault"
                   />
                   <div class="subtitle">%</div>
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

const isIcon = ref<boolean>(true)
const threshold = ref('')
const monitoringPoliciesStore = useMonitoringPoliciesStore()

const conditionsOptions = [
  { id: 1, name: '> greater than' },
  { id: 2, name: '> greater than' }
]
const icons = markRaw({
  WifiNoConnection,
  Warning,
  Error
})
const alertConditions = [
  {id: 1, isEnabled: false, alertSeverity: 'Critical', condition: '', threshold: '', thresholdValue: '90', conditionsOptions, icon: icons.Error},
  {id: 2, isEnabled: false, alertSeverity: 'Major', condition: '', threshold: '', thresholdValue: '1', conditionsOptions, icon: icons.WifiNoConnection},
  {id: 3, isEnabled: false, alertSeverity: 'Minor', condition: '', threshold: '', thresholdValue: '1', conditionsOptions, icon: icons.WifiNoConnection},
  {id: 4, isEnabled: false, alertSeverity: 'Warning', condition: '', threshold: '', thresholdValue: '1', conditionsOptions, icon: icons.Warning}
]

const showSeverity = (value: any) => {
  return { style: value as string }
}

const selectCondition = (conditionType: any) => {
  console.log('select condition')
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
    tr{
      height: auto !important;
      td {
        .Major {
            rotate: 180deg;
          }
        .alert-conditions,.threshold-metric-input {
          margin-top: 0.8em;
        }
         .threshold-metric-input{
          display: flex;
          justify-content: flex-start;
          align-items: center;
          column-gap: 0.75em;
          .subtitle {
            align-self: baseline;
            margin-top: 0.70rem;
            color: var(--feather-cleared);
          }
          }
          .threshold-metric-input:last-child {
            :deep(.label-border) {
               width: 14.19991px !important;
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
