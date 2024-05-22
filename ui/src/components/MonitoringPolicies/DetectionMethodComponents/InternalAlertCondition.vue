<template>
    <div class="alert-conditions">
        <div>
            <h2>Specify Internal Event Alert Conditions</h2>
            <p>Select a Trigger and Clear event for the alert.</p>
            <p>Clear events will be limited based on the trigger event.</p>
        </div>
        <div>
            <div class="subtitle">Select Trigger and Clear Events</div>
            <div class="row">
                <div class="col event-trigger">
                <FeatherSelect
                    label="Trigger Event"
                    :options="eventDefinitionOptions"
                    text-prop="name"
                    :disabled="monitoringPoliciesStore.selectedPolicy?.isDefault"
                    v-model="condition.triggerEvent"
                    @update:model-value="(e) => onUpdateTriggerEvent(e as unknown as AlertEventDefinition)"
                    class="trigger-event-input"
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
                class="alert-message-input"
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
import Search from '@featherds/icon/action/Search'
import { ISelectItemType } from '@featherds/select'

const eventDefinitionOptions = ref([] as ISelectItemType[])
const monitoringPoliciesStore = useMonitoringPoliciesStore()
const condition = ref({} as AlertCondition)
const clearEvent = ref()

const severityOptions = [
  { id: Severity.Critical, name: 'Critical' },
  { id: Severity.Major, name: 'Major' },
  { id: Severity.Minor, name: 'Minor' },
  { id: Severity.Warning, name: 'Warning' },
  { id: Severity.Cleared, name: 'Cleared' }
]

const onUpdateTriggerEvent = (e: AlertEventDefinition) => {
  setClearEvent(e)
  monitoringPoliciesStore.updateCondition(condition.value.id, condition.value)
}

onMounted(() => {
  if (monitoringPoliciesStore.selectedRule && monitoringPoliciesStore.selectedRule.alertConditions) {
    eventDefinitionOptions.value = monitoringPoliciesStore.cachedEventDefinitions?.get('internal') ?? []
    condition.value = monitoringPoliciesStore.selectedRule?.alertConditions[0]
    if (monitoringPoliciesStore.selectedRule.alertConditions[0].triggerEvent) {
      setClearEvent(monitoringPoliciesStore.selectedRule.alertConditions[0].triggerEvent)
    }
  }
})

const setClearEvent = (event: AlertEventDefinition) => {
  if (event.clearKey) {
    clearEvent.value = monitoringPoliciesStore.getClearEventName(event.clearKey)
  } else {
    clearEvent.value = ''
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
  }
}

.subtitle {
  @include typography.subtitle1;
  margin-bottom: 10px;
}

.trigger-event-input {
  :deep(.label-border) {
    width: 80px !important;
  }
}

.alert-message-input {
  :deep(.label-border) {
    width: 60px !important;
  }
}

.margin-fix {
  margin-bottom: 10px;
}
</style>
