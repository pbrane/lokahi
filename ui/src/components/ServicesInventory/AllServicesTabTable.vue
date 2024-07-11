<template>
  <div class="monitoring-policies-table-wrapper">
    <TableCard class="card">
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
            </tr>
          </thead>
          <TransitionGroup name="data-table" tag="tbody">
            <tr class="policies-table-row" v-for="node of tabContent" :key="node?.id">
              <td>{{ node.type }}</td>
              <td>{{ node.service }}</td>
              <td>{{ node.node }}</td>
              <td>{{ node.description }}</td>
              <td>
                <TextBadge :type="getBadgeType(node.reachability)">{{ node.reachability }}</TextBadge>
              </td>
              <td>
                <TextBadge :type="'indeterminate'">{{ node.uptime }}</TextBadge>
              </td>
              <td>
                <TextBadge :type="'indeterminate'">{{ node.latency }}</TextBadge>
              </td>
              <td class="actions-icons">
                <ServiceInventoryIconActions
                  :node="node"
                  nodeEdit="edit"
                  class="icon-action"
                  data-test="icon-action-list"
                />
              </td>
            </tr>
          </TransitionGroup>
        </table>
        <FeatherPagination :total="100" :page-size="10" :modelValue="1" />
      </div>
    </TableCard>
  </div>
</template>

<script setup lang="ts">
import {reactive, defineProps } from 'vue'
import { SORT } from '@featherds/table'
import TextBadge from '../Common/TextBadge.vue'
import { ServiceInventoryItem } from '@/types'

defineProps<{
  tabContent: ServiceInventoryItem[]
  columns: { id: string; label: string }[]
}>()

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

const getBadgeType = (value: string) => {
  const parsedValue = parseFloat(value)
  return parsedValue > 50 ? 'indeterminate' : 'critical'
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

  .feather-pagination {
    border: none !important;
  }
}
</style>
