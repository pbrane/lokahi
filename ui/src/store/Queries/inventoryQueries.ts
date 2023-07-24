import { useQuery } from 'villus'
import { defineStore } from 'pinia'
import {
  FindAllNodesByMonitoredStateDocument,
  FindAllNodesByNodeLabelSearchDocument,
  FindAllNodesByNodeLabelSearchQueryVariables,
  FindAllNodesByTagsDocument,
  FindAllNodesByTagsQueryVariables,
  ListTagsByNodeIdsDocument,
  MonitoredState,
  Node,
  NodeLatencyMetricDocument,
  NodeLatencyMetricQuery,
  NodeTags,
  TimeRangeUnit,
  TsResult
} from '@/types/graphql'
import useSpinner from '@/composables/useSpinner'
import { AZURE_SCAN, DetectedNode, InventoryNode, Monitor, MonitoredNode, UnmonitoredNode } from '@/types'

export const useInventoryQueries = defineStore('inventoryQueries', () => {
  const selectedState = ref(MonitoredState.Monitored)
  const monitoredNodes = ref<MonitoredNode[]>([])
  const unmonitoredNodes = ref<UnmonitoredNode[]>([])
  const detectedNodes = ref<DetectedNode[]>([])

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
  onGetMonitoredNodes((data) => formatNodes(data.findAllNodesByMonitoredState ?? [], MonitoredState.Monitored))
  watchEffect(() => (monitoredNodesFetching.value ? startSpinner() : stopSpinner()))

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
  onGetUnmonitoredNodes((data) => formatNodes(data.findAllNodesByMonitoredState ?? [], MonitoredState.Unmonitored))
  watchEffect(() => (unmonitoredNodesFetching.value ? startSpinner() : stopSpinner()))

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
  onGetDetectedNodes((data) => formatNodes(data.findAllNodesByMonitoredState ?? [], MonitoredState.Detected))
  watchEffect(() => (detectedNodesFetching.value ? startSpinner() : stopSpinner()))

  // Get nodes by label
  const labelSearchVariables = reactive<FindAllNodesByNodeLabelSearchQueryVariables>(
    { labelSearchTerm: '', monitoredState: MonitoredState.Monitored }
  )
  const {
    isFetching: filteredNodesByLabelFetching,
    execute: filterNodesByLabel
  } = useQuery({
    query: FindAllNodesByNodeLabelSearchDocument,
    cachePolicy: 'network-only',
    fetchOnMount: false,
    variables: labelSearchVariables
  })
  watchEffect(() => (filteredNodesByLabelFetching.value ? startSpinner() : stopSpinner()))

  const getNodesByLabel = async (label: string, monitoredState: MonitoredState) => {
    labelSearchVariables.labelSearchTerm = label
    labelSearchVariables.monitoredState = monitoredState
    const result = await filterNodesByLabel()
    if (result.data) {
      await formatNodes(result.data.findAllNodesByNodeLabelSearch ?? [], labelSearchVariables.monitoredState)
    }
  }

  // Get nodes by tags
  const filterNodesByTagsVariables = reactive<FindAllNodesByTagsQueryVariables>(
    { tags: <string[]>[], monitoredState: MonitoredState.Monitored }
  )
  const {
    isFetching: filteredNodesByTagsFetching,
    execute: filterNodesByTags
  } = useQuery({
    query: FindAllNodesByTagsDocument,
    cachePolicy: 'network-only',
    fetchOnMount: false,
    variables: filterNodesByTagsVariables
  })
  watchEffect(() => (filteredNodesByTagsFetching.value ? startSpinner() : stopSpinner()))

  const getNodesByTags = async (tags: string[], monitoredState: MonitoredState) => {
    filterNodesByTagsVariables.tags = tags
    filterNodesByTagsVariables.monitoredState = monitoredState
    const result = await filterNodesByTags()
    if (result.data) {
      await formatNodes(result.data.findAllNodesByTags ?? [], filterNodesByTagsVariables.monitoredState)
    }
  }

  // Get tags for nodes
  const listTagsByNodeIdsVariables = reactive({ nodeIds: <number[]>[] })
  const {
    data: tagData,
    execute: getTags
  } = useQuery({
    query: ListTagsByNodeIdsDocument,
    cachePolicy: 'network-only',
    fetchOnMount: false,
    variables: listTagsByNodeIdsVariables
  })

  // Get metrics for nodes
  const metricsVariables = reactive({
    id: 0,
    instance: '',
    monitor: Monitor.ICMP,
    timeRange: 1,
    timeRangeUnit: TimeRangeUnit.Minute
  })
  const { execute: getMetrics } = useQuery({
    query: NodeLatencyMetricDocument,
    cachePolicy: 'network-only',
    fetchOnMount: false,
    variables: metricsVariables
  })

  const formatNodes = async (data: Partial<Node>[], monitoredState: MonitoredState) => {
    switch (monitoredState) {
      case MonitoredState.Detected:
        return await formatDetectedNodes(data)
      case MonitoredState.Monitored:
        return await formatMonitoredNodes(data)
      case MonitoredState.Unmonitored:
        return await formatUnmonitoredNodes(data)
      default:
        return data
    }
  }

  // sets the initial monitored node object and then calls for tags and metrics
  const formatMonitoredNodes = async (data: Partial<Node>[]) => {
    if (data.length) {
      monitoredNodes.value = mapMonitoredNodes(data)
      await getTagsForData(data)
      await getMetricsForData(data)
    } else {
      monitoredNodes.value = []
    }
  }

  const mapMonitoredNodes = (data: Partial<Node>[]): MonitoredNode[] => {
    return data.map((node) => {
      const { ipAddress: snmpPrimaryIpAddress } = node.ipInterfaces?.filter((x) => x.snmpPrimary)[0] ?? {}
      return {
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
    })
  }

  // may be used to get tags for monitored/unmonitored/detected nodes
  const getTagsForData = async (data: Partial<Node>[]) => {
    listTagsByNodeIdsVariables.nodeIds = data.map((node) => node.id)
    const tagResult = await getTags()
    monitoredNodes.value = addTagsToNodes(tagResult.data?.tagsByNodeIds ?? [], monitoredNodes.value) as MonitoredNode[]
  }

  // may only be used for monitored nodes
  const getMetricsForData = async (data: Partial<Node>[]) => {
    for (const node of data) {
      const { ipAddress: snmpPrimaryIpAddress } = node.ipInterfaces?.filter((x) => x.snmpPrimary)[0] ?? {}
      const instance = node.scanType === AZURE_SCAN ? `azure-node-${node.id}` : snmpPrimaryIpAddress!

      metricsVariables.id = node.id
      metricsVariables.instance = instance
      const metricsResult = await getMetrics()
      if (metricsResult.data) {
        monitoredNodes.value = addMetricsToMonitoredNodes(metricsResult.data, monitoredNodes.value)
      }
    }
  }

  // callback after getTags call complete
  const addTagsToNodes = (tags: NodeTags[], nodeList: InventoryNode[]) => {
    return nodeList.map((node) => {
      tags.forEach((tag) => {
        if (node.id === tag.nodeId) {
          node.anchor.tagValue = tag.tags ?? []
        }
      })

      return node
    })
  }

  // callback after getMetrics call complete
  const addMetricsToMonitoredNodes = (data: NodeLatencyMetricQuery, monitoredNodes: MonitoredNode[]) => {
    const latency = data.nodeLatency
    const status = data.nodeStatus

    const nodeId = latency?.data?.result?.[0]?.metric?.node_id || status?.id

    if (!nodeId) {
      console.warn('Cannot obtain metrics: No node id')
      return monitoredNodes
    }

    const nodeLatency = latency?.data?.result as TsResult[]
    const latenciesValues = [...nodeLatency][0]?.values as number[][]
    const latencyValue = latenciesValues?.length ? latenciesValues[latenciesValues.length - 1][1] : undefined

    return monitoredNodes.map((node) => {
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
    getMonitoredNodes: () => fetchByState(MonitoredState.Monitored),
    getUnmonitoredNodes: () => fetchByState(MonitoredState.Unmonitored),
    getDetectedNodes: () => fetchByState(MonitoredState.Detected),
    getNodesByLabel,
    getNodesByTags,
    fetchByState,
    fetchByLastState,
    isFetching: monitoredNodesFetching || unmonitoredNodesFetching || detectedNodesFetching
  }
})
