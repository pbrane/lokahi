<template>
  <div class="flows-container">
    <PageHeadline
      text="Flows"
      data-test="flows-page-header"
    />
    <!-- Filter Area -->
    <div class="filters">
      <BasicChipSelect
        :list="timeOptions"
        :size="160"
        :show-chip="true"
      />

      <div class="filters-divider"></div>

      <BasicAutocomplete
        class="filter-autocomplete"
        :get-items="getAppliications"
        :items="applicationsAutoComplete"
        label="Filter Exporters"
        ref="exportersAutocompleteRef"
      />

      <div class="filters-divider"></div>

      <BasicAutocomplete
        class="filter-autocomplete"
        :get-items="getAppliications"
        :items="applicationsAutoComplete"
        label="Filter Applications"
        ref="appsAutocompleteRef"
      />
    </div>

    <!-- Chart Area -->
    <div class="flows">
      <ExpandingChartWrapper
        :title="'Top Ten Exporters (24 Hrs) - Total'"
        :model-value="flowsStore.exporters.expansionOpen"
        :on-filter-click="(e) => flowsStore.filterDialogToggle(e, false)"
      >
        <TableChart
          :id="'tableChartExporters'"
          :selected-filter-range="flowsStore.filters.dateFilter"
          :chart-data="flowsStore.exporters.tableChartData"
          :table-data="flowsStore.datasets"
        >
        </TableChart>
      </ExpandingChartWrapper>

      <ExpandingChartWrapper
        :title="'Top Ten Applications (24 Hrs) - Total'"
        :model-value="flowsStore.applications.expansionOpen"
        :on-filter-click="(e) => flowsStore.filterDialogToggle(e, true)"
      >
        <TableChart
          :id="'tableChartApplications'"
          :selected-filter-range="flowsStore.filters.dateFilter"
          :chart-data="flowsStore.applications.tableChartData"
          :table-data="flowsStore.datasets"
        >
        </TableChart>
      </ExpandingChartWrapper>
    </div>
  </div>
  <FeatherDialog
    id="appDialog"
    v-model="flowsStore.applications.filterDialogOpen"
    :labels="appDialogLabels"
    @update:model-value="(e) => (flowsStore.applications.filterDialogOpen = e)"
  >
    <FeatherCheckboxGroup
      label=""
      vertical
      class="chart-dialog-group"
    >
      <FeatherCheckbox v-model="flowsStore.applications.dialogFilters.http">HTTP</FeatherCheckbox>
      <FeatherCheckbox v-model="flowsStore.applications.dialogFilters.https">HTTPS</FeatherCheckbox>
      <FeatherCheckbox v-model="flowsStore.applications.dialogFilters.pandoPub">Pando-Pub</FeatherCheckbox>
      <FeatherCheckbox v-model="flowsStore.applications.dialogFilters.snmp">SNMP</FeatherCheckbox>
      <FeatherCheckbox v-model="flowsStore.applications.dialogFilters.imaps">IMAPS</FeatherCheckbox>
    </FeatherCheckboxGroup>

    <template v-slot:footer>
      <FeatherButton
        primary
        @click="flowsStore.appDialogRefreshClick"
        >Refresh</FeatherButton
      >
    </template>
  </FeatherDialog>
  <FeatherDialog
    v-model="flowsStore.exporters.filterDialogOpen"
    :labels="expDialogLabels"
    @update:model-value="(e) => (flowsStore.exporters.filterDialogOpen = e)"
  >
    <FeatherCheckboxGroup
      label=""
      vertical
      class="chart-dialog-group"
    >
      <FeatherCheckbox v-model="flowsStore.exporters.dialogFilters.http">HTTP</FeatherCheckbox>
      <FeatherCheckbox v-model="flowsStore.exporters.dialogFilters.https">HTTPS</FeatherCheckbox>
      <FeatherCheckbox v-model="flowsStore.exporters.dialogFilters.pandoPub">Pando-Pub</FeatherCheckbox>
      <FeatherCheckbox v-model="flowsStore.exporters.dialogFilters.snmp">SNMP</FeatherCheckbox>
      <FeatherCheckbox v-model="flowsStore.exporters.dialogFilters.imaps">IMAPS</FeatherCheckbox>
    </FeatherCheckboxGroup>

    <template v-slot:footer>
      <FeatherButton
        primary
        @click="flowsStore.expDialogRefreshClick"
        >Refresh</FeatherButton
      >
    </template>
  </FeatherDialog>
</template>

<script setup lang="ts">
import { useFlowsStore } from '@/store/Views/flowsStore'

const flowsStore = useFlowsStore()
const appDialogLabels = {
  title: 'Top Ten Applications (24 Hrs) - Total'
}
const expDialogLabels = {
  title: 'Top Ten Exporters (24 Hrs) - Total'
}

onBeforeMount(async () => {
  flowsStore.generateTableChart()
})

// DUMMY DATA
const timeOptions = [
  { id: 'today', name: 'Today' },
  { id: '24h', name: 'Last 24 hours' },
  { id: 'week', name: 'Last 7 days' }
]
const applicationsAutoComplete = ref([
  { id: 'app1', name: 'Application 1' },
  { id: 'app2', name: 'Application 2' },
  { id: 'app3', name: 'Application 3' }
])
const getAppliications = () => {
  return {}
}
</script>

<style scoped lang="scss">
@use '@featherds/styles/themes/variables';
@use '@/styles/vars';
@use '@/styles/mediaQueriesMixins.scss';
@use '@featherds/styles/mixins/typography';

.flows {
  width: 100%;
  min-width: 400px;
  border: 1px solid var(variables.$border-on-surface);
  border-radius: vars.$border-radius-s;
  padding: var(variables.$spacing-m);
  background-color: var(variables.$surface);
  display: flex;
  flex-direction: column;
  gap: var(variables.$spacing-m);

  &-container {
    margin: var(variables.$spacing-m);
    @include mediaQueriesMixins.screen-md {
      margin: 40px 80px;
    }
  }
}
.filters {
  margin-bottom: var(variables.$spacing-m);
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
  align-content: stretch;
  gap: var(variables.$spacing-xl);
  .filter-autocomplete {
    width: 100%;
    max-width: 360px;
  }
  &-divider {
    border: 1px solid #74757d;
    display: flex;
    width: 0px;
    align-self: stretch;
  }
  :deep(.feather-input-wrapper-container) {
    background-color: var(variables.$surface);
  }
}
.chart-dialog-group {
  min-width: 325px;
}
</style>
