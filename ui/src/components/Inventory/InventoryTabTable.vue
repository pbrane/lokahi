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
                v-on:sort-changed="sortChanged"
              >
                {{ col.label }}
              </FeatherSortHeader>
              <th/>
            </tr>
          </thead>
            <TransitionGroup name="data-table" tag="tbody">
              <tr class="policies-table-row" v-for="node of tabContent">
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
                  <div @click.prevent="editNode" class="icon">
                    <Icon :icon="editIcon" />
                  </div>
                  <div @click.prevent="removeNode" class="icon">
                    <Icon :icon="removeIcon" />
                  </div>
                </td>
              </tr>
            </TransitionGroup>
          </table>
        </div>
      </TableCard>
    </div>
    <AlertRulesDrawer/>
  </template>

<script setup lang="ts">
import { IIcon, InventoryItem, RawMetric, MonitoredStates } from '@/types'
import Delete from '@featherds/icon/action/Delete'
import Edit from '@featherds/icon/action/Edit'
import { PropType } from 'vue'
import { SORT } from '@featherds/table'
import { BadgeTypes } from '../Common/commonTypes'
import TextBadge from '../Common/TextBadge.vue'

const removeIcon: IIcon = { image: markRaw(Delete), tooltip: 'Alert Rule Delete', size: 1.5, cursorHover: true }
const editIcon: IIcon = { image: markRaw(Edit), tooltip: 'Alert Rule Edit', size: 1.5, cursorHover: true }

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

const sort = reactive({
  NodeName: SORT.ASCENDING,
  ManagementIP: SORT.NONE,
  Tags: SORT.NONE,
  MonitoringLocation: SORT.NONE,
  Latency: SORT.NONE,
  Uptime: SORT.NONE,
  Status: SORT.NONE
}) as any

const columns: { id: string; label: string }[] = [
  { id: 'NodeName', label: 'Node Name' },
  { id: 'ManagementIP', label: 'Management IP' },
  { id: 'Tags', label: 'Tags' },
  { id: 'MonitoringLocation', label: 'Monitoring Location' },
  { id: 'Latency', label: 'Latency' },
  { id: 'Uptime', label: 'Uptime' },
  { id: 'Status', label: 'Status' }
]

const editNode = () => console.log('edit ')
const removeNode = () => console.log('remove inventory')
const sortChanged = (sortObj: Record<string, string>) => console.log('sort inventory')

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
        .actions-icons {
          display: flex;
          justify-content: flex-start;
          align-items: center;
          gap: 5px;
          min-height: 52px;
          cursor: pointer;
          .icon {
            color: var(--feather-primary);
            :deep(:focus) {
              outline: none;
           }
          }
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
