<template>
    <div class="alert-conditions">
        <div>
            <h2>Specify Syslog Alert Conditions</h2>
            <p>Create your Regular Expression (regEx) to find relevant syslog data.</p>
            <p>Then create a customized alert message.</p>
        </div>
    </div>

    <div>
        <div class="subtitle">Syslog Matching</div>
        <p class="margin-fix">If you need help with Regex, we recommend (X)</p>
        <FeatherInput
            label="Regular Expression (RegEx)"
            clear="Clear Text"
            v-model.trim="condition.alertRegex"
            @update:model-value="monitoringPoliciesStore.updateCondition(condition.id, condition)"
            :disabled="monitoringPoliciesStore.selectedPolicy?.isDefault"
        ><template #pre>
            <FeatherIcon :icon="Search" />
          </template></FeatherInput>
    </div>

    <div>
        <div class="subtitle">Alert Message</div>
        <p class="margin-fix">You can use the following variables to craft your customized alert message</p>
        <div class="subtitle">Standard syslog variables (RFC5424):</div>
        <div class="variables margin-fix">
            <div>$application</div>
            <div>$process</div>
            <div>$messageID</div>
            <div>$message</div>
            <div>$priority</div>
        </div>
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
</template>

<script setup lang="ts">
import { useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'
import { Severity } from '@/types/graphql'
import { PolicyAlertCondition } from '@/types/policies'
import Search from '@featherds/icon/action/Search'

const monitoringPoliciesStore = useMonitoringPoliciesStore()
const condition = ref({} as PolicyAlertCondition)

const severityOptions = [
  { id: Severity.Critical, name: 'Critical' },
  { id: Severity.Major, name: 'Major' },
  { id: Severity.Minor, name: 'Minor' },
  { id: Severity.Warning, name: 'Warning' },
  { id: Severity.Cleared, name: 'Cleared' }
]

onMounted(() => {
  if (monitoringPoliciesStore.selectedRule?.alertConditions && monitoringPoliciesStore.selectedRule?.alertConditions[9]) {
    condition.value = monitoringPoliciesStore.selectedRule?.alertConditions[9]
  }
})

</script>

<style scoped lang="scss">
@use '@featherds/styles/themes/variables';
@use '@/styles/mediaQueriesMixins';
@use '@featherds/styles/mixins/typography';
@import "@featherds/styles/themes/variables";

.variables {
    display: flex;
    align-items: center;
    justify-content: space-around;
    background-color: var($shade-4);
    line-height: 3
}

.subtitle {
  @include typography.subtitle1;
  margin-bottom: 10px;
}

.margin-fix {
  margin-bottom: 10px;
}
</style>
