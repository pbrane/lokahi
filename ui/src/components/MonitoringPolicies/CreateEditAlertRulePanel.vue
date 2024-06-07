<template>
  <div v-if="monitoringPoliciesStore.selectedRule && monitoringPoliciesStore.selectedPolicy" :class="['main-alert', { 'main': !title }]">
    <div :class="['header', { 'main-heading': !title }]">
      <h2>{{ title || 'Create New Alert Rule' }}</h2>
      <p> {{ subTitle || 'A rule is a condition or set of conditions that triggers an alert.' }}</p>
    </div>
    <div :class="['content', { 'scroll-bar': !scrollBar }]">
      <div class="basic-information">
        <h3>Basic Information</h3>
        <p>Create a clear and descriptive name for the rule to easily identify its purpose and functionality. Then
          select a event type.</p>
      </div>

      <div class="name">
        <FeatherInput
          v-model.trim="monitoringPoliciesStore.selectedRule.name"
          label="Name"
          data-test="rule-name-input"
          :error="monitoringPoliciesStore.validationErrors.ruleName"
          :readonly="monitoringPoliciesStore.selectedPolicy?.isDefault"
        />
      </div>

      <div class="event-type">
        <div class="subtitle">Select an Event Type</div>
        <BasicSelect
          :list="eventTypeOptions"
          @item-selected="selectEventType"
          :selectedId="monitoringPoliciesStore.selectedRule?.eventType"
          :disabled="monitoringPoliciesStore.selectedPolicy?.isDefault"
        />
      </div>
      <InternalAlertCondition v-if="monitoringPoliciesStore.selectedRule?.eventType?.match(EventType.Internal)" />
      <SNMPTrapAlertConditions v-if="monitoringPoliciesStore.selectedRule?.eventType?.match(EventType.SnmpTrap)" />
      <SyslogAlertConditions v-if="monitoringPoliciesStore.selectedRule?.eventType?.match(EventType.Syslog)" />
      <MetricThresholdAlertConditions v-if="monitoringPoliciesStore.selectedRule?.eventType?.match(EventType.MetricThreshold)" />
    </div>
    <div class="footer">
      <div v-if="!title">
        <FeatherButton
          secondary
          @click="monitoringPoliciesStore.closeAlertRuleDrawer"
        >
          Cancel
        </FeatherButton>
        <ButtonWithSpinner
          primary
          @click="monitoringPoliciesStore.saveRule"
          :disabled="disableSaveRuleBtn"
        >
          Save
        </ButtonWithSpinner>
      </div>
      <FeatherButton
        v-else
        text
        @click="monitoringPoliciesStore.saveRule"
        :disabled="disableSaveRuleBtn"
      >
        SAVE ALERT RULE
      </FeatherButton>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'
import { EventType } from '@/types/graphql'

defineProps({
  title: String,
  subTitle: String,
  scrollBar: Boolean
})

const monitoringPoliciesStore = useMonitoringPoliciesStore()

const eventTypeOptions = [
  { id: EventType.Internal, name: 'Internal' },
  { id: EventType.SnmpTrap, name: 'SNMP Trap' },
  { id: EventType.Syslog, name: 'Syslog' },
  { id: EventType.MetricThreshold, name: 'Metric Threshold' }
]

const disableSaveRuleBtn = computed(
  () => monitoringPoliciesStore?.selectedPolicy?.isDefault || !monitoringPoliciesStore?.selectedRule?.name
)

const selectEventType = (eventType: EventType) => {
  // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
  monitoringPoliciesStore.selectedRule!.eventType = eventType
}
</script>

<style lang="scss" scoped>
@use '@featherds/styles/mixins/typography';
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/lib/grid';
@use '@featherds/table/scss/table';
@use '@/styles/vars.scss';

.main-alert {
  height: auto;

  .main-heading {
    border-bottom: 1px solid #E1E1E3;
  }

  .header {
    padding: 20px;
    min-height: 11%;
  }
  .scroll-bar {
    overflow: auto;
    height: calc(100vh - 10rem);
  }
  .content {
    padding: 10px 20px 20px 20px;

    .name {
      margin-top: 5px;
      :deep(.label-border) {
        min-width: 50px !important;
      }
    }

    .subtitle {
      @include typography.subtitle1;
      margin-bottom: 5px;
    }

    .basic-information {
      margin: 8px 0px;
    }
  }

  .footer {
    padding: 10px 20px 5px 20px;
    display: flex;
    align-items: center;
    justify-content: flex-end;
  }
}
</style>
