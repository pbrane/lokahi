<template>
  <div class="container">
    <div class="header">
      <PageHeadline text="Insights Dashboard" />
      <DashboardHeaderLinks />
    </div>
    <div class="section-title">Alert Status</div>
    <div class="list-alerts">
      <AlertsSeverityFilters @click="redirect('Alerts')" />
    </div>
    <div>
      <DashboardApplications />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useFlowsStore } from '@/store/Views/flowsStore'

const router = useRouter()
const flowsStore = useFlowsStore()

const redirect = (route: string) => {
  router.push(route)
}

onMounted(async () => {
  await flowsStore.getApplicationDataset()
})
</script>

<style scoped lang="scss">
@use '@featherds/styles/mixins/typography';
@use '@featherds/styles/themes/variables';
@use '@/styles/mediaQueriesMixins.scss';
@use '@/styles/vars.scss';

.container {
  display: flex;
  margin: 0 1rem;
  flex-direction: column;
  width: 100%;
  padding: var(variables.$spacing-l) var(variables.$spacing-m);
  margin: auto;
  @include mediaQueriesMixins.screen-md {
    padding: var(variables.$spacing-xl);
  }
  > .header {
    @include typography.headline2();
    display: flex;
    align-items: center;
    flex-direction: column;
    width: 100%;

    @include mediaQueriesMixins.screen-md {
      flex-direction: row;
      justify-content: space-between;
    }
  }
  .list-alerts {
    overflow-x: auto;
  }
  .section-title {
    @include typography.headline3();
  }
}
</style>
