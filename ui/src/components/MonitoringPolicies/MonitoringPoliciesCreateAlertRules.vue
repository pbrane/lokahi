<template>
    <div class="alert-rules-container">
    <div class="alert-rules-content">
       <CreateEditAlertRulePanel
        :title="title"
        :subTitle="subTitle"
        :scrollBar="scrollBar"/>
    </div>
   </div>

</template>

<script setup lang="ts">
import { getDefaultRule, useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'
import { CreateEditMode } from '@/types'

const monitoringPoliciesStore = useMonitoringPoliciesStore()
const scrollBar = ref(true)
const title = ref('Specify Alert Conditions')
const subTitle = ref('Alert conditions are an additional set of parameters specific to the chosen detection method. Alert condition help to  determine what alerts will be triggered and their assigned severity.')

onMounted(() => {
  monitoringPoliciesStore.setRuleEditMode(CreateEditMode.Create)
  monitoringPoliciesStore.displayRuleForm(getDefaultRule(monitoringPoliciesStore.cachedEventDefinitions?.get('internal') ?? []))
})
</script>

<style scoped lang="scss">
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/lib/grid';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/vars.scss';

.alert-rules-container {
  position: relative;
  border: 1px solid var(variables.$border-on-surface);
  border-radius: vars.$border-radius-surface;
  padding: var(--feather-spacing-m);
  margin-top: 30px;
  font-family: 'Roboto', sans-serif;
  box-shadow: 5px 5px 5px var(variables.$surface);

  .alert-rules-content {
    position: relative;
    display: flex;
    flex-direction: column;
    top: 10px;
    flex-grow: 1;
  }
}
</style>
