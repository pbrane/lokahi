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
        class="my-autocomplete"
        label=""
        hide-label
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
          <h4>Clear Event (Optional):</h4>
          <p>{{ clearEvent }}</p>
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
        class="input-name"
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
import { AlertCondition, AlertEventDefinition, Severity } from '@/types/graphql'
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
  if (monitoringPoliciesStore.selectedRule?.vendor && monitoringPoliciesStore.selectedRule?.eventType) {
    const vendor = monitoringPoliciesStore.formattedVendors?.find((x) => x.toLowerCase().indexOf(monitoringPoliciesStore.selectedRule?.vendor?.toLowerCase() ?? 'generic') > -1)
    selectedVendor.value = { _text: vendor }
    await monitoringPoliciesStore.listAlertEventDefinitionsByVendor()
  }
  if (monitoringPoliciesStore.selectedRule?.alertConditions && monitoringPoliciesStore.selectedRule?.alertConditions[0]) {
    condition.value = monitoringPoliciesStore.selectedRule?.alertConditions[0]
    if (monitoringPoliciesStore.selectedRule?.alertConditions[0].triggerEvent?.clearKey) {
      clearEvent.value = monitoringPoliciesStore.getClearEventName(monitoringPoliciesStore.selectedRule?.alertConditions[0].triggerEvent.clearKey)
    } else {
      clearEvent.value = ''
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

const setSelectedVendor = (v: any) => {
  if (monitoringPoliciesStore.selectedRule && v._text) {
    monitoringPoliciesStore.selectedRule.vendor = monitoringPoliciesStore.vendors?.find((x) => x.toLowerCase().indexOf(v._text.toLowerCase()) > -1)
    monitoringPoliciesStore.listAlertEventDefinitionsByVendor()
  }
}

const onUpdateTriggerEvent = (e: AlertEventDefinition) => {
  if (e.clearKey) {
    clearEvent.value = monitoringPoliciesStore.getClearEventName(e.clearKey)
  } else {
    clearEvent.value = ''
  }
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

    .event-clear {
      display: flex;
      align-items: center;
      gap: 5px;
    }
  }
}

.my-autocomplete {
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

.margin-fix {
  margin-bottom: 10px;
}

.input-name {
  :deep(.label-border) {
    width: 50px !important;
  }
}
</style>
