<template>
  <div class="container">
    <div class="content">
      <div class="policies-content">
        <section class="feather-row">
          <div class="feather-col-12">
            <div class="alert-rules-header">
              <p class="title">Alert Rules</p>
              <p class="sub-title">Create one or more alert rules to define your alert triggers. Monitoring Policies must include at least one Alert Rule.</p>
            </div>
            <div class="alerts-rule-table">
              <MonitoringPoliciesAlertsRulesTable/>
            </div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped lang="scss">
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/lib/grid';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/vars.scss';

.container {
  display: flex;
  justify-content: center;
  .content {
  width: 85%;
  margin: var(variables.$spacing-xxl) var(variables.$spacing-l) 0 var(variables.$spacing-l);
 }
}

.title {
  font-weight: 900;
  font-size: 18px;
}

.sub-title {
  font-size: 14px;
  margin-top: 5px;
}

.policies-content {
  background: var(variables.$surface);
  padding: var(variables.$spacing-l);
  border: 1px solid var(variables.$border-on-surface);
  .alerts-rule-table,.alert-rules-header {
    margin: var(variables.$spacing-m);
  }
  .alert-rules-header{
    margin-bottom: var(variables.$spacing-xl);
  }
}

</style>
