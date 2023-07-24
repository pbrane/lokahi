import { useQuery } from 'villus'
import { defineStore } from 'pinia'
import {
  NodeLatencyMetricDocument,
  TsResult,
  TimeRangeUnit,
  ListTagsByNodeIdsDocument,
  FindAllNodesByNodeLabelSearchDocument,
  Node,
  FindAllNodesByTagsDocument,
  FindAllNodesByMonitoredStateDocument,
  NodeTags,
  NodeLatencyMetricQuery, MonitoredState, FindAllNodesByNodeLabelSearchQueryVariables, FindAllNodesByTagsQueryVariables
} from '@/types/graphql'
import useSpinner from '@/composables/useSpinner'
import { DetectedNode, Monitor, MonitoredNode, UnmonitoredNode, AZURE_SCAN } from '@/types'

export const useInventoryQueries = defineStore('inventoryQueries', () => {
  const selectedState = ref(MonitoredState.Monitored)
  const monitoredNodes = ref<MonitoredNode[]>([])
  const unmonitoredNodes = ref<UnmonitoredNode[]>([])
  const detectedNodes = ref<DetectedNode[]>([])
  const variables = reactive({ nodeIds: <number[]>[] })
  const labelSearchVariables = reactive<FindAllNodesByNodeLabelSearchQueryVariables>(
    { labelSearchTerm: '', monitoredState: MonitoredState.Monitored }
  )
  const tagsVariables = reactive<FindAllNodesByTagsQueryVariables>(
    { tags: <string[]>[], monitoredState: MonitoredState.Monitored }
  )
  const metricsVariables = reactive({
    id: 0,
    instance: '',
    monitor: Monitor.ICMP,
    timeRange: 1,
    timeRangeUnit: TimeRangeUnit.Minute
  })

  const { startSpinner, stopSpinner } = useSpinner()

  // Get monitored nodes
  const {
    onData: onGetMonitoredNodes,
    isFetching: monitoredNodesFetching,
    execute: getMonitoredNodes
  } = useQuery({
    query: FindAllNodesByMonitoredStateDocument,
    fetchOnMount: false,
    cachePolicy: 'network-only',
    variables: { monitoredState: MonitoredState.Monitored }
  })

  // Get unmonitored nodes
  const {
    onData: onGetUnmonitoredNodes,
    isFetching: unmonitoredNodesFetching,
    execute: getUnmonitoredNodes
  } = useQuery({
    query: FindAllNodesByMonitoredStateDocument,
    fetchOnMount: false,
    cachePolicy: 'network-only',
    variables: { monitoredState: MonitoredState.Unmonitored }
  })

  // Get detected nodes
  const {
    onData: onGetDetectedNodes,
    isFetching: detectedNodesFetching,
    execute: getDetectedNodes
  } = useQuery({
    query: FindAllNodesByMonitoredStateDocument,
    fetchOnMount: false,
    cachePolicy: 'network-only',
    variables: { monitoredState: MonitoredState.Detected }
  })

  // Get nodes by label - cannot yet filter by monitored state
  const {
    onData: onFilteredByLabelData,
    isFetching: filteredNodesByLabelFetching,
    execute: filterNodesByLabel
  } = useQuery({
    query: FindAllNodesByNodeLabelSearchDocument,
    cachePolicy: 'network-only',
    fetchOnMount: false,
    variables: labelSearchVariables
  })

  const getNodesByLabel = (label: string, monitoredState: MonitoredState) => {
    labelSearchVariables.labelSearchTerm = label
    labelSearchVariables.monitoredState = monitoredState
    filterNodesByLabel()
  }

  // Get nodes by tags - cannot yet filter by monitored state
  const {
    onData: onFilteredByTagsData,
    isFetching: filteredNodesByTagsFetching,
    execute: filterNodesByTags
  } = useQuery({
    query: FindAllNodesByTagsDocument,
    cachePolicy: 'network-only',
    fetchOnMount: false,
    variables: tagsVariables
  })

  const getNodesByTags = (tags: string[], monitoredState: MonitoredState) => {
    tagsVariables.tags = tags
    tagsVariables.monitoredState = monitoredState
    filterNodesByTags()
  }

  // Get tags for nodes
  const {
    data: tagData,
    execute: getTags,
    onData: onTagsData
  } = useQuery({
    query: ListTagsByNodeIdsDocument,
    cachePolicy: 'network-only',
    fetchOnMount: false,
    variables
  })

  // Get metrics for nodes
  const { onData: onMetricsData, execute: getMetrics } = useQuery({
    query: NodeLatencyMetricDocument,
    cachePolicy: 'network-only',
    fetchOnMount: false,
    variables: metricsVariables
  })

  watchEffect(() => (monitoredNodesFetching.value ? startSpinner() : stopSpinner()))
  watchEffect(() => (filteredNodesByLabelFetching.value ? startSpinner() : stopSpinner()))
  watchEffect(() => (filteredNodesByTagsFetching.value ? startSpinner() : stopSpinner()))
  watchEffect(() => (unmonitoredNodesFetching.value ? startSpinner() : stopSpinner()))
  watchEffect(() => (detectedNodesFetching.value ? startSpinner() : stopSpinner()))

  onGetMonitoredNodes((data) => formatMonitoredNodes(data.findAllNodesByMonitoredState ?? []))
  onFilteredByLabelData((data) => formatMonitoredNodes(data.findAllNodesByNodeLabelSearch ?? []))
  onFilteredByTagsData((data) => formatMonitoredNodes(data.findAllNodesByTags ?? []))
  onGetUnmonitoredNodes((data) => formatUnmonitoredNodes(data.findAllNodesByMonitoredState ?? []))
  onGetDetectedNodes((data) => formatDetectedNodes(data.findAllNodesByMonitoredState ?? []))
  onMetricsData((data) => addMetricsToMonitoredNodes(data))
  onTagsData((data) => addTagsToMonitoredNodes(data.tagsByNodeIds ?? []))

  // sets the initial monitored node object and then calls for tags and metrics
  const formatMonitoredNodes = async (data: Partial<Node>[]) => {
    if (data.length) {
      setMonitoredNodes(data)
      await getTagsForData(data)
      await getMetricsForData(data)
    } else {
      monitoredNodes.value = []
    }
  }

  // sets the initial monitored node object
  const setMonitoredNodes = (data: Partial<Node>[]) => {
    monitoredNodes.value = []
    for (const node of data) {
      const { ipAddress: snmpPrimaryIpAddress } = node.ipInterfaces?.filter((x) => x.snmpPrimary)[0] ?? {}
      const monitoredNode: MonitoredNode = {
        id: node.id,
        label: node.nodeLabel,
        status: '',
        metrics: [],
        anchor: {
          profileValue: '--',
          profileLink: '',
          locationValue: node.location?.location ?? '--',
          locationLink: '',
          managementIpValue: snmpPrimaryIpAddress ?? '',
          managementIpLink: '',
          tagValue: []
        },
        isNodeOverlayChecked: false,
        type: MonitoredState.Monitored
      }

      monitoredNodes.value.push(monitoredNode)
    }
  }

  // may be used to get tags for monitored/unmonitored/detected nodes
  const getTagsForData = async (data: Partial<Node>[]) => {
    variables.nodeIds = data.map((node) => node.id)
    await getTags()
  }

  // may only be used for monitored nodes
  const getMetricsForData = async (data: Partial<Node>[]) => {
    for (const node of data) {
      const { ipAddress: snmpPrimaryIpAddress } = node.ipInterfaces?.filter((x) => x.snmpPrimary)[0] ?? {}
      const instance = node.scanType === AZURE_SCAN ? `azure-node-${node.id}` : snmpPrimaryIpAddress!

      metricsVariables.id = node.id
      metricsVariables.instance = instance
      await getMetrics()
    }
  }

  // callback after getTags call complete
  const addTagsToMonitoredNodes = (tags: NodeTags[]) => {
    monitoredNodes.value = monitoredNodes.value.map((node) => {
      tags.forEach((tag) => {
        if (node.id === tag.nodeId) {
          node.anchor.tagValue = tag.tags ?? []
        }
      })

      return node
    })
  }

  // callback after getMetrics call complete
  const addMetricsToMonitoredNodes = (data: NodeLatencyMetricQuery) => {
    const latency = data.nodeLatency
    const status = data.nodeStatus

    const nodeId = latency?.data?.result?.[0]?.metric?.node_id || status?.id

    if (!nodeId) {
      console.warn('Cannot obtain metrics: No node id')
      return
    }

    const nodeLatency = latency?.data?.result as TsResult[]
    const latenciesValues = [...nodeLatency][0]?.values as number[][]
    const latencyValue = latenciesValues?.length ? latenciesValues[latenciesValues.length - 1][1] : undefined

    monitoredNodes.value = monitoredNodes.value.map((node) => {
      if (node.id === Number(nodeId)) {
        node.metrics = [
          {
            type: 'latency',
            label: 'Latency',
            value: latencyValue,
            status: ''
          },
          {
            type: 'status',
            label: 'Status',
            status: status?.status ?? ''
          }
        ]
      }

      return node
    })
  }

  const formatUnmonitoredNodes = async (data: Partial<Node>[]) => {

    if (data.length) {

      await getTagsForData(data)

      unmonitoredNodes.value = []
      data.forEach(({ id, nodeLabel, location, ipInterfaces }) => {
        const { ipAddress: snmpPrimaryIpAddress } = ipInterfaces?.filter((x) => x.snmpPrimary)[0] ?? {}
        const tagsObj = tagData.value?.tagsByNodeIds?.filter((item) => item.nodeId === id)[0]
        unmonitoredNodes.value.push({
          id: id,
          label: nodeLabel!,
          anchor: {
            locationValue: location?.location ?? '--',
            tagValue: tagsObj?.tags ?? [],
            managementIpValue: snmpPrimaryIpAddress ?? ''
          },
          isNodeOverlayChecked: false,
          type: MonitoredState.Unmonitored
        })
      })
    } else {
      unmonitoredNodes.value = []
    }
  }

  const formatDetectedNodes = async (data: Partial<Node>[]) => {

    if (data.length) {
      await getTagsForData(data)
      detectedNodes.value = []
      data.forEach(({ id, nodeLabel, location }) => {
        const tagsObj = tagData.value?.tagsByNodeIds?.filter((item) => item.nodeId === id)[0]
        detectedNodes.value.push({
          id: id,
          label: nodeLabel!,
          anchor: {
            locationValue: location?.location ?? '--',
            tagValue: tagsObj?.tags ?? []
          },
          isNodeOverlayChecked: false,
          type: MonitoredState.Detected
        })
      })
    } else {
      detectedNodes.value = []
    }
  }

  const fetchByState = async (stateIn: MonitoredState) => {
    if (stateIn === MonitoredState.Monitored) {
      await getMonitoredNodes()
      selectedState.value = MonitoredState.Monitored
    } else if (stateIn === MonitoredState.Unmonitored) {
      await getUnmonitoredNodes()
      selectedState.value = MonitoredState.Unmonitored
    } else if (stateIn === MonitoredState.Detected) {
      await getDetectedNodes()
      selectedState.value = MonitoredState.Detected
    }
  }

  const fetchByLastState = async () => {
    await fetchByState(selectedState.value)
  }

  return {
    monitoredNodes,
    unmonitoredNodes,
    detectedNodes,
    getTags,
    getMonitoredNodes: () => fetchByState(MonitoredState.Monitored),
    getNodesByLabel,
    getNodesByTags,
    getUnmonitoredNodes: () => fetchByState(MonitoredState.Unmonitored),
    getDetectedNodes: () => fetchByState(MonitoredState.Detected),
    fetchByState,
    fetchByLastState,
    isFetching: monitoredNodesFetching || unmonitoredNodesFetching || detectedNodesFetching
  }
})
