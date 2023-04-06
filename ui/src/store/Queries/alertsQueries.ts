import { defineStore } from 'pinia'
import { useQuery } from 'villus'
import { AlertsDocument, CountAlertsDocument, TimeRange } from '@/types/graphql'
import { AlertsFilters, Pagination } from '@/types/alerts'

export const useAlertsQueries = defineStore('alertsQueries', () => {
  const fetchAlertsData = ref({})

  const fetchAlerts = async (alertsFilters: AlertsFilters, pagination: Pagination) => {
    const { data, execute, isFetching } = useQuery({
      query: AlertsDocument,
      variables: {
        page: pagination.page,
        pageSize: pagination.pageSize,
        // search: alertsFilters.search, // TODO: not avail for EAR
        severities: alertsFilters.severities,
        sortAscending: alertsFilters.sortAscending,
        sortBy: alertsFilters.sortBy,
        timeRange: alertsFilters.timeRange
      },
      fetchOnMount: false,
      cachePolicy: 'network-only'
    })

    await execute()

    if (!isFetching.value) {
      fetchAlertsData.value = data.value?.findAllAlerts || []
    }
  }

  const fetchCountAlerts = async (severityFilters = [] as string[], timeRange = TimeRange.All) =>
    useQuery({
      query: CountAlertsDocument,
      variables: {
        severityFilters,
        timeRange
      },
      fetchOnMount: false,
      cachePolicy: 'network-only'
    })

  return {
    fetchAlerts,
    fetchAlertsData,
    fetchCountAlerts
  }
})
