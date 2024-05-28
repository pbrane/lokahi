<template>
  <div class="container">
    <div class="content">
      <div class="header">
        <HeadlinePage text="Monitoring Policies" />
        <FeatherButton
          primary
          @click="onCreatePolicy"
          data-test="create-policy-btn"
          >Create Policy
        </FeatherButton>
      </div>
        <section class="feather-row">
          <div :class="displayDetails ? 'feather-col-8' : 'feather-col-12'">
            <MonitoringPoliciesTable
              @policy-selected="displayDetails = true"
              :refreshMonitoringPolicies = "reFresh"
            />
          </div>
          <div v-if="displayDetails" class="feather-col-4">
            <MonitoringPoliciesDetailPanel
              @on-close="displayDetails = false"
              @on-refresh="onRefresh"
            />
          </div>
        </section>
     </div>
    </div>
</template>

<script setup lang="ts">
import router from '@/router'
import { useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'
import { CreateEditMode } from '@/types'

const displayDetails = ref<boolean>(false)
const reFresh = ref<boolean>(false)
const store = useMonitoringPoliciesStore()

const onCreatePolicy = () => {
  store.displayPolicyForm()
  store.setPolicyEditMode(CreateEditMode.Create)
  router.push('/monitoring-policies/create')
}

onMounted(() => store.getMonitoringPolicies())
const onRefresh = () => {
  reFresh.value = !reFresh.value
}
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
}

.content {
  width: 86%;
  margin-right: var(variables.$spacing-l);
  margin-left: var(variables.$spacing-l);
}

.header {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
}

</style>
