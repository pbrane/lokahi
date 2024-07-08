import { Tag } from './graphql'
import { Chip } from './metric'

export interface Anchor {
  profileValue?: number | string
  profileLink?: string
  locationValue?: string
  locationLink?: string
  managementIpValue?: string
  managementIpLink?: string
  tagValue: Tag[]
}

interface InventoryNode {
  id: number
  label: string | undefined
  status: string
  metrics: Chip[]
  anchor: Anchor
  isNodeOverlayChecked: boolean
  type: MonitoredStates.DETECTED | MonitoredStates.MONITORED | MonitoredStates.UNMONITORED
}

interface IpInterface {
  id: number;
  ipAddress: string;
  nodeId: number;
  snmpPrimary: boolean;
}

interface Location {
  id: number;
  location: string;
}

interface Tag {
  id: number;
  name: string;
}

interface NewInventoryNode {
  id: number;
  ipInterfaces: IpInterface[];
  location: Location;
  monitoredState: string;
  monitoringLocationId: number;
  nodeLabel: string;
  scanType: string;
  tags: Tag[];
  nodeAlias: string | null;
}
interface RawMetric {
  metric: {
    __name__: string,
    instance: string,
    location_id: string,
    monitor: string,
    node_id: string,
    system_id: string
  },
  value: [number, number],
  values: []
}

interface RawMetrics {
  status: string,
  data: {
    resultType: string,
    result: Array<RawMetric>
  }
}

interface InventoryItemFilters {
  sortAscending?: boolean
  sortBy?: string
  searchValue?: string,
  searchType?: string,
}

export interface InventoryItem extends NewInventoryNode {
  metrics?: RawMetric
}

export interface InventoryPage {
  nodes: Array<InventoryItem>;
}

export const enum MonitoredStates {
  MONITORED = 'MONITORED',
  UNMONITORED = 'UNMONITORED',
  DETECTED = 'DETECTED'
}
