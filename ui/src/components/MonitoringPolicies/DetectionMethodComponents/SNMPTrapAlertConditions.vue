<template>
    <div class="alert-conditions">
        <div>
            <h2>Specify SNMP Trap Alert Conditions</h2>
            <p>Please select a vendor to help filter to the relevant SNMP trap.</p>
            <p>These conditions help to determine what alerts will be triggered and their assigned severity.</p>
        </div>

        <div>
            <div class="subtitle">Select a Vendor</div>
            <FeatherAutocomplete
              class="my-autocomplete height-fix"
              label="Users"
              v-model="selectedVendor"
              :loading="loading"
              :results="vendorSearchResults"
              type="single"
              @update:model-value="setSelectedVendor"
              @search="search"
            ></FeatherAutocomplete>
        </div>
        <div>
            <div class="subtitle">Select SNMP Trap & Assign Conditions</div>
            <div class="row">
                <div class="col event-trigger">
                    <FeatherSelect
                        label="Trigger Event"
                        :options="monitoringPoliciesStore.eventDefinitions"
                        text-prop="name"
                        :disabled="monitoringPoliciesStore.selectedPolicy?.isDefault"
                        v-model="condition.triggerEvent"
                        @update:model-value="monitoringPoliciesStore.updateCondition(condition.id, condition)"
                    ><template #pre> <FeatherIcon :icon="icons.Search" /> </template
                    ></FeatherSelect>
                </div>
                <div class="col event-clear">
                    <FeatherSelect
                        label="Clear Event (Optional)"
                        :options="monitoringPoliciesStore.eventDefinitions"
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
import { useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'
import { AlertCondition, Severity } from '@/types/graphql'
import { IAutocompleteItemType } from '@featherds/autocomplete'
import Search from '@featherds/icon/action/Search'
import { ISelectItemType } from '@featherds/select'

const eventDefinitionOptions = ref([] as ISelectItemType[])
const monitoringPoliciesStore = useMonitoringPoliciesStore()
const condition = ref({} as AlertCondition)
const loading = ref(false)
const selectedVendor = ref(undefined as unknown as IAutocompleteItemType)
const vendorSearchResults = ref([] as IAutocompleteItemType[])

const severityOptions = [
  { id: Severity.Critical, name: 'Critical' },
  { id: Severity.Major, name: 'Major' },
  { id: Severity.Minor, name: 'Minor' },
  { id: Severity.Warning, name: 'Warning' },
  { id: Severity.Cleared, name: 'Cleared' }
]

onMounted(async () => {
  if (monitoringPoliciesStore.selectedRule?.vendor && monitoringPoliciesStore.selectedRule?.eventType) {
    const vendor = monitoringPoliciesStore.formattedVendors?.find((x) => x.toLowerCase().indexOf(monitoringPoliciesStore.selectedRule?.vendor?.toLowerCase() ?? 'generic') > -1)
    selectedVendor.value = { _text: vendor }
    await monitoringPoliciesStore.listAlertEventDefinitionsByVendor()
  }
  if (monitoringPoliciesStore.selectedRule?.alertConditions && monitoringPoliciesStore.selectedRule?.alertConditions[0]) {
    condition.value = monitoringPoliciesStore.selectedRule?.alertConditions[0]
  }
})

watch(() => [monitoringPoliciesStore.eventDefinitions], () => {
  eventDefinitionOptions.value = monitoringPoliciesStore.eventDefinitions || []
})

const search = (q: string) => {
  loading.value = true

  vendorSearchResults.value = monitoringPoliciesStore.formattedVendors?.filter((x) => x.toLowerCase().indexOf(q.toLowerCase()) > -1).map((x) => ({_text: x})) as IAutocompleteItemType[]

  loading.value = false
}

const setSelectedVendor = (v: any) => {
  if (monitoringPoliciesStore.selectedRule && v._text) {
    monitoringPoliciesStore.selectedRule.vendor = monitoringPoliciesStore.vendors?.find((x) => x.toLowerCase().indexOf(v._text.toLowerCase()) > -1)
    monitoringPoliciesStore.listAlertEventDefinitionsByVendor()
  }
}

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
      .event-trigger {
        :deep(.label-border){
          min-width: 74px !important;
        }
      }
      .event-clear {
        :deep(.label-border){
          min-width: 24% !important;
        }
      }
    }
  }
  .my-autocomplete  {
      :deep(.feather-autocomplete-content) {
        textarea {
          height: 26px !important;
        }
      }
  }
  .subtitle {
    @include typography.subtitle1;
    margin-bottom: 10px;
  }
</style>
