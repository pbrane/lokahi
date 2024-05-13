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
            <tr class="policies-table-row" v-for="rule in monitoringPoliciesStore.selectedPolicy?.rules"
              :key="rule?.id">
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
                <div @click.prevent="countAlertsAndOpenDeleteModal(rule)" class="icon">
                  <Icon :icon="removeIcon" />
                </div>
              </td>
            </tr>
          </TransitionGroup>
        </table>
      </div>
    </TableCard>
    <div>
      <FeatherButton
        text
        @click="createRule"
        :disabled="disableAddAlertRule"
      >
        <template v-slot:icon>
          <Icon :icon="addIcon" />
          ALERT RULE
        </template>
      </FeatherButton>
    </div>
  </div>
  <AlertRulesDrawer />
  <DeleteConfirmationModal 
    :isVisible="isVisible" 
    :customMsg="deleteMsg" 
    :closeModal="() => closeModal()"
    :deleteHandler="() => monitoringPoliciesStore.removeRule()" 
    :isDeleting="mutations.deleteRuleIsFetching" 
  />
</template>

<script setup lang="ts">
import useModal from '@/composables/useModal'
import useSnackbar from '@/composables/useSnackbar'
import { useMonitoringPoliciesMutations } from '@/store/Mutations/monitoringPoliciesMutations'
import { useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'
import { CreateEditMode, IIcon } from '@/types'
import { PolicyRule } from '@/types/graphql'
import Add from '@featherds/icon/action/Add'
import CopyIcon from '@featherds/icon/action/ContentCopy'
import Delete from '@featherds/icon/action/Delete'
import Edit from '@featherds/icon/action/Edit'

const { openModal, closeModal, isVisible } = useModal()
const { showSnackbar } = useSnackbar()
const monitoringPoliciesStore = useMonitoringPoliciesStore()
const mutations = useMonitoringPoliciesMutations()
const removeIcon: IIcon = { image: markRaw(Delete), tooltip: 'Alert Rule Delete', size: 1.5, cursorHover: true }
const editIcon: IIcon = { image: markRaw(Edit), tooltip: 'Alert Rule Edit', size: 1.5, cursorHover: true }
const copyIcon: IIcon = { image: markRaw(CopyIcon), tooltip: 'Alert Rule Copy', size: 1.5, cursorHover: true }
const addIcon: IIcon = { image: markRaw(Add), tooltip: 'Alert Rule Add', size: 1.5, cursorHover: true }
const disableAddAlertRule = computed(() => monitoringPoliciesStore.selectedPolicy?.isDefault)
const deleteMsg = computed(() =>
  `Deleting rule ${monitoringPoliciesStore.selectedRule?.name} removes ${monitoringPoliciesStore.numOfAlertsForRule} associated alerts. Do you wish to proceed?`
)

const columns: { id: string; label: string }[] = [
  { id: 'AlertRule', label: 'Alert Rule' },
  { id: 'ConditionalLogic', label: 'Conditional Logic' },
  { id: 'Description', label: 'Description' },
  { id: 'Actions', label: 'Actions' }
]

const copyRule = () => console.log('copy alerts')
const createRule = () => {
  monitoringPoliciesStore.setRuleEditMode(CreateEditMode.Create)
  monitoringPoliciesStore.openAlertRuleDrawer(undefined)
}
const editRule = (rule: PolicyRule) => {
  monitoringPoliciesStore.setRuleEditMode(CreateEditMode.Edit)
  monitoringPoliciesStore.openAlertRuleDrawer(rule)
}
const countAlertsAndOpenDeleteModal = async (rule: PolicyRule) => {
  if (monitoringPoliciesStore.selectedPolicy?.rules?.length && monitoringPoliciesStore.selectedPolicy?.rules?.length > 1) {
    monitoringPoliciesStore.selectedRule = rule
    await monitoringPoliciesStore.countAlertsForRule()
    openModal()
  } else {
    showSnackbar({
      msg: 'Policy must have 1 rule',
      error: true
    })
  }
}

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
}
</style>
