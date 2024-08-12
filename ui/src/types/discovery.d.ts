import { IcmpActiveDiscovery } from './graphql'

interface IKey {
  [key: string]: any
}

export interface DiscoveryInput extends IKey {
  id: number | null
  type: number
  name: string
  location: string[]
  tags: string[]
  IPRange: string
  communityString?: string | null
  UDPPort?: number | string | null
}

export interface DiscoveryTrapMeta {
  communityStrings?: string;
  udpPorts?: string;
  toggle?: { toggle: boolean, id: number };
}

export interface DiscoverySNMPMeta extends DiscoveryTrapMeta {
  ipRanges?: string;
}

export interface DiscoverySNMPV3Meta extends DiscoverySNMPMeta {
  username?: string;
  context?: string;
}

export interface DiscoverySNMPV3Auth extends DiscoverySNMPV3Meta {
  password?: string;
  usePasswordAsKey?: boolean;
}

export interface DiscoverySNMPV3AuthPrivacy extends DiscoverySNMPV3Auth {
  privacy?: string;
  usePrivacyAsKey?: boolean;
}

export interface DiscoveryAzureMeta {
  clientId?: string;
  clientSecret?: string;
  subscriptionId?: string;
  directoryId?: string;
}

export interface DiscoveryServicesMeta {
  discoveryTargets?: string;
  services: Service
}

export interface DiscoveryWindowServerMeta {
  username?: string;
  password?: string;
  windowsProtocol?: string;
  discoveryTarget?: Array<string>
}

export type DiscoveryMeta = DiscoveryServicesMeta | DiscoverySNMPMeta | DiscoveryTrapMeta | DiscoveryAzureMeta | DiscoveryWindowServerMeta

export interface NewOrUpdatedDiscovery {
  id?: number;
  name?: string;
  tags?: Array<Tag>;
  locations?: Array<any>;
  type?: string;
  meta?: DiscoveryMeta;
}

export interface DiscoveryStoreErrors {
  name?: string,
  tags?: string,
  locations?: string,
  locationId?: string,
  ip?: string,
  communityString?: string,
  updPort?: string,
  clientId?: string,
  clientSubscription?: string,
  subscriptionId?: string,
  directoryId?: string,
  clientSecret?: string,
  password?: string,
  username?: string,
  context?: string,
  privacy?: string,
  windowsProtocol?: string,
  discoveryTargets? : string,
}

export interface DiscoveryStore {
  discoveryFormActive: boolean;
  deleteModalOpen: boolean;
  helpActive: boolean;
  foundLocations: Array<any>;
  foundTags: Array<string>;
  instructionsType: string;
  loadedDiscoveries: Array<NewOrUpdatedDiscovery>;
  loading: boolean;
  locationSearch: string;
  locationError: string;
  newDiscoveryModalActive: boolean;
  selectedDiscovery: NewOrUpdatedDiscovery;
  snmpV3Enabled: boolean;
  soloTypeEditor: boolean;
  discoveryTypePageActive: boolean;
  tagSearch: string;
  tagError: string;
  validationErrors: DiscoveryStoreErrors;
  disableSave: boolean;
  validateOnKeyUp: boolean;
}
export interface ServerDiscovery {
  discoveryType?: string;
  details?: {
    id?: number,
    tags?: Array<Tag>,
    ipAddresses?: Array<string>,
    locations?: Array<Location>,
    locationId: string,
    name: string,
    clientId?: string,
    directoryId?: string,
    subscriptionId?: string,
    clientSecret?: string,
    snmpConfig?: {
      ports: Array<string>,
      readCommunities: Array<string>,
    } }
}

export interface PassiveServerDiscovery {
  id?: any;
  locationId?: string | undefined;
  name?: string | undefined;
  snmpCommunities?: string[] | undefined;
  snmpPorts?: number[] | undefined; toggle: boolean;
  tags?: Array<Tag>;
}

export interface ServerDiscoveries {
  listActiveDiscovery?: Array<ServerDiscovery>;
  passiveDiscoveries?: Array<PassiveServerDiscovery>;
}

export interface IcmpActiveDiscoveryPlusTags extends IcmpActiveDiscovery {
  tags: Array<Tag>
}

export interface Service {
  label?: string;
  type?: string;
  port?: number;
}
