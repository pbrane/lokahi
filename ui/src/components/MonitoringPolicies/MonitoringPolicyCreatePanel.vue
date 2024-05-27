<template>
  <div class="monitoring-policies-edit-panel">
    <div class="monitoring-policy-header">
      <div class="page-headline1">Monitoring Policies</div>
      <FeatherButton
        primary
        @click="handleGoBack"
        data-test="create-policy-btn"
        >
        Go Back
      </FeatherButton>
    </div>
    <FeatherTabContainer class="mt-2 " :modelValue="selectedTab" >
      <template v-slot:tabs>
        <FeatherTab>Basic Information</FeatherTab>
        <FeatherTab>Alert Rules</FeatherTab>
      </template>
      <FeatherTabPanel class="bg-white mt-0 p-2" >
        <MonitoringPolicyBasicInformationEditAddForm />
        <div class="d-flex justify-content-end mt-2">
          <FeatherButton text @click="handleCancel">Cancel</FeatherButton>
          <FeatherButton primary  @click="handleNext">Next</FeatherButton>
        </div>
      </FeatherTabPanel>
      <FeatherTabPanel>TODO</FeatherTabPanel>
    </FeatherTabContainer>
  </div>
</template>

<script setup lang="ts">
import { useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'
import { Policy } from '@/types/policies'
import router from '@/router'

const store = useMonitoringPoliciesStore()
const route = useRoute()
const selectedTab = ref(0)
const handleCancel = () => {
  router.push('/monitoring-policies-new/')
}

const handleGoBack = () => {
  router.push('/monitoring-policies-new/')
}
const handleNext = () => {
  selectedTab.value = 1
}
watchEffect(() => {
  if (store.monitoringPolicies.length > 0 && route?.params?.id) {
    const filteredPolicy = store.monitoringPolicies.find((item: Policy) => item.id === Number(route.params.id))
    store.displayPolicyForm(filteredPolicy as Policy)
  }
})
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@/styles/vars.scss';

.monitoring-policies-edit-panel {
  max-width: 1222px;
  margin: 2% auto;
  .monitoring-policy-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    column-gap: 5px;
    :deep(.btn-secondary){
      border: none;
    }
  }
}

.bg-white {
  margin-bottom: var(--feather-spacing-l);
  background: var(--feather-surface);
  padding: 30px
}

.page-headline1 {
  @include typography.headline1;
}

.page-headline2 {
  @include typography.headline2;
}

.page-headline3 {
  @include typography.headline3;
}

.mt-2 {
  margin-top: var(variables.$spacing-m);
}

.p-2 {
  padding: var(variables.$spacing-m);
}

.d-flex {
  display: flex;
}

.justify-content-center {
  justify-content: center;
}

.justify-content-end {
  justify-content: end;
}
</style>
