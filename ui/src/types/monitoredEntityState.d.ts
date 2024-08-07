/**
 * Similar to graphql.ts/MonitoredEntityState.
 */
export interface MonitoredEntityState {
  id: number;
  tenantId: string;
  monitoredEntityId: string;
  state: boolean;
  firstObservationTime: number;
}


export interface MonitoredEntityStateMetrics {
  instance: string;
  monitor: string;
  status: string;
  resultType: string;
  name: string;
  latencyValue: number[];
  reachabilityValue: number[];
}
