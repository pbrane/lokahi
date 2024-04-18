<template>
  <div class="monitoring-policies-edit-panel">
    <div class="page-headline1">Monitoring Policies</div>
    <FeatherTabContainer class="mt-2" :modelValue="selectedTab">
      <template v-slot:tabs>
        <FeatherTab>Basic Information</FeatherTab>
        <FeatherTab>Alert Rules</FeatherTab>
      </template>
      <FeatherTabPanel class="bg-white mt-0 p-2">
        <!-- copy Initial from Here -->
        <MonitoringPolicyBasicInformationEditAddForm />
        <div class="d-flex justify-content-end mt-2">
          <FeatherButton text @click="handleCancel">Cancel</FeatherButton>
          <FeatherButton primary @click="handleNext">
            {{ route?.params?.id !== '0' ? 'Next' : 'Save' }}
          </FeatherButton>
        </div>
      </FeatherTabPanel>
      <FeatherTabPanel>
        <MonitoringPoliciesRules />
      </FeatherTabPanel>
    </FeatherTabContainer>
  </div>
</template>

<script setup lang="ts">
import { useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'
import { Policy } from '@/types/policies'
import router from '@/router'

const selectedTab = ref(0)
const store = useMonitoringPoliciesStore()
const route = useRoute()
const handleNext = () => {
  if (route.params.id == '0') {
    store.savePolicy(true)
  } else {
    selectedTab.value = 1
  }
}

const handleCancel = () => {
  store.clearSelectedPolicy()
  router.push('/monitoring-policies-new/')
}
const deleteMsg = computed(() =>
  `Deleting monitoring policy ${store.selectedPolicy?.name} removes ${store.numOfAlertsForPolicy} associated alerts. Do you wish to proceed?`
)

watchEffect(() => {
  if (store.monitoringPolicies.length > 0 && route?.params?.id !== '0') {
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
