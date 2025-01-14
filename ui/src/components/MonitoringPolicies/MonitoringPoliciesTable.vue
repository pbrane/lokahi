<template>
  <div class="monitoring-policies-table-wrapper">
    <TableCard class="card border">
      <div class="header">
        <div class="title-container">
          <div class="title-time">
            <span class="title"/>
          </div>
          <div class="btns">
            <FeatherButton
              primary
              icon="Download"
              @click="onDownload"
            >
              <FeatherIcon :icon="icons.DownloadFile" />
            </FeatherButton>
            <FeatherButton
              primary
              icon="Refresh"
              @click="onRefresh"
            >
              <FeatherIcon :icon="icons.Refresh" />
            </FeatherButton>
          </div>
        </div>
      </div>
      <div class="container">
        <table class="data-table" aria-label="Monitoring Policies Table">
          <thead>
            <tr>
              <FeatherSortHeader v-for="col of columns" :key="col.label" scope="col" :property="col.id"
                :sort="(sort as any)[col.id]" v-on:sort-changed="sortChanged">
                {{ col.label }}
              </FeatherSortHeader>
            </tr>
          </thead>
          <TransitionGroup name="data-table" tag="tbody">
            <tr v-for="policy in pageData" :key="policy.id"
              :class="{ 'policies-table-row': true, 'active': policy.id === store.selectedPolicy?.id }"
              @click="() => onSelectPolicy(policy.id)">
              <td>
                <div class="action">
                  <span class="check-circle">
                    <FeatherTooltip
                      :title="policy?.enabled ? 'Enabled' : 'Disabled'"
                      v-slot="{ attrs, on }"
                    >
                      <FeatherIcon
                        v-bind="attrs"
                        v-on="on" :icon="policy?.enabled ? CheckCircle : Circle"
                        :class="{ 'enabled': policy?.enabled }"
                        class="enabled-icon"
                        data-test="check-icon"
                      />
                    </FeatherTooltip>
                  </span>
                  <span class="policy-name">
                    {{ policy.name }}
                  </span>
                </div>
              </td>
              <td>{{ policy.memo }}</td>
              <td>{{ policy.rules?.length || 0 }}</td>
              <td>{{ (policy.id && store.affectedNodesByMonitoringPolicyCount?.get(policy.id)) || '--' }}</td>
            </tr>
          </TransitionGroup>
        </table>
        <FeatherPagination
          v-model="page"
          :pageSize="pageSize"
          :total="total"
          @update:modelValue="updatePage"
          @update:pageSize="updatePageSize"
          v-if="hasMonitoringPolicies"
        />
      </div>
    </TableCard>
  </div>
</template>

<script setup lang="ts">
import Circle from '@/assets/circle.svg'
import { useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'
import { DownloadCSVMonitoringPoliciesVariables, DownloadFormat } from '@/types/graphql'
import { MonitorPolicyFilters, MonitoringPolicy } from '@/types/policies'
import CheckCircle from '@featherds/icon/action/CheckCircle'
import DownloadFile from '@featherds/icon/action/DownloadFile'
import Refresh from '@featherds/icon/navigation/Refresh'
import { SORT } from '@featherds/table'
import { sortBy } from 'lodash'

const store = useMonitoringPoliciesStore()
const page = ref(0)
const pageSize = ref(0)
const total = ref(0)
const pageData = ref([] as MonitoringPolicy[])
const clonedMonitoringPolicies = ref([] as MonitoringPolicy[])
const policiesFilter = ref({} as MonitorPolicyFilters)
const icons = markRaw({
  CheckCircle,
  DownloadFile,
  Refresh
})

const props = defineProps<{
  refreshMonitoringPolicies: boolean
}>()

const emit = defineEmits<{
  (e: 'policySelected', id: number): void
}>()

const onSelectPolicy = (id?: number) => {
  if (id) {
    const selectedPolicy = store.monitoringPolicies.find((item: MonitoringPolicy) => item.id === Number(id))

    if (selectedPolicy) {
      store.displayPolicyForm(selectedPolicy)
      emit('policySelected', Number(selectedPolicy.id))
    }
  }
}

const hasMonitoringPolicies = computed(() => store.monitoringPolicies && store.monitoringPolicies.length > 0)

const loadData = () => {
  if (hasMonitoringPolicies.value) {
    page.value = 1
    pageSize.value = 10
    clonedMonitoringPolicies.value = store.monitoringPolicies
    total.value = clonedMonitoringPolicies.value.length
    pageData.value = getPageObjects(clonedMonitoringPolicies.value, page.value, pageSize.value)
  }
}

onMounted(() => {
  store.loadVendors()
  store.getInitialAlertEventDefinitions()
  loadData()
  policiesFilter.value.sortBy = 'id'
  policiesFilter.value.sortAscending = true
  if (store.selectedPolicy?.id) {
    onSelectPolicy(store.selectedPolicy.id)
  }
})

watch(() => [hasMonitoringPolicies.value, props.refreshMonitoringPolicies], () => {
  loadData()
})

const columns = [
  { id: 'name', label: 'Name' },
  { id: 'memo', label: 'Description' },
  { id: 'alertRules', label: 'Alert Rules' },
  { id: 'affectedNodes', label: 'Affected Nodes' }
]

const sort = reactive({
  name: SORT.NONE,
  memo: SORT.NONE,
  alertRules: SORT.NONE,
  affectedNodes: SORT.NONE
}) as any

const sortChanged = (sortObj: Record<string, string>) => {
  policiesFilter.value.sortAscending = sortObj.value === 'asc' ? true : false

  if (sortObj.property !== 'affectedNodes' && sortObj.property !== 'alertRules') {
    policiesFilter.value.sortBy = sortObj.property
    if (sortObj.value === 'asc') {
      clonedMonitoringPolicies.value = sortBy(store.monitoringPolicies, sortObj.property)
    } else if (sortObj.value === 'desc') {
      clonedMonitoringPolicies.value = sortBy(store.monitoringPolicies, sortObj.property).reverse()
    } else {
      clonedMonitoringPolicies.value = store.monitoringPolicies
    }
  } else if (sortObj.property === 'alertRules') {
    policiesFilter.value.sortBy = sortObj.property
    if (sortObj.value === 'asc') {
      clonedMonitoringPolicies.value = sortBy(store.monitoringPolicies, item => item.rules?.length)
    } else if (sortObj.value === 'desc') {
      clonedMonitoringPolicies.value = sortBy(store.monitoringPolicies, item => item.rules?.length ? -item.rules.length : 0)
    } else {
      clonedMonitoringPolicies.value = store.monitoringPolicies
    }
  }

  pageData.value = getPageObjects(clonedMonitoringPolicies.value, page.value, pageSize.value)

  for (const prop in sort) {
    sort[prop] = SORT.NONE
  }
  sort[sortObj.property] = sortObj.value
}

// Function to retrieve objects for a given page
const getPageObjects = (array: Array<any>, pageNumber: number, pageSize: number) => {
  const startIndex = (pageNumber - 1) * pageSize
  const endIndex = startIndex + pageSize
  return array.slice(startIndex, endIndex)
}

const onDownload = () => {
  const request: DownloadCSVMonitoringPoliciesVariables = {
    page: page.value > 0 ? page.value - 1 : 0,
    pageSize: pageSize.value,
    sortAscending: policiesFilter.value.sortAscending,
    sortBy: policiesFilter.value.sortBy === 'affectedNodes' || policiesFilter.value.sortBy === 'alertRules' ? 'id' : policiesFilter.value.sortBy,
    downloadFormat: DownloadFormat.Csv
  }

  store.downloadMonitoringPoliciesCSV(request)
}

const onRefresh = async () => {
  await store.getMonitoringPolicies()
}

const updatePage = (v: number) => {
  if (hasMonitoringPolicies.value) {
    page.value = v
    pageData.value = getPageObjects(clonedMonitoringPolicies.value, v, pageSize.value)
  }
}

const updatePageSize = (v: number) => {
  if (hasMonitoringPolicies.value) {
    pageSize.value = v
    page.value = 1
    pageData.value = getPageObjects(clonedMonitoringPolicies.value, page.value, v)
  }
}
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/_transitionDataTable';
@use '@/styles/vars.scss';

.monitoring-policies-table-wrapper {
  margin-bottom: 20px;
  width: 100%;
  overflow-x: hidden;

  .card {
    .header {
      display: flex;
      justify-content: space-between;

      .title-container {
        display: flex;
        justify-content: space-between;
        width: 100%;
        margin-bottom: 20px;

        .title-time {
          display: flex;
          flex-direction: column;

          .title {
            @include typography.headline3;
            margin-left: 20px;
            margin-top: 2px;
          }
        }

        .btns {
          margin-right: 15px;

          :deep(:focus) {
            outline: none;
          }
        }
      }
    }
  }

  .container {
    display: block;
    overflow-x: auto;

    table {
      width: 100%;
      @include table.table;

      .active {
        background-color: var(variables.$shade-4)
      }

      thead {
        background: var(variables.$background);
        text-transform: uppercase;
      }

      .action {
        display: flex;
        justify-content: flex-start;
        align-items: center;
        gap: 20px;
      }

      td {
        white-space: nowrap;

        div {
          border-radius: 5px;
        }
      }

      tr.policies-table-row {
        cursor: pointer;
        transition: background-color 0.3s ease;

        &:hover {
          background-color: var(variables.$shade-4)
        }
      }

    }
  }
}

.check-circle {
  margin-top: 5px;

  .enabled-icon {
    width: 1.5rem;
    height: 1.5rem;
    color: var(variables.$shade-3);

    &.enabled {
      color: var(variables.$success);
    }
  }
}
</style>
