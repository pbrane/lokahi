<template>
  <TableCard>
   <div>
    <section class="feather-row">
      <h3 data-test="heading" class="feather-col-6 title">Latest Events</h3>
      <div class="btns action-container feather-col-6">
        <div class="search-container">
          <FeatherInput
            :label="searchLabel"
            v-model.trim="searchEvents"
            type="search"
            data-test="search-input"
            @update:model-value="onSearchChanged"
          >
            <template #pre>
              <FeatherIcon :icon="icons.Search" />
            </template>
          </FeatherInput>
        </div>
            <FeatherButton
              primary
              icon="Download"
            >
              <FeatherIcon :icon="icons.DownloadFile" @click.prevent = "download"/>
            </FeatherButton>
            <FeatherButton
              primary
              icon="Refresh"
              @click="refresh"
              >
              <FeatherIcon :icon="icons.Refresh"/>
            </FeatherButton>
          </div>
    </section>

    <div class="container">
      <table class="data-table" aria-label="Events Table" data-test="data-table">
        <thead>
          <tr>
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
          </tr>
        </thead>
        <TransitionGroup name="data-table" tag="tbody" v-if="hasEvents && nodeStatusStore.eventsPagination.total">
          <tr v-for="event in eventSearchedData" :key="event.id as number" data-test="data-item">
            <td>{{ fnsFormat(event.producedTime, 'M/dd/yyyy HH:mm:ssxxx') }}</td>
            <td>{{ event.uei }}</td>
            <td>{{ event.ipAddress }}</td>
          </tr>
        </TransitionGroup>
      </table>
      <div v-if="!hasEvents || nodeStatusStore.eventsPagination.total === 0">
        <EmptyList
          :content="emptyListContent"
          data-test="empty-list"
        />
      </div>
    </div>
        <FeatherPagination
          v-model="nodeStatusStore.eventsPagination.page"
          :pageSize="nodeStatusStore.eventsPagination.pageSize"
          :total="nodeStatusStore.eventsPagination.total"
          @update:modelValue="onPageChanged"
          @update:pageSize="onPageSizeChanged"
          data-test="pagination"
          v-if="hasEvents && nodeStatusStore.eventsPagination.total"
        />
  </div>
  </TableCard>
</template>

<script lang="ts" setup>
import DownloadFile from '@featherds/icon/action/DownloadFile'
import Refresh from '@featherds/icon/navigation/Refresh'
import { useNodeStatusStore } from '@/store/Views/nodeStatusStore'
import { format as fnsFormat } from 'date-fns'
import { SORT } from '@featherds/table'
import Search from '@featherds/icon/action/Search'
import { Event } from '@/types/graphql'
import useSpinner from '@/composables/useSpinner'

const { startSpinner, stopSpinner } = useSpinner()
const nodeStatusStore = useNodeStatusStore()
const searchEvents = ref('')
const eventSearchedData = ref([] as Event[])
const searchLabel = 'Search Events'
const icons = markRaw({
  DownloadFile,
  Refresh,
  Search
})
const columns = [
  { id: 'producedTime', label: 'Time' },
  { id: 'eventUei', label: 'UEI' },
  { id: 'ipAddress', label: 'IP Address' }
]
const emptyListContent = {
  msg: 'No results found.'
}
const sort: Record<string, string>  = reactive({
  producedTime: SORT.NONE,
  eventUei: SORT.NONE,
  ipAddress: SORT.NONE
})
const eventData = computed(() => {
  const events = nodeStatusStore.fetchEventsByNodeData.eventsList as Event[] || ([] as Event[])
  return {
    events
  }
})
const hasEvents = computed(() => eventData.value.events.length > 0)
const updateEvents = () => {
  if (hasEvents.value) {
    eventSearchedData.value = [...eventData.value.events] as Event[]
  }
}
const fetchEventsByNodeList = async () => {
  startSpinner()
  await nodeStatusStore.getEventsByNode()
  stopSpinner()
}
onMounted(async () => {
  await fetchEventsByNodeList()
})
watch(() => eventData.value, () => {
  updateEvents()
})
const onPageChanged = (v: number) => {
  startSpinner()
  nodeStatusStore.setEventsByNodePage(v)
  stopSpinner()
}
const onPageSizeChanged = (v: number) => {
  startSpinner()
  nodeStatusStore.setEventsByNodePageSize(v)
  stopSpinner()
}
const sortChanged = (sortObj: Record<string, string>) => {
  startSpinner()
  if (sortObj.value === 'asc' || sortObj.value === 'desc') {
    const sortAscending  = sortObj.value === 'asc' ? true : false
    nodeStatusStore.eventsSortChanged({ sortAscending, sortBy: sortObj.property })

  } else {
    const sortAscending  = true
    const sortByAlerts = { sortAscending, sortBy: 'id' }
    nodeStatusStore.eventsSortChanged(sortByAlerts)
  }

  for (const prop in sort) {
    sort[prop] = SORT.NONE
  }
  sort[sortObj.property] = sortObj.value
  stopSpinner()
}
const onSearchChanged = (searchTerm: any) => {
  startSpinner()
  nodeStatusStore.eventsSearchChanged(searchTerm)
  stopSpinner()
}
const download = () => {
  startSpinner()
  nodeStatusStore?.downloadEvents(searchEvents.value)
  stopSpinner()
}
const refresh = async () => {
  nodeStatusStore.eventsPagination.page = 1
  nodeStatusStore.eventsPagination.pageSize = 10
  fetchEventsByNodeList()
}
</script>

<style lang="scss" scoped>
@use "@featherds/styles/themes/variables";
@use "@featherds/styles/mixins/typography";
@use "@featherds/table/scss/table";
@use "@/styles/_transitionDataTable";

.main-section {
  padding: 0 var(--spacing-s) var(--spacing-l) var(--spacing-s);
}

.feather-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1rem 0 0.5rem;

  .title {
    margin: 0 0 2.5rem 1rem;
  }
  .action-container {
    display: flex;
    justify-content: flex-end;
    gap: 5px;
    width: 30%;

    > .search-container {
      width: 80%;
      margin-right: 3px;

      :deep(.label-border) {
        width: 110px !important;
      }
    }
  }
}

.container {
  display: block;
  overflow-x: auto;
  table {
    width: 100%;
    padding: 0 1.5rem 1.5rem 1.5rem;
    @include table.table;
    thead {
      background: var(variables.$background);
      text-transform: uppercase;
    }
    td {
      white-space: nowrap;
      div {
        border-radius: 5px;
        padding: 0px 5px 0px 5px;
      }
    }
  }
}
.feather-pagination {
  border: none;
  padding: 20px 0px;
  :deep(.per-page-text), :deep(.page-size-select) {
    display: block !important;
   }
}
</style>
