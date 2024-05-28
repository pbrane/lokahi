<template>
  <div class="monitoring-policies-edit-panel">
    <div class="monitoring-policy-header">
      <div class="page-headline1">Monitoring Policies</div>
      <FeatherButton
        primary
        @click="handleGoBack"
        data-test="create-policy-btn"
        >Go Back
      </FeatherButton>
    </div>
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
          <FeatherButton
           primary v-if="isPolicyEditable"
           :disabled="isSavePolicyDisabled"
           @click.prevent="savePolicy">
           Save Policy
          </FeatherButton>
          <FeatherButton v-else primary @click="handleNext">
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
import router from '@/router'
import { useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'
import { CreateEditMode } from '@/types'

const selectedTab = ref(0)
const store = useMonitoringPoliciesStore()
const route = useRoute()

onMounted(() => {
  store.cacheAlertsByRuleId()
})

const handleNext = () => {
  if (route.params.id == '0') {
    store.savePolicy({isCopy: true})
  } else {
    selectedTab.value = 1
  }
}

const isPolicyEditable = computed(() => store.policyEditMode === 2)
const isSavePolicyDisabled = computed(
  () => !store.selectedPolicy?.rules?.length || !store.selectedPolicy?.name || store.selectedPolicy.isDefault
)

const handleCancel = () => {
  store.clearSelectedPolicy()
  store.clearSelectedRule()
  store.setPolicyEditMode(CreateEditMode.None)
  store.setRuleEditMode(CreateEditMode.None)
  store.cachedAffectedAlertsByRule = new Map()
  router.push('/monitoring-policies')
}

const handleGoBack = () => {
  handleCancel()
}

const savePolicy = async () => {
  const result = await store?.savePolicy()
  if (result) {
    router.push('/monitoring-policies')
  }
}
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
