import { LATENCY, REACHABILITY } from '@/components/ServicesInventory/serviceinventory.constants'
import { mapMetricsToClientFromServer, mapToClientFromServer } from '@/mappers/monitoredEntityState.mapper'
import { Pagination } from '@/types/alerts'
import { ServiceMetricsVariable, TimeRangeUnit } from '@/types/graphql'
import { MonitoredEntityState, MonitoredEntityStateMetrics } from '@/types/monitoredEntityState'
import { cloneDeep } from 'lodash'
import { defineStore } from 'pinia'
import { useServiceInventoryQueries } from '../Queries/serviceInventoryQueries'

type TState = {
  monitoredEntityStatesList: MonitoredEntityState[],
  pagination: Pagination,
  monitoredEntityStatesMetric: Map<string, MonitoredEntityStateMetrics>,
  loading: boolean,
}

const defaultPagination: Pagination = {
  page: 1, // FE pagination component has base 1 (first page)
  pageSize: 10,
  total: 0
}

export const useServiceInventoryStore = defineStore('serviceInventoryStore', {
  state: (): TState => ({
    monitoredEntityStatesList: [],
    pagination: cloneDeep(defaultPagination),
    monitoredEntityStatesMetric: new Map<string, MonitoredEntityStateMetrics>(),
    loading: false,
  }),
  actions: {
    async getMonitoredEntityStatesList(): Promise<MonitoredEntityState[]> {
      const { getMonitoredEntityStatesList, getServiceMetric } = useServiceInventoryQueries()
      this.pagination = cloneDeep(defaultPagination)
      const data = await getMonitoredEntityStatesList()
      this.pagination.total = data?.length ?? 0
      this.monitoredEntityStatesList = data?.map(mapToClientFromServer) ?? []
      try {
        await Promise.all(
          this.monitoredEntityStatesList.map(async (state) => {
            const variables: ServiceMetricsVariable = {
              monitoredEntityId: state.monitoredEntityId,
              metricName: LATENCY,
              timeRangeUnit: TimeRangeUnit.Hour,
              timeRange: 24
            }
            const latencyData = await getServiceMetric(variables)
            variables.metricName = REACHABILITY
            const reachabilityData = await getServiceMetric(variables)
            this.monitoredEntityStatesMetric.set(variables.monitoredEntityId, mapMetricsToClientFromServer(latencyData ?? {}, reachabilityData ?? {}))
          })
        )
      } catch (error) {
        console.error(error)
        throw error
      }
      return this.getPageObjects(this.monitoredEntityStatesList, this.pagination.page, this.pagination.pageSize)
    },
    // Function to retrieve objects for a given page
    getPageObjects(array: Array<MonitoredEntityState>, pageNumber: number, pageSize: number): Array<MonitoredEntityState> {
      const startIndex = (pageNumber - 1) * pageSize
      const endIndex = startIndex + pageSize
      return array.slice(startIndex, endIndex)
    },
    setMonitoredEntityStatePage(page: number): void {
      if (page !== Number(this.pagination.page)) {
        this.pagination = {
          ...this.pagination,
          page
        }
      }

      this.getMonitoredEntityStatesList()
    },
    setMonitoredEntityStatePageSize(pageSize: number): void {
      if (pageSize !== this.pagination.pageSize) {
        this.pagination = {
          ...this.pagination,
          page: 1, // always request first page on change
          pageSize
        }
      }

      this.getMonitoredEntityStatesList()
    }
  }
})
