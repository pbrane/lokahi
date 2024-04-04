import { createAndDownloadBlobFile } from '@/components/utils'
import { FLOWS_ENABLED } from '@/constants'
import { useNodeStatusQueries } from '@/store/Queries/nodeStatusQueries'
import { AZURE_SCAN, DeepPartial } from '@/types'
import { AlertsFilters, AlertsSort, EventsFilters, Pagination } from '@/types/alerts'
import { DownloadCsvVariables, DownloadFormat, Exporter, ListAlertResponse, ListEventResponse, NodeUpdateInput, RequestCriteriaInput, TimeRange } from '@/types/graphql'
import { cloneDeep } from 'lodash'
import { defineStore } from 'pinia'
import { useNodeMutations } from '../Mutations/nodeMutations'

const alertsFilterDefault: AlertsFilters = {
  timeRange: TimeRange.All,
  nodeLabel: '',
  severities: [],
  sortAscending: true,
  sortBy: 'id',
  nodeId: 1
}

const eventsFilterDefault: EventsFilters = {
  searchTerm: '',
  sortAscending: true,
  sortBy: 'id',
  nodeId: 1
}

const defaultPagination: Pagination = {
  page: 1, // FE pagination component has base 1 (first page)
  pageSize: 10,
  total: 0
}


export const useNodeStatusStore = defineStore('nodeStatusStore', () => {
  const alertsFilter = ref(cloneDeep(alertsFilterDefault))
  const eventsFilter = ref(cloneDeep(eventsFilterDefault))
  const alertsPagination = ref(cloneDeep(defaultPagination))
  const eventsPagination = ref(cloneDeep(defaultPagination))
  const nodeStatusQueries = useNodeStatusQueries()
  const mutations = useNodeMutations()
  const fetchedData = computed(() => nodeStatusQueries.fetchedData)
  const fetchAlertsByNodeData = ref({} as ListAlertResponse)
  const fetchEventsByNodeData = ref({} as ListEventResponse)
  const exporters = ref<DeepPartial<Exporter>[]>([])
  const nodeId = ref()

  const setNodeId = (id: number) => {
    nodeStatusQueries.setNodeId(id)
    nodeId.value = id
  }

  const fetchExporters = async (id: number) => {
    // flows can be queried up to last 7 days.
    const now = new Date()
    const startTime = now.setDate(now.getDate() - 7)
    const endTime = Date.now()

    const payload: RequestCriteriaInput = {
      exporter: [{
        nodeId: id
      }],
      timeRange: {
        startTime,
        endTime
      }
    }

    if (FLOWS_ENABLED) {
      const data = await nodeStatusQueries.fetchExporters(payload)
      exporters.value = data.value?.findExporters || []
    } else {
      exporters.value = []
    }
  }

  // add the exporter object to the matching node snmp interface (match on ifIndex)
  // which is used to show if the interface is observing flows data
  const node = computed(() => {
    const node = nodeStatusQueries.fetchedData.node

    const azureInterfaces = new Map(node.azureInterfaces?.map((azureInterface: any) => {
      return [azureInterface.id, azureInterface]
    }))

    const snmpInterfaces = node.snmpInterfaces?.map((snmpInterface: any) => {
      for (const exporter of exporters.value) {
        if (exporter.snmpInterface?.ifIndex === snmpInterface.ifIndex) {
          return { ...snmpInterface, exporter }
        }
      }
      return { ...snmpInterface, exporter: {} }
    }) || []

    return { ...node, snmpInterfaces, azureInterfaces }
  })

  const updateNodeAlias = async (nodeAlias: string) => {
    const updateInput: NodeUpdateInput = {
      id: node.value.id,
      nodeAlias
    }

    await mutations.updateNode({ node: updateInput })
  }

  const downloadIpInterfacesToCsv = async (searchTerm: string) => {
    const downloadTopNQueryVariables: DownloadCsvVariables = {
      nodeId: nodeId.value,
      searchTerm: searchTerm,
      downloadFormat: DownloadFormat.Csv
    }
    const bytes = await nodeStatusQueries.downloadIpInterfaces(downloadTopNQueryVariables)
    createAndDownloadBlobFile(bytes, `${node.value.nodeLabel}-ip-interfaces.csv`)
  }

  const downloadSNMPInterfacesToCsv = async (searchTerm: string) => {
    const downloadTopNQueryVariables: DownloadCsvVariables = {
      nodeId: nodeId.value,
      searchTerm: searchTerm,
      downloadFormat: DownloadFormat.Csv
    }
    const bytes = await nodeStatusQueries.downloadSNMPInterfaces(downloadTopNQueryVariables)
    createAndDownloadBlobFile(bytes, `${node.value.nodeLabel}-snmp-interfaces.csv`)
  }

  const downloadAlertsByNodesToCsv = async () => {
    const page = alertsPagination.value.page > 0 ? alertsPagination.value.page - 1 : 0
    const pagination = {
      ...alertsPagination.value,
      page
    }

    const bytes = await nodeStatusQueries.downloadAlertsByNode(alertsFilter.value, pagination, { downloadFormat: DownloadFormat.Csv })
    const filename = `${node.value.nodeLabel}-recent-alerts.csv`
    createAndDownloadBlobFile(bytes || [], filename)
  }

  const downloadEvents = async (searchTerm: string) => {
    const queryVariables: DownloadCsvVariables = {
      nodeId: nodeId.value,
      searchTerm: searchTerm || '',
      downloadFormat: DownloadFormat.Csv
    }
    const bytes = await nodeStatusQueries.downloadEvents(queryVariables)
    const fileName = `${node.value.nodeLabel}-events.csv`
    createAndDownloadBlobFile(bytes, fileName)
  }

  const getAlertsByNode = async () => {
    const page = alertsPagination.value.page > 0 ? alertsPagination.value.page - 1 : 0
    const pagination = {
      ...alertsPagination.value,
      page
    }
    await nodeStatusQueries.getAlertsByNodeQuery(alertsFilter.value, pagination)
    fetchAlertsByNodeData.value = nodeStatusQueries.fetchAlertsByNodeData
    if (fetchAlertsByNodeData.value.totalAlerts !== alertsPagination.value.total) {
      alertsPagination.value = {
        ...alertsPagination.value,
        total: fetchAlertsByNodeData.value.totalAlerts
      }
    }
  }

  const getEventsByNode = async () => {
    const page = eventsPagination.value.page > 0 ? eventsPagination.value.page - 1 : 0
    const newPage = {
      page: page,
      pageSize: eventsPagination.value.pageSize,
      total: eventsPagination.value.total
    }
    eventsFilter.value.nodeId = nodeId.value
    await nodeStatusQueries.getEventsByNodeQuery(eventsFilter.value, newPage)
    fetchEventsByNodeData.value = nodeStatusQueries.fetchedEventsByNodeData
    if (fetchEventsByNodeData.value.totalEvents !== eventsPagination.value.total) {
      eventsPagination.value = {
        page: eventsPagination.value.page,
        pageSize: eventsPagination.value.pageSize,
        total: fetchEventsByNodeData.value.totalEvents
      }
    }
  }

  const setAlertsByNodePage = (page: number): void => {
    if (page !== Number(alertsPagination.value.page)) {
      alertsPagination.value = {
        ...alertsPagination.value,
        page
      }
    }

    getAlertsByNode()
  }

  const setEventsByNodePage = (page: number): void => {
    if (page !== Number(eventsPagination.value.page)) {
      eventsPagination.value = {
        ...eventsPagination.value,
        page
      }
    }

    getEventsByNode()
  }

  const setAlertsByNodePageSize = (pageSize: number): void => {
    if (pageSize !== alertsPagination.value.pageSize) {
      alertsPagination.value = {
        ...alertsPagination.value,
        page: 1, // always request first page on change
        pageSize
      }
    }

    getAlertsByNode()
  }

  const setEventsByNodePageSize = (pageSize: number): void => {
    if (pageSize !== eventsPagination.value.pageSize) {
      eventsPagination.value = {
        ...eventsPagination.value,
        page: 1, // always request first page on change
        pageSize
      }
    }

    getEventsByNode()
  }

  const alertsByNodeSortChanged = (sortObj: AlertsSort) => {

    alertsFilter.value = {
      ...alertsFilter.value,
      sortBy: sortObj.sortBy,
      sortAscending: sortObj.sortAscending
    }

    if (alertsPagination.value.page !== 1 || alertsPagination.value.total !== fetchAlertsByNodeData.value.totalAlerts) {
      alertsPagination.value = {
        ...alertsPagination.value,
        page: 1, // always request first page on change
        total: fetchAlertsByNodeData.value.totalAlerts
      }
    }

    getAlertsByNode()
  }

  const eventsSortChanged = (sortObj: AlertsSort) => {
    eventsFilter.value = {
      ...eventsFilter.value,
      sortBy: sortObj.sortBy,
      sortAscending: sortObj.sortAscending,
      nodeId: nodeId.value
    }

    if (eventsPagination.value.page !== 1 || eventsPagination.value.total !== fetchEventsByNodeData.value.totalEvents) {
      eventsPagination.value = {
        ...eventsPagination.value,
        page: 1, // always request first page on change
        total: fetchEventsByNodeData.value.totalEvents
      }
    }

    getEventsByNode()
  }

  const eventsSearchChanged = (searchTerm: string) => {
    eventsFilter.value.searchTerm = searchTerm
    getEventsByNode()
  }

  return {
    updateNodeAlias,
    fetchedData,
    setNodeId,
    isAzure: computed(() => fetchedData.value.node.scanType === AZURE_SCAN),
    fetchExporters,
    exporters,
    node,
    nodeId,
    downloadIpInterfacesToCsv,
    downloadSNMPInterfacesToCsv,
    getAlertsByNode,
    getEventsByNode,
    fetchAlertsByNodeData,
    fetchEventsByNodeData,
    alertsPagination,
    eventsPagination,
    setAlertsByNodePageSize,
    setEventsByNodePageSize,
    setEventsByNodePage,
    setAlertsByNodePage,
    alertsByNodeSortChanged,
    eventsSortChanged,
    eventsSearchChanged,
    downloadAlertsByNodesToCsv,
    downloadEvents
  }
})
