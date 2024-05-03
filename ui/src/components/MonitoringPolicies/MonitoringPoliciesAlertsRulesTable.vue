<template>
  <div class="monitoring-policies-table-wrapper">
    <TableCard class="card border">
      <div class="container">
        <table class="data-table" aria-label="Monitoring Policies Table">
          <thead>
            <tr class="header-row">
              <th v-for="col of columns" :key="col?.id">{{ col.label }}</th>
            </tr>
          </thead>
          <TransitionGroup name="data-table" tag="tbody">
            <tr class="policies-table-row" v-for="rule in monitoringPoliciesStore.selectedPolicy?.rules" :key="rule?.id">
              <td>Outage</td>
              <td>System Event / Device Unreachable</td>
              <td>Critical / Interval 5 / 5 MINUTES</td>
              <td class="actions-icons">
                <div @click.prevent="copyRule" class="icon">
                  <Icon :icon="copyIcon" />
                </div>
                <div @click.prevent="editRule(rule)" class="icon">
                  <Icon :icon="editIcon" />
                </div>
                <div @click.prevent="removeRule" class="icon">
                  <Icon :icon="removeIcon" />
                </div>
              </td>
            </tr>
          </TransitionGroup>
        </table>
      </div>
    </TableCard>
    <div class="create-alert-btn">
      <Icon :icon="addIcon" />
      <FeatherButton class="feather-button" @click="createRule">ALERT RULE</FeatherButton>
    </div>
  </div>
  <AlertRulesDrawer/>
</template>

<script setup lang="ts">
import { useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'
import { IIcon } from '@/types'
import { PolicyRule } from '@/types/graphql'
import Add from '@featherds/icon/action/Add'
import CopyIcon from '@featherds/icon/action/ContentCopy'
import Delete from '@featherds/icon/action/Delete'
import Edit from '@featherds/icon/action/Edit'

const monitoringPoliciesStore = useMonitoringPoliciesStore()
const removeIcon: IIcon = { image: markRaw(Delete), tooltip: 'Alert Rule Delete', size: 1.5, cursorHover: true }
const editIcon: IIcon = { image: markRaw(Edit), tooltip: 'Alert Rule Edit', size: 1.5, cursorHover: true }
const copyIcon: IIcon = { image: markRaw(CopyIcon), tooltip: 'Alert Rule Copy', size: 1.5, cursorHover: true }
const addIcon: IIcon = { image: markRaw(Add), tooltip: 'Alert Rule Add', size: 1.5, cursorHover: true }

const columns: { id: string; label: string }[] = [
  { id: 'AlertRule', label: 'Alert Rule' },
  { id: 'ConditionalLogic', label: 'Conditional Logic' },
  { id: 'Description', label: 'Description' },
  { id: 'Actions', label: 'Actions' }
]

const copyRule = () => console.log('copy alerts')
const createRule = () => {
  monitoringPoliciesStore.openAlertRuleDrawer(undefined)
}
const editRule = (rule: PolicyRule) => {
  monitoringPoliciesStore.openAlertRuleDrawer(rule)
}
const removeRule = () => console.log('remove alerts')

</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/_transitionDataTable';
@use '@/styles/vars.scss';

.monitoring-policies-table-wrapper {
  width: 100%;
  overflow-x: hidden;

  .card {
    padding: 0px !important;
  }

  .container {
    display: block;
    overflow-x: auto;
    box-shadow: 1px 1px 2px var(--feather-disabled-text-on-surface);

    table {
      width: 100%;
      @include table.table;

      thead {
        background: var(variables.$background);
        text-transform: uppercase;
      }

      .actions-icons {
        display: flex;
        justify-content: flex-start;
        align-items: center;
        gap: 5px;
        min-height: 52px;
        cursor: pointer;
        .icon {
          color: var(--feather-primary);
          :deep(:focus) {
            outline: none;
         }
        }
      }

      td {
        white-space: nowrap;
          .toggle-wrapper {
            display: flex;
            justify-content: flex-start;
            align-items: center;

            :deep(.feather-list-item) {
              padding: 0px !important;
            }
        }
      }
    }
  }

  .create-alert-btn {
    margin-top: 15px;
    display: flex;
    align-items: center;

    .feather-button {
      background-color: transparent;
      font-weight: var(variables.$font-semibold);
      color: var(--feather-primary);
      border: none;
      border-radius: 4px;
      font-size: 16px;
      cursor: pointer;
    }
  }
}

</style>
