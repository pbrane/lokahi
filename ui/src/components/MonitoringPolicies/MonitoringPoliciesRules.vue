<template>
  <div class="container">
    <div class="content">
      <div class="policies-content">
        <section class="feather-row">
          <div class="feather-col-12">
            <div class="alert-rules-header">
              <p class="title">Alert Rules</p>
              <p class="sub-title">Create one or more alert rules to define your alert triggers. Monitoring Policies
                must include at least one Alert Rule.</p>
            </div>
            <div class="alerts-rule" v-if="!isCheckAlertRule">
              <MonitoringPoliciesAlertsRulesTable />
            </div>
            <div class="alerts-rule" v-if="isCheckAlertRule">
              <MonitoringPoliciesCreateAlertRules />
            </div>
            <div class="footer">
              <FeatherButton 
                secondary 
                @click="handleCancel" 
                class="cancel-button"
              >
                CANCEL
              </FeatherButton>
              <ButtonWithSpinner 
                primary 
                @click.prevent="savePolicy" 
                :disabled="savePolicyEnableDisable"
              >
                SAVE POLICY
              </ButtonWithSpinner>
            </div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'
import router from '@/router'

const monitoringPoliciesStore = useMonitoringPoliciesStore()
const isCheckAlertRule = ref()
const savePolicyEnableDisable = computed(
  () => !monitoringPoliciesStore.selectedPolicy?.rules?.length || !monitoringPoliciesStore.selectedPolicy?.name || monitoringPoliciesStore.selectedPolicy.isDefault
)

watchEffect(() => {
  isCheckAlertRule.value = (monitoringPoliciesStore.selectedPolicy?.rules?.length ?? 0) <= 0
})

const handleCancel = () => {
  monitoringPoliciesStore.clearSelectedPolicy()
  monitoringPoliciesStore.clearSelectedRule()
  router.push('/monitoring-policies-new/')
}

const savePolicy = async () => {
  const result = await monitoringPoliciesStore?.savePolicy()
  if (result) {
    router.push('/monitoring-policies-new/')
  }
}
</script>

<style scoped lang="scss">
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/lib/grid';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/vars.scss';

.title {
  font-weight: 900;
  font-size: 18px;
}

.content {
  .policies-content {
    background: var(variables.$surface);
    padding: var(variables.$spacing-l);
    border: 1px solid var(variables.$border-on-surface);

    .alerts-rule,
    .alert-rules-header {
      margin: var(variables.$spacing-m);
    }

    .alert-rules-header {
      margin-bottom: var(variables.$spacing-xl);
    }

    .feather-row {
      .feather-col-12 {
        .footer {
          padding: 20px;
          display: flex;
          align-items: center;
          justify-content: flex-end;

          .cancel-button {
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
    }
  }
}

.sub-title {
  font-size: 14px;
  margin-top: 5px;
}
</style>
