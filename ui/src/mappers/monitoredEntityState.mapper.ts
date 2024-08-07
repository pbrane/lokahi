import { MonitoredEntityState as MonitoredEntityStateServer, TimeSeriesQueryResult } from '@/types/graphql'
import { MonitoredEntityState as MonitoredEntityStateClient, MonitoredEntityStateMetrics } from '@/types/monitoredEntityState'

export const mapToClientFromServer = (server: MonitoredEntityStateServer): MonitoredEntityStateClient => {
  return {
    id: server.id,
    tenantId: server.tenantId,
    monitoredEntityId: server.monitoredEntityId,
    state: server.state,
    firstObservationTime: server.firstObservationTime
  } as MonitoredEntityStateClient
}

export const mapMetricsToClientFromServer = (latencyData: TimeSeriesQueryResult, reachabilityData: TimeSeriesQueryResult): MonitoredEntityStateMetrics => {
  const data: MonitoredEntityStateMetrics = {
    instance: latencyData.data?.result?.[0]?.metric?.instance ?? '',
    monitor: latencyData.data?.result?.[0]?.metric?.monitor ?? '',
    status: latencyData.status === 'success' ? 'Up' : 'Down,',
    resultType: latencyData.data?.resultType ?? '',
    name: latencyData.data?.result?.[0]?.metric?.__name__ ?? '',
    latencyValue: latencyData.data?.result?.[0]?.value ?? [],
    reachabilityValue: reachabilityData.data?.result?.[0]?.value ?? []
  }

  return data
}
