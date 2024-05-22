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
        label=""
        class="vendor-autocomplete"
        v-model="selectedVendor"
        :loading="loading"
        :results="vendorSearchResults"
        type="single"
        @update:model-value="setSelectedVendor"
        :error="monitoringPoliciesStore.validationErrors.ruleVendor"
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
            @update:model-value="(e) => onUpdateTriggerEvent(e as unknown as AlertEventDefinition)"
          ><template #pre>
            <FeatherIcon :icon="icons.Search" />
          </template></FeatherSelect>
        </div>
        <div class="col event-clear">
          <FeatherInput
            label="Clear Event"
            v-model.trim="clearEvent"
            readonly
          ></FeatherInput>
        </div>
      </div>
    </div>

    <div>
      <div class="subtitle">Alert Message</div>
      <p class="margin-fix">Based on vendor selection, additional variables may be used to craft a customized alert
        message
      </p>
      <FeatherInput
        label="Message"
        v-model.trim="condition.alertMessage"
        @update:model-value="monitoringPoliciesStore.updateCondition(condition.id, condition)"
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
        }" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'
import { CreateEditMode } from '@/types'
import { AlertCondition, AlertEventDefinition, EventType, Severity } from '@/types/graphql'
import { IAutocompleteItemType } from '@featherds/autocomplete'
import Search from '@featherds/icon/action/Search'
import { ISelectItemType } from '@featherds/select'

const eventDefinitionOptions = ref([] as ISelectItemType[])
const monitoringPoliciesStore = useMonitoringPoliciesStore()
const condition = ref({} as AlertCondition)
const loading = ref(false)
const selectedVendor = ref(undefined as unknown as IAutocompleteItemType)
const vendorSearchResults = ref([] as IAutocompleteItemType[])
const clearEvent = ref()

const severityOptions = [
  { id: Severity.Critical, name: 'Critical' },
  { id: Severity.Major, name: 'Major' },
  { id: Severity.Minor, name: 'Minor' },
  { id: Severity.Warning, name: 'Warning' },
  { id: Severity.Cleared, name: 'Cleared' }
]

onMounted(async () => {
  if (monitoringPoliciesStore.selectedRule && monitoringPoliciesStore.selectedRule.alertConditions) {
    let extractedVendor = ''
    if (monitoringPoliciesStore.ruleEditMode === CreateEditMode.Create) {
      extractedVendor = monitoringPoliciesStore.selectedRule.vendor ?? ''
    }
    if (monitoringPoliciesStore.ruleEditMode === CreateEditMode.Edit) {
      extractedVendor = monitoringPoliciesStore.extractVendorFromEventUei(monitoringPoliciesStore.selectedRule.alertConditions[0].triggerEvent?.uei ?? '') ?? ''
    }
    const formattedVendor = monitoringPoliciesStore.formattedVendors?.find((x) => x.toLowerCase().indexOf(extractedVendor.toLowerCase()) > -1) ?? ''
    selectedVendor.value = { _text: formattedVendor }
    await monitoringPoliciesStore.listAlertEventDefinitionsByVendor(monitoringPoliciesStore.selectedRule.eventType as EventType, formattedVendor)
    condition.value = monitoringPoliciesStore.selectedRule.alertConditions[0]
    if (monitoringPoliciesStore.selectedRule.alertConditions[0].triggerEvent) {
      setClearEvent(monitoringPoliciesStore.selectedRule.alertConditions[0].triggerEvent)
    }
  }
})

watch(() => [monitoringPoliciesStore.eventDefinitions], () => {
  eventDefinitionOptions.value = monitoringPoliciesStore.eventDefinitions || []
})

const search = (q: string) => {
  loading.value = true

  vendorSearchResults.value = monitoringPoliciesStore.formattedVendors?.filter((x) => x.toLowerCase().indexOf(q.toLowerCase()) > -1).map((x) => ({ _text: x })) as IAutocompleteItemType[]

  loading.value = false
}

const setSelectedVendor = async (v: any) => {
  if (monitoringPoliciesStore.selectedRule && v._text) {
    await monitoringPoliciesStore.listAlertEventDefinitionsByVendor(monitoringPoliciesStore.selectedRule.eventType as EventType, v._text)
    if (monitoringPoliciesStore.eventDefinitions?.length) {
      setClearEvent(monitoringPoliciesStore.eventDefinitions[0])
    } else {
      clearEvent.value = ''
    }
  }
}

const setClearEvent = (event: AlertEventDefinition) => {
  if (event.clearKey) {
    clearEvent.value = monitoringPoliciesStore.getClearEventName(event.clearKey)
  } else {
    clearEvent.value = ''
  }
}

const onUpdateTriggerEvent = (e: AlertEventDefinition) => {
  setClearEvent(e)
  monitoringPoliciesStore.updateCondition(condition.value.id, condition.value)
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
      :deep(.label-border) {
        min-width: 74px !important;
      }
    }
  }
}

.vendor-autocomplete {
  :deep(.label-border) {
    width: 0px !important;
  }
}

.subtitle {
  @include typography.subtitle1;
  margin-bottom: 10px;
}

.margin-fix {
  margin-bottom: 10px;
}
</style>
