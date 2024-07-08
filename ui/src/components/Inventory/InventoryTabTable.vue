<template>
  <div class="monitoring-policies-table-wrapper">
    <TableCard class="card border">
      <div class="container">
        <table class="data-table" aria-label="Monitoring Policies Table">
          <thead>
          <tr class="header-row">
            <FeatherSortHeader
              v-for="col of columns"
              :key="col.label"
              scope="col"
              :property="col.id"
              :sort="(sort as any)[col.id]"
              v-on:sort-changed="sortChanged(col.id, $event)"
            >
              {{ col.label }}
            </FeatherSortHeader>
            <th>Tags</th>
            <th>Monitoring Location</th>
            <th>Latency</th>
            <th>Uptime</th>
            <th>Status</th>
            <th/>
          </tr>
        </thead>
          <TransitionGroup name="data-table" tag="tbody">
            <tr class="policies-table-row" v-for="node of tabContent" :key="node?.id">
              <td class="node-alias">{{ node?.nodeAlias || node?.nodeLabel }}</td>
              <td>{{ node.nodeLabel }}</td>
              <td class="node-tags">{{ node?.tags?.length || 0}}</td>
              <td>{{ node?.location?.location }}</td>
              <td>--</td>
              <td>--</td>
              <td v-if="state === MonitoredStates.MONITORED">
                <div v-for="badge, index in metricsAsTextBadges(node?.metrics)" :key="index">
                  <TextBadge v-if="badge.label" :type="badge.type">{{ badge.label }}</TextBadge>
                </div>
              </td>
              <td v-else>--</td>
              <td class="actions-icons">
              <InventoryIconActionList :node="node" nodeEdit="edit" className="icon-action" data-test="icon-action-list" />
            </td>
            </tr>
          </TransitionGroup>
        </table>
        <div v-if="!hasNodes || total === 0">
          <EmptyList :content="emptyListContent" data-test="empty-list" />
        </div>
      </div>
      <div class="alert-list-bottom" v-if="hasNodes">
        <FeatherPagination
          v-model="page"
          :pageSize="pageSize"
          :total="total"
          :pageSizes="[10, 20, 50]"
          @update:model-value="onPageChanged"
          @update:pageSize="onPageSizeChanged"
          data-test="pagination"
        />
      </div>
    </TableCard>
  </div>
  <AlertRulesDrawer/>
</template>

<script setup lang="ts">
import { InventoryItem, RawMetric, MonitoredStates } from '@/types'
import { PropType } from 'vue'
import { SORT } from '@featherds/table'
import { BadgeTypes } from '../Common/commonTypes'
import TextBadge from '../Common/TextBadge.vue'
import { useInventoryStore } from '@/store/Views/inventoryStore'
import useSpinner from '@/composables/useSpinner'

const { startSpinner, stopSpinner } = useSpinner()
const store = useInventoryStore()
defineProps({
  tabContent: {
    type: Object as PropType<InventoryItem[]>,
    required: true
  },
  state: {
    type: String,
    required: true
  }
})

const emptyListContent = {
  msg: 'No results found.'
}

const sort = reactive({
  NodeName: SORT.NONE,
  ManagementIP: SORT.NONE
}) as any

const columns: { id: string; label: string }[] = [
  { id: 'nodeAlias', label: 'Node Name' },
  { id: 'nodeLabel', label: 'Management IP' }
]

const page = computed(() => store.inventoryNodesPagination.page || 1)
const pageSize = computed(() => store.inventoryNodesPagination.pageSize)
const total = computed(() => store.inventoryNodesPagination.total)
const data = computed(() => store.nodes || [])

const hasNodes = computed(() => {
  return (data.value || []).length > 0
})

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const sortChanged = (columnId: string, sortObj: Record<string, string>) => {
  startSpinner()
  let sortByNode
  if (sortObj.value === 'asc' || sortObj.value === 'desc') {
    const sortAscending = sortObj.value === 'asc'
    sortByNode = { sortAscending, sortBy: columnId }
  } else {
    const sortAscending = true
    sortByNode = { sortAscending, sortBy: 'id' }
  }
  store.inventeriesByNodeSortChanged(sortByNode)
  for (const prop in sort) {
    sort[prop] = SORT.NONE
  }
  sort[sortObj.property] = sortObj.value
  stopSpinner()
}

const onPageChanged = (p: number) => {
  startSpinner()
  store.setInventoriesByNodePage(p)
  stopSpinner()
}

const onPageSizeChanged = (p: number) => {
  startSpinner()
  store.setInventoriesByNodePageSize(p)
  stopSpinner()
}

const metricsAsTextBadges = (metrics?: RawMetric) => {
  const badges = []
  if (metrics?.value?.[1]) {
    badges.push({ type: BadgeTypes.success, label: 'Up' })
  } else {
    badges.push({ type: BadgeTypes.error, label: 'Down' })
  }
  return badges
}
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/_transitionDataTable';
@use '@/styles/vars.scss';

.monitoring-policies-table-wrapper {
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
}
</style>
