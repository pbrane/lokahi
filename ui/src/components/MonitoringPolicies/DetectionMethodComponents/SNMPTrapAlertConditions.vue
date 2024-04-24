<template>
    <div class="alert-conditions">
        <div>
            <h2>Specify SNMP Trap Alert Conditions</h2>
            <p>Please select a vendor to help filter to the relevant SNMP trap.</p>
            <p>These conditions help to determine what alerts will be triggered and their assigned severity.</p>
        </div>

        <div>
            <div class="subtitle">Select a Vendor</div>
            <FeatherInput
            label=""
            hide-label
            v-focus
            required
            class="input-name"
            data-test="input-name"
            :disabled="monitoringPoliciesStore.selectedPolicy?.isDefault"
            >
            <template #pre> <FeatherIcon :icon="icons.Search" /> </template
          ></FeatherInput>
        </div>
        <div>
            <div class="subtitle">Select SNMP Trap & Assign Conditions</div>
            <div class="row">
                <div class="col">
                    <FeatherSelect
                        label="Trigger Event"
                        :options="triggerEventDefinitionOptions"
                        text-prop="name"
                        :disabled="monitoringPoliciesStore.selectedPolicy?.isDefault"
                        v-model="condition.triggerEvent"
                        @update:model-value="monitoringPoliciesStore.updateCondition(condition.id, condition)"
                    ><template #pre> <FeatherIcon :icon="icons.Search" /> </template
                    ></FeatherSelect>
                </div>
                <div class="col">
                    <FeatherSelect
                        label="Clear Event (Optional)"
                        :options="clearEventDefinitionOptions"
                        text-prop="name"
                        clear="Remove"
                        :disabled="monitoringPoliciesStore.selectedPolicy?.isDefault"
                        v-model="condition.clearEvent"
                        @update:model-value="monitoringPoliciesStore.updateCondition(condition.id, condition)"
                        >
                        <template #pre> <FeatherIcon :icon="icons.Search" /> </template
                    ></FeatherSelect>
                </div>
            </div>
        </div>

        <div>
            <div class="subtitle">Alert Message</div>
            <p>Based on vendor selection, additional variables may be used to craft a customized alert message</p>
            <FeatherInput
            label="Message"
            v-focus
            required
            class="input-name"
            data-test="input-name"
            :disabled="monitoringPoliciesStore.selectedPolicy?.isDefault"
            ></FeatherInput>
        </div>

        <div>
            <div class="subtitle">Alert Severity</div>
            <BasicSelect
                :list="severityOptions"
                :selectedId="condition.severity"
                :disabled="monitoringPoliciesStore.selectedPolicy?.isDefault"
                @item-selected="(val: string) => {
                    condition.severity = val
                    monitoringPoliciesStore.updateCondition(condition.id, condition)
                }"
            />
        </div>
    </div>
</template>

<script setup lang="ts">
import { useAlertEventDefinitionQueries } from '@/store/Queries/alertEventDefinitionQueries'
import { useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'
import { AlertCondition, EventType, Severity } from '@/types/graphql'
import Search from '@featherds/icon/action/Search'
import { ISelectItemType } from '@featherds/select'

const clearEventDefinitionOptions = ref([] as ISelectItemType[])
const triggerEventDefinitionOptions = ref([] as ISelectItemType[])
const alertEventDefinitionStore = useAlertEventDefinitionQueries()
const monitoringPoliciesStore = useMonitoringPoliciesStore()
const condition = ref({} as AlertCondition)

const severityOptions = [
  { id: Severity.Critical, name: 'Critical' },
  { id: Severity.Major, name: 'Major' },
  { id: Severity.Minor, name: 'Minor' },
  { id: Severity.Warning, name: 'Warning' },
  { id: Severity.Cleared, name: 'Cleared' }
]

onMounted(async () => {
  const result = await alertEventDefinitionStore.listAlertEventDefinitions(monitoringPoliciesStore.selectedRule?.eventType as EventType)
  clearEventDefinitionOptions.value = result.value?.listAlertEventDefinitions || []
  triggerEventDefinitionOptions.value = result.value?.listAlertEventDefinitions || []

  if (monitoringPoliciesStore.selectedRule?.alertConditions && monitoringPoliciesStore.selectedRule?.alertConditions[0]) {
    condition.value = monitoringPoliciesStore.selectedRule?.alertConditions[0]
  }
})

const icons = markRaw({
  Search
})
</script>

<style scoped lang="scss">
@use '@featherds/styles/themes/variables';
@use '@/styles/mediaQueriesMixins';
@use '@featherds/styles/mixins/typography';

  .row {
    @include mediaQueriesMixins.screen-lg {
      display: flex;
      gap: var(variables.$spacing-xl);
      .col {
        flex: 1;
      }
    }
  }

  .subtitle {
    @include typography.subtitle1;
    margin-bottom: 10px;
  }
</style>
