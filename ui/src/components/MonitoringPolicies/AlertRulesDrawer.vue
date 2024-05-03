<template>
    <FeatherDrawer
      v-model:model-value="monitoringPoliciesStore.alertRuleDrawer"
      v-on:hidden="monitoringPoliciesStore.closeAlertRuleDrawer"
      width="50em"
      class="alert-drawer"
    >
      <CreateEditAlertRulePanel/>
    </FeatherDrawer>
</template>

<script setup lang="ts">
import { useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'

const monitoringPoliciesStore = useMonitoringPoliciesStore()

</script>
