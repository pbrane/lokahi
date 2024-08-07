<template>
  <div class="service-table-wrapper">
    <TableCard class="card" v-if="store.monitoredEntityStatesList.length && !store.loading" >
      <div class="container">
        <table class="data-table" aria-label="All Services Table">
          <thead>
            <tr class="header-row">
              <FeatherSortHeader 
                v-for="col of columns"
                :key="col.label"
                scope="col"
                :property="col.id"
                :sort="(sort as any)[col.id]"
              >
                {{ col.label }}
              </FeatherSortHeader>
              <th>Reachability</th>
              <th>Status</th>
              <th>Latency</th>
              <th>Actions</th>
            </tr>
          </thead>
          <TransitionGroup name="data-table" tag="tbody">
            <tr class="policies-table-row" v-for="service of monitoredEntityStatesList" :key="service?.id">
              <td>{{ extractMetricValue('monitor', service) }}</td>
              <td>{{ service.monitoredEntityId }}</td>
              <td>{{ fnsFormat(service.firstObservationTime, 'M/dd/yyyy HH:mm:ssxxx') }}</td>
              <td>
                <TextBadge :type="getBadgeType('reachability', extractMetricValue('reachability', service))">
                  {{ extractMetricValue('reachability', service) ? `${extractMetricValue('reachability', service)}%` : `--` }}
                </TextBadge>
              </td>
              <td>
                <TextBadge :type="getBadgeType('status', extractMetricValue('status', service))">
                  {{ extractMetricValue('status', service) }}
                </TextBadge>
              </td>
              <td>
                <TextBadge :type="getBadgeType('latency', extractMetricValue('latency', service))">
                  {{ extractMetricValue('reachability', service) ? `${extractMetricValue('latency', service)}ms` : `--` }}
                </TextBadge>
              </td>
              <td class="actions-icons">
                <ServiceInventoryIconActions :service="service" nodeEdit="edit" class="icon-action"
                  data-test="icon-action-list" />
              </td>
            </tr>
          </TransitionGroup>
        </table>
        <FeatherPagination
          v-model="store.pagination.page"
          :pageSize="store.pagination.pageSize"
          :total="store.pagination.total"
          @update:modelValue="onPageChanged"
          @update:pageSize="onPageSizeChanged"
          v-if="hasMonitoredEntityStates && store.pagination.total" 
        />
      </div>
    </TableCard>
    <FeatherSpinner v-if="store.loading" />
  </div>
</template>

<script setup lang="ts">
import useSpinner from '@/composables/useSpinner'
import { useServiceInventoryStore } from '@/store/Views/serviceInventoryStore'
import { MonitoredEntityState } from '@/types/monitoredEntityState'
import { SORT } from '@featherds/table'
import { format as fnsFormat } from 'date-fns'
import { reactive } from 'vue'
import TextBadge from '../Common/TextBadge.vue'
import { columns } from './serviceinventory.constants'

const sort = reactive({
  Type: SORT.ASCENDING,
  Service: SORT.NONE,
  Node: SORT.ASCENDING,
  Description: SORT.NONE,
  Reachability: SORT.NONE,
  Latency: SORT.NONE,
  Uptime: SORT.NONE,
  Actions: SORT.NONE
}) as any

const hasMonitoredEntityStates = computed(() => store.monitoredEntityStatesList && store.monitoredEntityStatesList.length > 0)

const store = useServiceInventoryStore()
const { startSpinner, stopSpinner } = useSpinner()
const monitoredEntityStatesList = ref<Array<MonitoredEntityState>>([])

onMounted(async () => {
  startSpinner()
  monitoredEntityStatesList.value = await store.getMonitoredEntityStatesList()
  stopSpinner()
})

const getBadgeType = (key: string, value: any) => {
  if (key === 'reachability') {
    return parseFloat(value) > 50 ? 'indeterminate' : 'critical'
  }

  if (key === 'latency') {
    return 'indeterminate'
  }

  if (key === 'status') {
    return String(value) === 'Up' ? 'indeterminate' : 'critical'
  }

  return ''
}

const extractMetricValue = (key: string, service: MonitoredEntityState) => {
  if (key === 'monitor') {
    return store.monitoredEntityStatesMetric.get(service.monitoredEntityId)?.monitor
  }

  if (key === 'reachability') {
    return store.monitoredEntityStatesMetric.get(service.monitoredEntityId)?.reachabilityValue[1].toFixed(2)
  }

  if (key === 'latency') {
    return store.monitoredEntityStatesMetric.get(service.monitoredEntityId)?.latencyValue[1].toFixed(2)
  }

  if (key === 'status') {
    return store.monitoredEntityStatesMetric.get(service.monitoredEntityId)?.status
  }
}

const onPageChanged = (v: number) => {
  startSpinner()
  store.setMonitoredEntityStatePage(v)
  stopSpinner()
}
const onPageSizeChanged = (v: number) => {
  startSpinner()
  store.setMonitoredEntityStatePageSize(v)
  stopSpinner()
}
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/_transitionDataTable';
@use '@/styles/vars.scss';

.service-table-wrapper {
  width: 100%;
  overflow-x: hidden;

  .card {
    padding: 0px !important;
  }

  .container {
    display: block;
    overflow-x: auto;
    box-shadow: 1px 1px 2px var(--feather-disabled-text-on-surface);

    table {
      width: 100%;
      @include table.table;

      thead {
        background: var(variables.$background);
        text-transform: uppercase;
      }

      .node-alias {
        color: var(variables.$primary);
        font-weight: bold;
      }

      .node-tags {
        color: var(variables.$primary);
      }

      td {
        white-space: nowrap;

        .toggle-wrapper {
          display: flex;
          justify-content: flex-start;
          align-items: center;

          :deep(.feather-list-item) {
            padding: 0px !important;
          }
        }
      }
    }
  }

  .feather-pagination {
    border: none !important;
  }
}
</style>
