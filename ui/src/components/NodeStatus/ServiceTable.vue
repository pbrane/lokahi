<template>
    <div class="container">
      <table class="data-table" aria-label="Windows Service Table">
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
              <th>Description</th>
              <th v-if="serviceType === ServiceType.NotRunning || serviceType === ServiceType.Error">Date Last Run</th>
              <th>CPU Usage</th>
              <th>Memory Usage</th>
              <th>Actions</th>
          </tr>
        </thead>
        <TransitionGroup
          name="data-table"
          tag="tbody"
        >
          <tr
            v-for="obj in pageObjects"
            :key="obj.id"
          >
            <td>{{ obj.service }}</td>
            <td>{{ obj.description }}</td>
            <td v-if="serviceType === ServiceType.NotRunning || serviceType === ServiceType.Error">{{ obj.lastRun }}</td>
            <td>{{ obj.cpuUsage }}</td>
            <td>{{ obj.memoryUsage }}</td>
            <td>
              <div class="action-template">
                <FeatherButton
                  primary
                  icon="Edit"
                  @click.stop="editService(obj)"
                >
                  <FeatherIcon :icon="AddNote"/>
                </FeatherButton>
              </div>
            </td>
          </tr>
        </TransitionGroup>
      </table>
      <div v-if="!dummyObjects.length">
        <EmptyList
          :content="emptyListContent"
          data-test="empty-list"
        />
      </div>
      <FeatherPagination
        v-model="servicesPagination.page"
        :pageSize="servicesPagination.pageSize"
        :total="servicesPagination.total"
        @update:modelValue="updatePage"
        @update:pageSize="updatePageSize"
        data-test="pagination"
        v-if="dummyObjects.length"
      />
    </div>
</template>

<script lang="ts" setup>
import { Pagination } from '@/types/alerts'
import AddNote from '@featherds/icon/action/AddNote'
import { SORT } from '@featherds/table'
import { cloneDeep, sortBy } from 'lodash'
import { ServiceType } from '../utils'

const emptyListContent = {
  msg: 'No results found.'
}

const dummyObjects = [
  {
    id: 1,
    service: 'wuauserv',
    description: 'Manages the downloading and installation of Windows updates',
    lastRun: '2024-04-12 01:01:00 UTC',
    cpuUsage: '4%',
    memoryUsage: '12 MB'
  },
  {
    id: 2,
    service: 'WinDefend',
    description: 'Provides antivirus and malware protection for Windows systems',
    lastRun: '2024-04-12 01:01:00 UTC',
    cpuUsage: '18%',
    memoryUsage: '60 MB'
  },
  {
    id: 3,
    service: 'MSSQLSERVER',
    description: 'Manages the SQL Server database engine and associated services',
    lastRun: '2024-04-12 01:01:00 UTC',
    cpuUsage: '2.8%',
    memoryUsage: '9 MB'
  },
  {
    id: 4,
    service: 'W32Time',
    description: 'Synchronizes the date and time for all clients and servers in the network',
    lastRun: '2024-04-12 01:01:00 UTC',
    cpuUsage: '0.4%',
    memoryUsage: '0.97 MB'
  },
  {
    id: 5,
    service: 'DHCP',
    description: 'Manages network configuration by automatically obtaining IP addresses',
    lastRun: '2024-04-12 01:01:00 UTC',
    cpuUsage: '9%',
    memoryUsage: '42 MB'
  },
  {
    id: 6,
    service: 'Dnscache',
    description: 'Resolves and caches Domain Name System (DNS) names for the computer',
    lastRun: '2024-04-12 01:01:00 UTC',
    cpuUsage: '3%',
    memoryUsage: '7 MB'
  },
  {
    id: 7,
    service: 'EventLog',
    description: 'Manages event logs, allowing applications and the operating system',
    lastRun: '2024-04-12 01:01:00 UTC',
    cpuUsage: '6%',
    memoryUsage: '18 MB'
  }
]

const defaultPagination: Pagination = {
  page: 1, // FE pagination component has base 1 (first page)
  pageSize: 10,
  total: 0
}

const servicesPagination = ref(cloneDeep(defaultPagination))
const pageObjects = ref([] as any[])
const clonedServices = ref([] as any[])
defineProps<{
  serviceType: ServiceType
}>()

const updateServices = () => {
  if (dummyObjects.length) {
    servicesPagination.value.total = dummyObjects.length
    clonedServices.value = dummyObjects
    pageObjects.value = getPageObjects(clonedServices.value, servicesPagination.value.page, servicesPagination.value.pageSize)
  } else {
    pageObjects.value = []
  }
}
onMounted(() => {
  updateServices()
})

// Function to retrieve objects for a given page
const getPageObjects = (array: Array<any>, pageNumber: number, pageSize: number) => {
  const startIndex = (pageNumber - 1) * pageSize
  const endIndex = startIndex + pageSize
  return array.slice(startIndex, endIndex)
}

const sort: Record<string, string>  = reactive({
  service: SORT.NONE
})
const columns = [
  { id: 'service', label: 'Service' }
]
const sortChanged = (sortObj: Record<string, string>) => {
  if (sortObj.value === 'asc') {
    clonedServices.value = sortBy(dummyObjects, sortObj.property)
  }
  if (sortObj.value === 'desc') {
    clonedServices.value = sortBy(dummyObjects, sortObj.property).reverse()
  }
  if (sortObj.value === 'none') {
    clonedServices.value = sortBy(dummyObjects, 'id')
  }

  servicesPagination.value.page = 1
  pageObjects.value = getPageObjects(clonedServices.value, servicesPagination.value.page, servicesPagination.value.pageSize)
  for (const prop in sort) {
    sort[prop] = SORT.NONE
  }
  sort[sortObj.property] = sortObj.value
}
const editService = (obj: any) => console.log('Service Edit', obj)
const updatePage = (v: number) => {
  if (dummyObjects.length) {
    servicesPagination.value.page = v
    pageObjects.value = getPageObjects(clonedServices.value, v, servicesPagination.value.pageSize)
  }
}
const updatePageSize = (v: number) => {
  if (dummyObjects.length) {
    servicesPagination.value.pageSize = v
    pageObjects.value = getPageObjects(clonedServices.value, servicesPagination.value.page, v)
  }
}
</script>

<style lang="scss" scoped>
@use "@featherds/styles/themes/variables";
@use "@featherds/styles/mixins/typography";
@use "@featherds/table/scss/table";
@use "@/styles/_transitionDataTable";

.container {
  display: block;
  overflow-x: auto;

  table {
    width: 100%;
    padding: 1rem 0;
    @include table.table;
    thead {
      background: var(variables.$background);
      text-transform: uppercase;
    }
    td:has(.description) {
      width: 30% !important;
      padding: 5px 0px;

      .description {
        padding: 10px 0px;
      }
    }
  }
}

.action-template {
  display: flex;
  align-items: center;
  gap: 1rem;
}
</style>
