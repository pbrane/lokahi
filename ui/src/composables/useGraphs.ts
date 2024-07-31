import { useQuery } from 'villus'
import { GetTimeSeriesMetricDocument, GetTimeSeriesMetricsWithIfNameDocument } from '@/types/graphql'
import { DataSets, MetricArgs, GraphProps } from '@/types/graphs'
import { GRAPH_TYPE_CPU_UTILIZATION, GRAPH_TYPE_MEMORY_UTILIZATION } from '@/components/NodeStatus/NodeStatus.constants'

export const useGraphs = () => {
  const dataSetsObject = reactive({} as any)

  const { data, execute: getMetric } = useQuery({
    query: GetTimeSeriesMetricDocument,
    cachePolicy: 'network-only',
    fetchOnMount: false
  })

  const { data: ifNameData, execute: getMetricsWithIfName } = useQuery({
    query: GetTimeSeriesMetricsWithIfNameDocument,
    cachePolicy: 'network-only',
    fetchOnMount: false
  })

  const finalizeDataSetsObject = (result: any, metricStr: string) => {
    if (result) {
      const { metric, values } = result

      if (values?.length) {
        dataSetsObject[metricStr] = {
          metric,
          values: values.filter((val: [number, number]) => {
            const [timestamp, value] = val
            if (timestamp && value) return val
          })
        }
      }
    }
  }

  const getMetrics = async (props: GraphProps) => {
    const { metrics, monitor, timeRange, timeRangeUnit, instance, nodeId, ifName } = props

    let result

    if (metrics.length === 1 && (metrics.includes(GRAPH_TYPE_CPU_UTILIZATION) || metrics.includes(GRAPH_TYPE_MEMORY_UTILIZATION))) {
      const variables: MetricArgs = { name: metrics[0], monitor, timeRange, timeRangeUnit, instance, nodeId }
      await getMetric({variables: variables})
      result = data.value?.metric?.data?.result?.[0]

      finalizeDataSetsObject(result, metrics[0])
    } else {
      for (const metricStr of metrics) {
        if (instance) {
          const variables: MetricArgs = { name: metricStr, monitor, timeRange, timeRangeUnit, instance, nodeId }
          await getMetric({variables: variables})
          result = data.value?.metric?.data?.result?.[0]

        } else {
          const variables: MetricArgs = { name: metricStr, monitor, timeRange, timeRangeUnit, nodeId, ifName }
          await getMetricsWithIfName({variables: variables})
          result = ifNameData.value?.metric?.data?.result?.[0]
        }

        finalizeDataSetsObject(result, metricStr)
      }
    }
  }

  return {
    getMetrics,
    dataSets: computed<DataSets>(() => Object.values(dataSetsObject))
  }
}
