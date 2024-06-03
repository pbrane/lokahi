import { AlertsFilters, EventsFilters, Pagination, Variables } from '@/types/alerts'
import { AlertsByNodeDocument, DownloadAlertsByNodeDocument, DownloadCSVEventVariables, DownloadCsvVariables, DownloadEventsDocument, DownloadFormat, DownloadIpInterfacesDocument, DownloadSNMPInterfacesDocument, FindExportersForNodeStatusDocument, ListAlertResponse, ListEventResponse, ListNodeEventsDocument, ListNodeStatusDocument, Node, RequestCriteriaInput } from '@/types/graphql'
import { defineStore } from 'pinia'
import { useQuery } from 'villus'
import { defaultListAlertResponse } from './alertsQueries'

export const DefaultEventListResponse = {
  eventsList: [],
  lastPage: 0,
  nextPage: 0,
  totalEvents: 0
} as ListEventResponse

export const useNodeStatusQueries = defineStore('nodeStatusQueries', () => {
  const variables = ref<Variables>({})
  const fetchAlertsByNodeData = ref({} as ListAlertResponse)
  const fetchedEventsByNodeData = ref({} as ListEventResponse)
  const setNodeId = (id: number) => {
    variables.value = { id }
  }

  const { data, execute: fetchNodeStatus } = useQuery({
    query: ListNodeStatusDocument,
    variables,
    cachePolicy: 'network-only'
  })

  const fetchedData = computed(() => ({
    node: data.value?.node || ({} as Node)
  }))

  const fetchExporters = async (requestCriteria: RequestCriteriaInput) => {
    const { execute, data } = useQuery({
      query: FindExportersForNodeStatusDocument,
      variables: {
        requestCriteria
      },
      cachePolicy: 'network-only'
    })
    await execute()
    return data
  }

  const getAlertsByNodeQuery = async (sortFilter: AlertsFilters, paginationFilter: Pagination) => {
    const { data, execute } = useQuery({
      query: AlertsByNodeDocument,
      variables: {
        page: paginationFilter.page,
        pageSize: paginationFilter.pageSize,
        sortBy: sortFilter.sortBy,
        sortAscending: sortFilter.sortAscending,
        nodeId: variables.value.id

      },
      cachePolicy: 'network-only'
    })
    await execute()

    if (data?.value?.getAlertsByNode) {
      fetchAlertsByNodeData.value = {...data?.value?.getAlertsByNode } as ListAlertResponse
    } else {
      fetchAlertsByNodeData.value = defaultListAlertResponse()
    }
  }

  const getEventsByNodeQuery = async (sortFilter: EventsFilters, paginationFilter: Pagination) => {
    const { data, execute } = useQuery({
      query: ListNodeEventsDocument,
      variables: {
        page: paginationFilter.page,
        pageSize: paginationFilter.pageSize,
        sortBy: sortFilter.sortBy,
        sortAscending: sortFilter.sortAscending,
        nodeId: sortFilter.nodeId,
        searchTerm: sortFilter.searchTerm,
        downloadFormat: DownloadFormat.Csv
      },
      cachePolicy: 'network-only'
    })
    await execute()

    if (data.value?.searchEvents) {
      fetchedEventsByNodeData.value = {...data.value.searchEvents}
    } else {
      fetchedEventsByNodeData.value = DefaultEventListResponse
    }
  }

  const downloadIpInterfaces = async (requestCriteria: DownloadCsvVariables) => {
    const { execute, data } = useQuery({
      query: DownloadIpInterfacesDocument,
      variables: requestCriteria,
      cachePolicy: 'network-only',
      fetchOnMount: false
    })
    await execute()
    return data.value?.downloadIpInterfacesByNodeAndSearchTerm?.responseBytes
  }

  const downloadSNMPInterfaces = async (requestCriteria: DownloadCsvVariables) => {
    const { execute, data } = useQuery({
      query: DownloadSNMPInterfacesDocument,
      variables: requestCriteria,
      cachePolicy: 'network-only',
      fetchOnMount: false
    })
    await execute()
    return data.value?.downloadSnmpInterfaces?.responseBytes
  }

  const downloadAlertsByNode = async ({sortBy, sortAscending}: AlertsFilters, {page, pageSize}: Pagination, downloadFormat: DownloadCsvVariables) => {
    const { execute, data } = useQuery({
      query: DownloadAlertsByNodeDocument,
      variables: {
        nodeId: variables.value.id,
        sortAscending,
        page,
        pageSize,
        sortBy,
        downloadFormat: downloadFormat.downloadFormat
      },
      cachePolicy: 'network-only',
      fetchOnMount: false
    })
    await execute()
    return data.value?.downloadRecentAlertsByNode?.responseBytes
  }

  const downloadEvents = async (requestCriteria: DownloadCSVEventVariables) => {
    const { execute, data } = useQuery({
      query: DownloadEventsDocument,
      variables: requestCriteria,
      cachePolicy: 'network-only',
      fetchOnMount: false
    })
    await execute()

    return data.value?.downloadEventsByNodeId?.responseBytes
  }

  return {
    setNodeId,
    fetchedData,
    fetchExporters,
    fetchNodeStatus,
    downloadIpInterfaces,
    downloadSNMPInterfaces,
    getAlertsByNodeQuery,
    fetchAlertsByNodeData,
    fetchedEventsByNodeData,
    downloadAlertsByNode,
    getEventsByNodeQuery,
    downloadEvents
  }
})
