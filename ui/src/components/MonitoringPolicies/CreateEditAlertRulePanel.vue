<template>
      <div class="main">
        <div :class="['header', {'main-heading':!title}]">
          <h2>{{ title || 'Create New Alert Rule' }}</h2>
          <p> {{ subTitle || 'A rule is a condition or set of conditions that triggers an alert.' }}</p>
        </div>
        <div class="content">
          <div class="basic-information">
            <h3>Basic Information</h3>
            <p>Create a clear and descriptive name for the rule to easily identify its purpose and functionality. Then select a event type.</p>
          </div>

          <div class="name">
            <FeatherInput
              v-model.trim="monitoringPoliciesStore.selectedRule!.name"
              label="Name"
              v-focus
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
            >
            </BasicSelect>
          </div>
          <div class="basic-information">
              <h2>Specify Trigger and Clear Event</h2>
              <p>Select a Trigger and Clear event for the alert. Clear events will be limited based on the trigger event.</p>
            </div>
          <div class="event">
            <div class="event-trigger">
              <div class="subtitle">Trigger Event</div>
              <BasicSelect
              :list="eventTriggerOptions"
              @item-selected.stop=""
              :selectedId="monitoringPoliciesStore.selectedRule?.eventType"
              :disabled="monitoringPoliciesStore.selectedPolicy?.isDefault"
              :icon="isIcon"
            />
            </div>

            <div class="event-clear">
              <div class="subtitle">Clear Event (Optional)</div>
              <BasicSelect
              :list="eventClearOptions"
              @item-selected.stop=""
              :selectedId="monitoringPoliciesStore.selectedRule?.eventType"
              :disabled="monitoringPoliciesStore.selectedPolicy?.isDefault"
              :icon="isIcon"
            />
            </div>
          </div>

          <div class="alert-conditions">
            <h2>Specify Alert Conditions</h2>
            <p>Alert conditions are an additional set of parameters specific to the chosen detection method. Alert conditions help to determine what alerts will b etriggered and their assigned severity.</p>
          </div>
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
          <FeatherButton v-else class="feather-button" @click="monitoringPoliciesStore.saveRule">SAVE ALERT RULE</FeatherButton>
        </div>
      </div>
</template>

<script setup lang="ts">
import { useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'
import { EventType } from '@/types/graphql'

defineProps({
  title: String,
  subTitle: String
})

const isIcon = ref(true)
const monitoringPoliciesStore = useMonitoringPoliciesStore()

const eventTypeOptions = [
  { id: EventType.SnmpTrap, name: 'SNMP Trap' },
  { id: EventType.Internal, name: 'Internal' }
]

const eventTriggerOptions = [
  { id: EventType.SnmpTrap, name: 'Device Unreachable' }
]

const eventClearOptions = [
  { id: EventType.SnmpTrap, name: 'Device Service Restored' }
]

const disableSaveRuleBtn = computed(
  () => monitoringPoliciesStore.selectedPolicy?.isDefault || !monitoringPoliciesStore.selectedRule?.name || !monitoringPoliciesStore.selectedRule?.alertConditions?.length
)

const selectEventType = (eventType: EventType) => {
  monitoringPoliciesStore.selectedRule!.eventType = eventType
}

</script>

<style lang="scss" scoped>
@use '@featherds/styles/mixins/typography';
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/lib/grid';
@use '@featherds/table/scss/table';
@use '@/styles/vars.scss';

  .main {
    position: absolute;

    .main-heading{
      border-bottom: 1px solid #E1E1E3;
    }
    .header {
      padding: 20px;
      min-height: 11%;
    }

    .content {
      padding: 10px 20px 20px 20px;
      height: 80%;

      .subtitle {
        @include typography.subtitle1;
      }

      .basic-information {
        margin: 8px 0px;
      }

      .event {
        width: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
        column-gap: 10px;
        .event-trigger,.event-clear{
          width: 50%;
        }
      }
    }
    .footer {
      padding: 20px 20px 0 20px;
      height: 10%;
      display: flex;
      align-items: center;
      justify-content: flex-end;
      .feather-button {
        margin-top:  var(--feather-spacing-m);
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

