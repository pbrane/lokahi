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
              <td>{{ rule?.name }}</td>
              <template v-if="rule.alertConditions">
                <td>{{ `${rule.alertConditions[0]?.triggerEvent?.eventType ?? 'Unknown'} / ${rule.alertConditions[0]?.triggerEvent?.name ?? 'Unknown'}` }}</td>
                <td>{{ `${rule.alertConditions[0]?.severity ?? 'Unknown'}`}}</td>
              </template>
              <template v-else>
                <td>Unknown</td>
                <td>Unknown</td>
              </template>
              <td>
                <FeatherButton @click.prevent="copyRule(rule)" :disabled="disableActionRule" icon="copy">
                  <FeatherIcon :icon="Copy"> </FeatherIcon>
                </FeatherButton>
                <FeatherButton @click.prevent="editRule(rule)" icon="edit">
                  <FeatherIcon :icon="Edit"> </FeatherIcon>
                </FeatherButton>
                <FeatherButton @click.prevent="countAlertsAndOpenDeleteModal(rule)" :disabled="disableActionRule" icon="delete">
                  <FeatherIcon :icon="Delete"> </FeatherIcon>
                </FeatherButton>
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
          <FeatherIcon :icon="Add"> </FeatherIcon>
          ALERT RULE
        </template>
      </FeatherButton>
    </div>
  </div>
  <AlertRulesDrawer />
  <DeleteConfirmationModal
    :isVisible="isVisible"
    :customMsg="deleteMsg"
    :closeModal="() => closeDeleteModal()"
    :deleteHandler="() => monitoringPoliciesStore.removeRule()"
    :isDeleting="mutations.deleteRuleIsFetching"
  />
</template>

<script setup lang="ts">
import useModal from '@/composables/useModal'
import useSnackbar from '@/composables/useSnackbar'
import { useMonitoringPoliciesMutations } from '@/store/Mutations/monitoringPoliciesMutations'
import { useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'
import { CreateEditMode } from '@/types'
import { PolicyRule } from '@/types/graphql'
import Add from '@featherds/icon/action/Add'
import Copy from '@featherds/icon/action/ContentCopy'
import Delete from '@featherds/icon/action/Delete'
import Edit from '@featherds/icon/action/Edit'

const { openModal, closeModal, isVisible } = useModal()
const { showSnackbar } = useSnackbar()
const monitoringPoliciesStore = useMonitoringPoliciesStore()
const mutations = useMonitoringPoliciesMutations()
const disableAddAlertRule = computed(() => monitoringPoliciesStore.selectedPolicy?.isDefault)
const disableActionRule = computed(() => monitoringPoliciesStore.selectedPolicy?.isDefault)
const deleteMsg = ref()

const columns: { id: string; label: string }[] = [
  { id: 'NameOfRule', label: 'Name' },
  { id: 'EventType', label: 'Event Type' },
  { id: 'Description', label: 'Description' },
  { id: 'Actions', label: 'Actions' }
]

const copyRule = (rule: PolicyRule) => {
  monitoringPoliciesStore.copyRule(rule)
}
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
    deleteMsg.value = `Deleting rule ${monitoringPoliciesStore.selectedRule?.name} removes ${monitoringPoliciesStore.cachedAffectedAlertsByRule?.get(monitoringPoliciesStore.selectedRule.id) ?? 0} associated alerts. Do you wish to proceed?`
    openModal()
  } else {
    showSnackbar({
      msg: 'Policy must have 1 rule',
      error: true
    })
  }
}
const closeDeleteModal = () => {
  monitoringPoliciesStore.selectedRule = undefined
  closeModal()
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
