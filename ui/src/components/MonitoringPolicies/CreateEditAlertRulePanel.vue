<template>
    <FeatherDrawer
      v-model:model-value="monitoringPoliciesStore.alertRuleDrawer"
      v-on:hidden="monitoringPoliciesStore.closeAlertRuleDrawer"
      width="50em"
      class="alert-drawer"
    >
      <div class="main">
        <div class="header">
          <h2>Create New Alert Rule</h2>
          <p>A rule is a condition or set of conditions that triggers an alert.</p>
        </div>

        <div class="content">
          <div class="basic-information">
            <h2>Basic Information</h2>
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
            <div class="subtitle">Event Type</div>
            <BasicSelect
              :list="eventTypeOptions"
              @item-selected="selectEventType"
              :selectedId="monitoringPoliciesStore.selectedRule?.eventType"
              :disabled="monitoringPoliciesStore.selectedPolicy?.isDefault"
            />
          </div>

          <div class="alert-conditions">
            <h2>Specify Alert Conditions</h2>
            <p>Alert conditions are an additional set of parameters specific to the chosen detection method. Alert conditions help to determine what alerts will b etriggered and their assigned severity.</p>
          </div>
        </div>

        <div class="footer">
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
      </div>
    </FeatherDrawer>
</template>

<script setup lang="ts">
import { useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'
import { EventType } from '@/types/graphql'

const monitoringPoliciesStore = useMonitoringPoliciesStore()
const eventTypeOptions = [
  { id: EventType.SnmpTrap, name: 'SNMP Trap' },
  { id: EventType.Internal, name: 'Internal' }
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

.alert-drawer {
  .main {
    position: absolute;
    height: 100%;

    .header {
      padding: 20px;
      border-bottom: 1px solid #E1E1E3;
      height: 10%;
    }

    .content {
      padding: 20px;
      height: 80%;

      .subtitle {
        @include typography.subtitle1;
      }

      .basic-information {
        margin: 10px 0px;
      }
    }

    .footer {
      padding: 20px;
      height: 10%;
      display: flex;
      align-items: center;
      justify-content: flex-end;
    }
  }
}
</style>
