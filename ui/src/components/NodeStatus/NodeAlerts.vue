<template>
  <div class="main-section">
    <section class="feather-row">
      <h3 data-test="heading" class="feather-col-6">Recent Alerts</h3>
      <div class="btns feather-col-6">
        <FeatherButton primary icon="Download">
          <FeatherIcon :icon="icons.DownloadFile" @click.prevent="nodeStatusStore.downloadAlertsByNodesToCsv" />
        </FeatherButton>
        <FeatherButton primary icon="Refresh" @click="fetchAlertsByNodeList">
          <FeatherIcon :icon="icons.Refresh" />
        </FeatherButton>
      </div>
    </section>
    <section class="node-component-content">
      <TableCard>
        <div class="container">
          <table class="data-table" aria-label="Recent Alerts Table" data-test="data-table">
            <thead>
              <tr>
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
                <th />
                <th />
              </tr>
            </thead>
            <TransitionGroup name="data-table" tag="tbody" v-if="hasAlerts">
              <tr
                v-for="alert in alertsData"
                :key="alert?.databaseId as string"
                class="alert-table-row"
                data-test="data-item"
              >
                <td class="alert-details-wrapper">
                  <div class="name headline alert-type" data-test="name">{{ alert?.type || alert?.label || 'Unknown' }}</div>
                  <div v-if="isRowExpanded(alert.databaseId)">
                    <div>Location: {{ alert.location }}</div>
                    <div>Description: {{ alert.description }}</div>
                    <div>Started: {{ fnsFormat(alert.firstEventTimeMs, 'HH:mm:ssxxx') }}</div>
                    <div>RuleName: {{ alert.ruleNameList?.join(', ') }}</div>
                    <div>PolicyName: {{ alert.policyNameList?.join(', ') }}</div>
                  </div>
                </td>
                <td>
                  <PillColor :item="showSeverity(alert?.severity)" data-test="severity-label" />
                </td>
                <td class="date-time">
                  <div class="date headline" data-test="date">
                    <span>{{ fnsFormat(alert.lastUpdateTimeMs, 'M/dd/yyyy') }}</span>
                  </div>
                  <div class="time" data-test="time">
                    <span>{{ fnsFormat(alert.lastUpdateTimeMs, 'HH:mm:ssxxx') }}</span>
                  </div>
                </td>
                <td>
                  <div class="check-circle">
                    <FeatherTooltip :title="alert.acknowledged ? Ack : UnAck" v-slot="{ attrs, on }">
                      <FeatherIcon
                        v-bind="attrs"
                        v-on="on"
                        :icon="icons.CheckCircle"
                        :class="{ acknowledged: alert.acknowledged }"
                        class="acknowledged-icon"
                        data-test="check-icon"
                      />
                    </FeatherTooltip>
                  </div>
                </td>
                <td>
                  <a href="#" class="expand-wrapper" @click.prevent="() => onExpandRow(alert.databaseId)">
                    <FeatherIcon :icon="icons.ArrowDropDown" :class="{ invertArrow: expandedIds.has(alert.databaseId) }" />
                  </a>
                </td>
              </tr>
            </TransitionGroup>
          </table>
          <div v-if="!hasAlerts || total === 0">
            <EmptyList :content="emptyListContent" data-test="empty-list" />
          </div>
        </div>
        <div>
          <div class="alert-list-bottom" v-if="hasAlerts">
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
        </div>
      </TableCard>
    </section>
  </div>
</template>

<script lang="ts" setup>
import { format as fnsFormat } from 'date-fns'
import CheckCircle from '@featherds/icon/action/CheckCircle'
import DownloadFile from '@featherds/icon/action/DownloadFile'
import ArrowDropDown from '@featherds/icon/navigation/ArrowDropDown'
import ExpandLess from '@featherds/icon/navigation/ExpandLess'
import ExpandMore from '@featherds/icon/navigation/ExpandMore'
import Refresh from '@featherds/icon/navigation/Refresh'
import { SORT } from '@featherds/table'
import useSpinner from '@/composables/useSpinner'
import { useNodeStatusStore } from '@/store/Views/nodeStatusStore'
import { IAlert } from '@/types/alerts'
import { Ack, UnAck } from '../Alerts/alerts.constants'

const icons = markRaw({
  ArrowDropDown,
  CheckCircle,
  DownloadFile,
  ExpandLess,
  ExpandMore,
  Refresh
})

const { startSpinner, stopSpinner } = useSpinner()
const nodeStatusStore = useNodeStatusStore()

const alertsData = ref<IAlert[]>([])
const expandedIds = ref(new Set<number>())

const columns = [
  { id: 'type', label: 'Alert Type' },
  { id: 'severity', label: 'Severity' },
  { id: 'lastEventTime', label: 'Time' }
]

const emptyListContent = {
  msg: 'No results found.'
}

const sort = reactive({
  alertType: SORT.NONE,
  severity: SORT.NONE,
  data: SORT.NONE,
  time: SORT.ASCENDING
}) as any

const page = computed(() => nodeStatusStore.alertsPagination.page || 1)
const pageSize = computed(() => nodeStatusStore.alertsPagination.pageSize)
const total = computed(() => nodeStatusStore.alertsPagination.total)
const data = computed(() => nodeStatusStore.fetchAlertsByNodeData || [])

const hasAlerts = computed(() => {
  return (data.value?.alerts || []).length > 0
})

const isRowExpanded = (id: any) => {
  const num = id ? Number(id) : 0

  return num && expandedIds.value.has(num)
}

const onExpandRow = (id: any) => {
  const num = id ? Number(id) : 0

  if (num) {
    if (expandedIds.value.has(num)) {
      expandedIds.value.delete(num)
    } else {
      expandedIds.value.add(num)
    }
  }
}

const sortChanged = (columnId: string, sortObj: Record<string, string>) => {
  startSpinner()

  if (sortObj.value === 'asc' || sortObj.value === 'desc') {
    const sortAscending = sortObj.value === 'asc'
    const sortByAlerts = { sortAscending, sortBy: columnId }
    nodeStatusStore.alertsByNodeSortChanged(sortByAlerts)
  } else {
    const sortAscending = true
    const sortByAlerts = { sortAscending, sortBy: 'id' }
    nodeStatusStore.alertsByNodeSortChanged(sortByAlerts)
  }

  for (const prop in sort) {
    sort[prop] = SORT.NONE
  }
  sort[sortObj.property] = sortObj.value
}

const showSeverity = (value: any) => ({ style: value as string })

const fetchAlertsByNodeList = async () => {
  startSpinner()
  expandedIds.value.clear()
  await nodeStatusStore.getAlertsByNode()
  stopSpinner()
}

const onPageChanged = (p: number) => {
  startSpinner()
  nodeStatusStore.setAlertsByNodePage(p)
}

const onPageSizeChanged = (p: number) => {
  startSpinner()
  nodeStatusStore.setAlertsByNodePageSize(p)
}

onMounted(() => {
  fetchAlertsByNodeList()
})

watch(() => nodeStatusStore.fetchAlertsByNodeData, () => {
  if (hasAlerts.value) {
    const alerts = data.value?.alerts || []
    alertsData.value = [...alerts]
  }
  stopSpinner()
})
</script>

<style lang="scss" scoped>
@use "@featherds/styles/themes/variables";
@use "@/styles/vars";
@use "@/styles/mediaQueriesMixins";
@use "@featherds/styles/mixins/typography";
@use "@featherds/table/scss/table";
@use "@/styles/_transitionDataTable";

.headline {
  font-size: 1rem;
  font-weight: 600;
}

.container table tr.alert-table-row {
  vertical-align: top;

  td {
    padding-bottom: 0.3rem;
    padding-top: 0.3rem;

    &.alert-details-wrapper {
      width: 18rem;
    }
  }
}

.expand-wrapper {
  font-size: 2em;
}

.invert-arrow {
  transform: scaleY(-1);
}

.main-section {
  padding: 0 var(variables.$spacing-s) 0 var(variables.$spacing-s);
}

.feather-row {
  margin-bottom: var(variables.$spacing-s);
  display: flex;
  flex-direction: row;
  gap: 0.5rem;
  align-items: center;
  justify-content: space-between;
  padding-left: 0.9rem;
}

.node-component-label {
  margin: 0;
  line-height: 20px;
  letter-spacing: 0.28px;
}

.node-component-content {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  gap: 2rem;
}

.container {
  display: block;
  table {
    width: 100%;
    @include table.table;
    thead {
      background: var(variables.$background);
      text-transform: uppercase;
    }
    tr {
      box-shadow: 0 1px 0 0 var(--feather-border-on-surface);

      .date-time {
        width: 15%;
      }

      td {
        white-space: nowrap;
        box-shadow: none;
        margin-top: var(variables.$spacing-s);

        .description,
        .alert-type {
          margin-top: var(variables.$spacing-s);
        }
      }
    }
  }
}

.check-circle {
  width: 4%;

  .acknowledged-icon {
    width: 1.5rem;
    height: 1.5rem;
    color: var(variables.$shade-3);

    &.acknowledged {
      color: var(variables.$success);
    }
  }
}

.alert-list-bottom {
  display: flex;
  flex-direction: row;
  justify-content: flex-end;
  align-items: center;
  padding: var(variables.$spacing-m) 0 var(variables.$spacing-s) var(variables.$spacing-s);

  :deep(> .feather-pagination) {
    border: 0;
    min-height: auto;
  }
}

.feather-expansion {
  box-shadow: none;
  background-color: unset;

  :deep(.feather-expansion-header-button) {
    height: auto;
    padding: 0;

    &.expanded {
      height: auto;
    }
  }

  :deep(.panel-content) {
    padding: 1rem 0 0 0 !important;
    width: 90%;
  }
}
</style>
