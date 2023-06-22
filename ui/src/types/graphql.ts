import { TypedDocumentNode as DocumentNode } from '@graphql-typed-document-node/core';
export type Maybe<T> = T;
export type InputMaybe<T> = T;
export type Exact<T extends { [key: string]: unknown }> = { [K in keyof T]: T[K] };
export type MakeOptional<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]?: Maybe<T[SubKey]> };
export type MakeMaybe<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]: Maybe<T[SubKey]> };
/** All built-in and custom scalars, mapped to their actual values */
export type Scalars = {
  ID: string;
  String: string;
  Boolean: boolean;
  Int: number;
  Float: number;
  Base64String: any;
  Instant: any;
  Json: any;
  Long: any;
  Map_String_StringScalar: any;
  UNREPRESENTABLE: any;
};

export type ActiveDiscovery = {
  __typename?: 'ActiveDiscovery';
  details?: Maybe<Scalars['Json']>;
  discoveryType?: Maybe<Scalars['String']>;
};

export type Alert = {
  __typename?: 'Alert';
  ackTimeMs: Scalars['Long'];
  ackUser?: Maybe<Scalars['String']>;
  acknowledged: Scalars['Boolean'];
  clearKey?: Maybe<Scalars['String']>;
  counter: Scalars['Long'];
  databaseId: Scalars['Long'];
  description?: Maybe<Scalars['String']>;
  firstEventTimeMs: Scalars['Long'];
  label?: Maybe<Scalars['String']>;
  lastEventId: Scalars['Long'];
  lastUpdateTimeMs: Scalars['Long'];
  location?: Maybe<Scalars['String']>;
  logMessage?: Maybe<Scalars['String']>;
  managedObject?: Maybe<ManagedObject>;
  nodeName?: Maybe<Scalars['String']>;
  reductionKey?: Maybe<Scalars['String']>;
  severity?: Maybe<Severity>;
  tenantId?: Maybe<Scalars['String']>;
  type?: Maybe<AlertType>;
  uei?: Maybe<Scalars['String']>;
};

export type AlertError = {
  __typename?: 'AlertError';
  databaseId: Scalars['Long'];
  error?: Maybe<Scalars['String']>;
};

export type AlertResponse = {
  __typename?: 'AlertResponse';
  alertErrorList?: Maybe<Array<Maybe<AlertError>>>;
  alertList?: Maybe<Array<Maybe<Alert>>>;
};

export enum AlertType {
  AlarmTypeUndefined = 'ALARM_TYPE_UNDEFINED',
  Clear = 'CLEAR',
  ProblemWithoutClear = 'PROBLEM_WITHOUT_CLEAR',
  ProblemWithClear = 'PROBLEM_WITH_CLEAR',
  Unrecognized = 'UNRECOGNIZED'
}

export type AzureActiveDiscovery = {
  __typename?: 'AzureActiveDiscovery';
  clientId?: Maybe<Scalars['String']>;
  createTimeMsec?: Maybe<Scalars['Long']>;
  directoryId?: Maybe<Scalars['String']>;
  id?: Maybe<Scalars['Long']>;
  locationId?: Maybe<Scalars['String']>;
  name?: Maybe<Scalars['String']>;
  subscriptionId?: Maybe<Scalars['String']>;
  tenantId?: Maybe<Scalars['String']>;
};

export type AzureActiveDiscoveryCreateInput = {
  clientId?: InputMaybe<Scalars['String']>;
  clientSecret?: InputMaybe<Scalars['String']>;
  directoryId?: InputMaybe<Scalars['String']>;
  locationId?: InputMaybe<Scalars['String']>;
  name?: InputMaybe<Scalars['String']>;
  subscriptionId?: InputMaybe<Scalars['String']>;
  tags?: InputMaybe<Array<InputMaybe<TagCreateInput>>>;
};

export type CertificateResponse = {
  __typename?: 'CertificateResponse';
  certificate?: Maybe<Scalars['Base64String']>;
  password?: Maybe<Scalars['String']>;
};

export type CountAlertResponse = {
  __typename?: 'CountAlertResponse';
  count: Scalars['Int'];
  error?: Maybe<Scalars['String']>;
};

export type DeleteAlertResponse = {
  __typename?: 'DeleteAlertResponse';
  alertDatabaseIdList?: Maybe<Array<Maybe<Scalars['Long']>>>;
  alertErrorList?: Maybe<Array<Maybe<AlertError>>>;
};

export type Event = {
  __typename?: 'Event';
  eventInfo?: Maybe<EventInfo>;
  eventParams?: Maybe<Array<Maybe<EventParameter>>>;
  id: Scalars['Int'];
  ipAddress?: Maybe<Scalars['String']>;
  nodeId: Scalars['Int'];
  producedTime: Scalars['Long'];
  tenantId?: Maybe<Scalars['String']>;
  uei?: Maybe<Scalars['String']>;
};

export type EventInfo = {
  __typename?: 'EventInfo';
  snmp?: Maybe<SnmpInfo>;
};

export type EventParameter = {
  __typename?: 'EventParameter';
  encoding?: Maybe<Scalars['String']>;
  name?: Maybe<Scalars['String']>;
  type?: Maybe<Scalars['String']>;
  value?: Maybe<Scalars['String']>;
};

export type Exporter = {
  __typename?: 'Exporter';
  ipInterface?: Maybe<IpInterface>;
  node?: Maybe<Node>;
  snmpInterface?: Maybe<SnmpInterface>;
};

export type ExporterFilterInput = {
  ipInterfaceId?: InputMaybe<Scalars['Long']>;
  nodeId?: InputMaybe<Scalars['Long']>;
};

export type FlowingPoint = {
  __typename?: 'FlowingPoint';
  direction?: Maybe<Scalars['String']>;
  label?: Maybe<Scalars['String']>;
  timestamp?: Maybe<Scalars['Instant']>;
  value: Scalars['Float'];
};

export type IcmpActiveDiscovery = {
  __typename?: 'IcmpActiveDiscovery';
  id: Scalars['Long'];
  ipAddresses?: Maybe<Array<Maybe<Scalars['String']>>>;
  locationId?: Maybe<Scalars['String']>;
  name?: Maybe<Scalars['String']>;
  snmpConfig?: Maybe<SnmpConfig>;
};

export type IcmpActiveDiscoveryCreateInput = {
  ipAddresses?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
  locationId?: InputMaybe<Scalars['String']>;
  name?: InputMaybe<Scalars['String']>;
  snmpConfig?: InputMaybe<SnmpConfigInput>;
  tags?: InputMaybe<Array<InputMaybe<TagCreateInput>>>;
};

export type IpInterface = {
  __typename?: 'IpInterface';
  hostname?: Maybe<Scalars['String']>;
  id: Scalars['Long'];
  ipAddress?: Maybe<Scalars['String']>;
  netmask?: Maybe<Scalars['String']>;
  nodeId: Scalars['Long'];
  snmpPrimary?: Maybe<Scalars['Boolean']>;
  tenantId?: Maybe<Scalars['String']>;
};

export type ListAlertResponse = {
  __typename?: 'ListAlertResponse';
  alerts?: Maybe<Array<Maybe<Alert>>>;
  lastPage: Scalars['Int'];
  nextPage: Scalars['Int'];
  totalAlerts: Scalars['Long'];
};

export type ManagedObject = {
  __typename?: 'ManagedObject';
  instance?: Maybe<ManagedObjectInstance>;
  type?: Maybe<ManagedObjectType>;
};

export type ManagedObjectInstance = {
  __typename?: 'ManagedObjectInstance';
  nodeVal?: Maybe<NodeRef>;
  snmpInterfaceLinkVal?: Maybe<SnmpInterfaceLinkRef>;
  snmpInterfaceVal?: Maybe<SnmpInterfaceRef>;
};

export enum ManagedObjectType {
  Any = 'ANY',
  Node = 'NODE',
  SnmpInterface = 'SNMP_INTERFACE',
  SnmpInterfaceLink = 'SNMP_INTERFACE_LINK',
  Undefined = 'UNDEFINED',
  Unrecognized = 'UNRECOGNIZED'
}

export type Minion = {
  __typename?: 'Minion';
  id: Scalars['Long'];
  label?: Maybe<Scalars['String']>;
  lastCheckedTime: Scalars['Long'];
  location?: Maybe<MonitoringLocation>;
  locationId: Scalars['Long'];
  status?: Maybe<Scalars['String']>;
  systemId?: Maybe<Scalars['String']>;
  tenantId?: Maybe<Scalars['String']>;
};

export type MonitorPolicy = {
  __typename?: 'MonitorPolicy';
  id?: Maybe<Scalars['Long']>;
  memo?: Maybe<Scalars['String']>;
  name?: Maybe<Scalars['String']>;
  notifyByEmail?: Maybe<Scalars['Boolean']>;
  notifyByPagerDuty?: Maybe<Scalars['Boolean']>;
  notifyByWebhooks?: Maybe<Scalars['Boolean']>;
  notifyInstruction?: Maybe<Scalars['String']>;
  rules?: Maybe<Array<Maybe<PolicyRule>>>;
  tags?: Maybe<Array<Maybe<Scalars['String']>>>;
  tenantId?: Maybe<Scalars['String']>;
};

export type MonitorPolicyInput = {
  id?: InputMaybe<Scalars['Long']>;
  memo?: InputMaybe<Scalars['String']>;
  name?: InputMaybe<Scalars['String']>;
  notifyByEmail?: InputMaybe<Scalars['Boolean']>;
  notifyByPagerDuty?: InputMaybe<Scalars['Boolean']>;
  notifyByWebhooks?: InputMaybe<Scalars['Boolean']>;
  notifyInstruction?: InputMaybe<Scalars['String']>;
  rules?: InputMaybe<Array<InputMaybe<PolicyRuleInput>>>;
  tags?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
  tenantId?: InputMaybe<Scalars['String']>;
};

export type MonitoringLocation = {
  __typename?: 'MonitoringLocation';
  address?: Maybe<Scalars['String']>;
  id: Scalars['Long'];
  latitude?: Maybe<Scalars['Float']>;
  location?: Maybe<Scalars['String']>;
  longitude?: Maybe<Scalars['Float']>;
  tenantId?: Maybe<Scalars['String']>;
};

export type MonitoringLocationCreateInput = {
  address?: InputMaybe<Scalars['String']>;
  latitude?: InputMaybe<Scalars['Float']>;
  location?: InputMaybe<Scalars['String']>;
  longitude?: InputMaybe<Scalars['Float']>;
};

export type MonitoringLocationUpdateInput = {
  address?: InputMaybe<Scalars['String']>;
  id: Scalars['Long'];
  latitude?: InputMaybe<Scalars['Float']>;
  location?: InputMaybe<Scalars['String']>;
  longitude?: InputMaybe<Scalars['Float']>;
};

/** Mutation root */
export type Mutation = {
  __typename?: 'Mutation';
  acknowledgeAlert?: Maybe<AlertResponse>;
  addNode?: Maybe<Node>;
  addTagsToNodes?: Maybe<Array<Maybe<Tag>>>;
  clearAlert?: Maybe<AlertResponse>;
  createAzureActiveDiscovery?: Maybe<AzureActiveDiscovery>;
  createIcmpActiveDiscovery?: Maybe<IcmpActiveDiscovery>;
  createLocation?: Maybe<MonitoringLocation>;
  createMonitorPolicy?: Maybe<MonitorPolicy>;
  deleteAlert?: Maybe<DeleteAlertResponse>;
  deleteLocation?: Maybe<Scalars['Boolean']>;
  deleteMinion?: Maybe<Scalars['Boolean']>;
  deleteNode?: Maybe<Scalars['Boolean']>;
  discoveryByNodeIds?: Maybe<Scalars['Boolean']>;
  escalateAlert?: Maybe<AlertResponse>;
  removeTagsFromNodes?: Maybe<Scalars['Boolean']>;
  savePagerDutyConfig?: Maybe<Scalars['Boolean']>;
  togglePassiveDiscovery?: Maybe<PassiveDiscoveryToggle>;
  unacknowledgeAlert?: Maybe<AlertResponse>;
  updateLocation?: Maybe<MonitoringLocation>;
  upsertPassiveDiscovery?: Maybe<PassiveDiscovery>;
};


/** Mutation root */
export type MutationAcknowledgeAlertArgs = {
  ids?: InputMaybe<Array<InputMaybe<Scalars['Long']>>>;
};


/** Mutation root */
export type MutationAddNodeArgs = {
  node?: InputMaybe<NodeCreateInput>;
};


/** Mutation root */
export type MutationAddTagsToNodesArgs = {
  tags?: InputMaybe<TagListNodesAddInput>;
};


/** Mutation root */
export type MutationClearAlertArgs = {
  ids?: InputMaybe<Array<InputMaybe<Scalars['Long']>>>;
};


/** Mutation root */
export type MutationCreateAzureActiveDiscoveryArgs = {
  discovery?: InputMaybe<AzureActiveDiscoveryCreateInput>;
};


/** Mutation root */
export type MutationCreateIcmpActiveDiscoveryArgs = {
  request?: InputMaybe<IcmpActiveDiscoveryCreateInput>;
};


/** Mutation root */
export type MutationCreateLocationArgs = {
  location?: InputMaybe<MonitoringLocationCreateInput>;
};


/** Mutation root */
export type MutationCreateMonitorPolicyArgs = {
  policy?: InputMaybe<MonitorPolicyInput>;
};


/** Mutation root */
export type MutationDeleteAlertArgs = {
  ids?: InputMaybe<Array<InputMaybe<Scalars['Long']>>>;
};


/** Mutation root */
export type MutationDeleteLocationArgs = {
  id: Scalars['Long'];
};


/** Mutation root */
export type MutationDeleteMinionArgs = {
  id?: InputMaybe<Scalars['String']>;
};


/** Mutation root */
export type MutationDeleteNodeArgs = {
  id?: InputMaybe<Scalars['Long']>;
};


/** Mutation root */
export type MutationDiscoveryByNodeIdsArgs = {
  ids?: InputMaybe<Array<InputMaybe<Scalars['Long']>>>;
};


/** Mutation root */
export type MutationEscalateAlertArgs = {
  ids?: InputMaybe<Array<InputMaybe<Scalars['Long']>>>;
};


/** Mutation root */
export type MutationRemoveTagsFromNodesArgs = {
  tags?: InputMaybe<TagListNodesRemoveInput>;
};


/** Mutation root */
export type MutationSavePagerDutyConfigArgs = {
  config?: InputMaybe<PagerDutyConfigInput>;
};


/** Mutation root */
export type MutationTogglePassiveDiscoveryArgs = {
  toggle?: InputMaybe<PassiveDiscoveryToggleInput>;
};


/** Mutation root */
export type MutationUnacknowledgeAlertArgs = {
  ids?: InputMaybe<Array<InputMaybe<Scalars['Long']>>>;
};


/** Mutation root */
export type MutationUpdateLocationArgs = {
  location?: InputMaybe<MonitoringLocationUpdateInput>;
};


/** Mutation root */
export type MutationUpsertPassiveDiscoveryArgs = {
  discovery?: InputMaybe<PassiveDiscoveryUpsertInput>;
};

export type Node = {
  __typename?: 'Node';
  createTime: Scalars['Long'];
  id: Scalars['Long'];
  ipInterfaces?: Maybe<Array<Maybe<IpInterface>>>;
  location?: Maybe<MonitoringLocation>;
  monitoredState?: Maybe<Scalars['String']>;
  monitoringLocationId: Scalars['Long'];
  nodeLabel?: Maybe<Scalars['String']>;
  objectId?: Maybe<Scalars['String']>;
  scanType?: Maybe<Scalars['String']>;
  snmpInterfaces?: Maybe<Array<Maybe<SnmpInterface>>>;
  systemContact?: Maybe<Scalars['String']>;
  systemDescr?: Maybe<Scalars['String']>;
  systemLocation?: Maybe<Scalars['String']>;
  systemName?: Maybe<Scalars['String']>;
  tenantId?: Maybe<Scalars['String']>;
};

export type NodeCreateInput = {
  label?: InputMaybe<Scalars['String']>;
  locationId?: InputMaybe<Scalars['String']>;
  managementIp?: InputMaybe<Scalars['String']>;
  tags?: InputMaybe<Array<InputMaybe<TagCreateInput>>>;
};

export type NodeRef = {
  __typename?: 'NodeRef';
  nodeID: Scalars['Long'];
};

export type NodeStatus = {
  __typename?: 'NodeStatus';
  id: Scalars['Long'];
  status?: Maybe<Scalars['String']>;
};

export type NodeTags = {
  __typename?: 'NodeTags';
  nodeId: Scalars['Long'];
  tags?: Maybe<Array<Maybe<Tag>>>;
};

export type PagerDutyConfigInput = {
  integrationkey?: InputMaybe<Scalars['String']>;
};

export type PassiveDiscovery = {
  __typename?: 'PassiveDiscovery';
  createTimeMsec?: Maybe<Scalars['Long']>;
  id?: Maybe<Scalars['Long']>;
  location?: Maybe<Scalars['String']>;
  name?: Maybe<Scalars['String']>;
  snmpCommunities?: Maybe<Array<Maybe<Scalars['String']>>>;
  snmpPorts?: Maybe<Array<Maybe<Scalars['Int']>>>;
  toggle: Scalars['Boolean'];
};

export type PassiveDiscoveryToggle = {
  __typename?: 'PassiveDiscoveryToggle';
  id: Scalars['Long'];
  toggle: Scalars['Boolean'];
};

export type PassiveDiscoveryToggleInput = {
  id: Scalars['Long'];
  toggle: Scalars['Boolean'];
};

export type PassiveDiscoveryUpsertInput = {
  id?: InputMaybe<Scalars['Long']>;
  location?: InputMaybe<Scalars['String']>;
  name?: InputMaybe<Scalars['String']>;
  snmpCommunities?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
  snmpPorts?: InputMaybe<Array<InputMaybe<Scalars['Int']>>>;
  tags?: InputMaybe<Array<InputMaybe<TagCreateInput>>>;
};

export type PolicyRule = {
  __typename?: 'PolicyRule';
  componentType?: Maybe<Scalars['String']>;
  id?: Maybe<Scalars['Long']>;
  name?: Maybe<Scalars['String']>;
  tenantId?: Maybe<Scalars['String']>;
  triggerEvents?: Maybe<Array<Maybe<TriggerEvent>>>;
};

export type PolicyRuleInput = {
  componentType?: InputMaybe<Scalars['String']>;
  id?: InputMaybe<Scalars['Long']>;
  name?: InputMaybe<Scalars['String']>;
  tenantId?: InputMaybe<Scalars['String']>;
  triggerEvents?: InputMaybe<Array<InputMaybe<TriggerEventInput>>>;
};

/** Query root */
export type Query = {
  __typename?: 'Query';
  /** Returns the total count of alerts filtered by severity and time. */
  countAlerts?: Maybe<CountAlertResponse>;
  defaultPolicy?: Maybe<MonitorPolicy>;
  findAllAlerts?: Maybe<ListAlertResponse>;
  findAllEvents?: Maybe<Array<Maybe<Event>>>;
  findAllLocations?: Maybe<Array<Maybe<MonitoringLocation>>>;
  findAllMinions?: Maybe<Array<Maybe<Minion>>>;
  findAllNodes?: Maybe<Array<Maybe<Node>>>;
  findAllNodesByMonitoredState?: Maybe<Array<Maybe<Node>>>;
  findAllNodesByNodeLabelSearch?: Maybe<Array<Maybe<Node>>>;
  findAllNodesByTags?: Maybe<Array<Maybe<Node>>>;
  findApplicationSeries?: Maybe<Array<Maybe<FlowingPoint>>>;
  findApplicationSummaries?: Maybe<Array<Maybe<TrafficSummary>>>;
  findApplications?: Maybe<Array<Maybe<Scalars['String']>>>;
  findEventsByNodeId?: Maybe<Array<Maybe<Event>>>;
  findExporters?: Maybe<Array<Maybe<Exporter>>>;
  findLocationById?: Maybe<MonitoringLocation>;
  findMinionById?: Maybe<Minion>;
  findMinionsByLocationId?: Maybe<Array<Maybe<Minion>>>;
  findMonitorPolicyById?: Maybe<MonitorPolicy>;
  findNodeById?: Maybe<Node>;
  getMinionCertificate?: Maybe<CertificateResponse>;
  icmpActiveDiscoveryById?: Maybe<IcmpActiveDiscovery>;
  listActiveDiscovery?: Maybe<Array<Maybe<ActiveDiscovery>>>;
  listIcmpActiveDiscovery?: Maybe<Array<Maybe<IcmpActiveDiscovery>>>;
  listMonitoryPolicies?: Maybe<Array<Maybe<MonitorPolicy>>>;
  metric?: Maybe<TimeSeriesQueryResult>;
  nodeStatus?: Maybe<NodeStatus>;
  passiveDiscoveries?: Maybe<Array<Maybe<PassiveDiscovery>>>;
  searchLocation?: Maybe<Array<Maybe<MonitoringLocation>>>;
  tags?: Maybe<Array<Maybe<Tag>>>;
  tagsByActiveDiscoveryId?: Maybe<Array<Maybe<Tag>>>;
  tagsByNodeId?: Maybe<Array<Maybe<Tag>>>;
  tagsByNodeIds?: Maybe<Array<Maybe<NodeTags>>>;
  tagsByPassiveDiscoveryId?: Maybe<Array<Maybe<Tag>>>;
};


/** Query root */
export type QueryCountAlertsArgs = {
  severityFilters?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
  timeRange?: InputMaybe<TimeRange>;
};


/** Query root */
export type QueryFindAllAlertsArgs = {
  nodeLabel?: InputMaybe<Scalars['String']>;
  page: Scalars['Int'];
  pageSize?: InputMaybe<Scalars['Int']>;
  severities?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
  sortAscending: Scalars['Boolean'];
  sortBy?: InputMaybe<Scalars['String']>;
  timeRange?: InputMaybe<TimeRange>;
};


/** Query root */
export type QueryFindAllNodesByMonitoredStateArgs = {
  monitoredState?: InputMaybe<Scalars['String']>;
};


/** Query root */
export type QueryFindAllNodesByNodeLabelSearchArgs = {
  labelSearchTerm?: InputMaybe<Scalars['String']>;
};


/** Query root */
export type QueryFindAllNodesByTagsArgs = {
  tags?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
};


/** Query root */
export type QueryFindApplicationSeriesArgs = {
  requestCriteria?: InputMaybe<RequestCriteriaInput>;
};


/** Query root */
export type QueryFindApplicationSummariesArgs = {
  requestCriteria?: InputMaybe<RequestCriteriaInput>;
};


/** Query root */
export type QueryFindApplicationsArgs = {
  requestCriteria?: InputMaybe<RequestCriteriaInput>;
};


/** Query root */
export type QueryFindEventsByNodeIdArgs = {
  id?: InputMaybe<Scalars['Long']>;
};


/** Query root */
export type QueryFindExportersArgs = {
  requestCriteria?: InputMaybe<RequestCriteriaInput>;
};


/** Query root */
export type QueryFindLocationByIdArgs = {
  id: Scalars['Long'];
};


/** Query root */
export type QueryFindMinionByIdArgs = {
  id?: InputMaybe<Scalars['String']>;
};


/** Query root */
export type QueryFindMinionsByLocationIdArgs = {
  locationId: Scalars['Long'];
};


/** Query root */
export type QueryFindMonitorPolicyByIdArgs = {
  id?: InputMaybe<Scalars['Long']>;
};


/** Query root */
export type QueryFindNodeByIdArgs = {
  id?: InputMaybe<Scalars['Long']>;
};


/** Query root */
export type QueryGetMinionCertificateArgs = {
  locationId?: InputMaybe<Scalars['Long']>;
};


/** Query root */
export type QueryIcmpActiveDiscoveryByIdArgs = {
  id?: InputMaybe<Scalars['Long']>;
};


/** Query root */
export type QueryMetricArgs = {
  labels?: InputMaybe<Scalars['Map_String_StringScalar']>;
  name?: InputMaybe<Scalars['String']>;
  timeRange?: InputMaybe<Scalars['Int']>;
  timeRangeUnit?: InputMaybe<TimeRangeUnit>;
};


/** Query root */
export type QueryNodeStatusArgs = {
  id?: InputMaybe<Scalars['Long']>;
};


/** Query root */
export type QuerySearchLocationArgs = {
  searchTerm?: InputMaybe<Scalars['String']>;
};


/** Query root */
export type QueryTagsArgs = {
  searchTerm?: InputMaybe<Scalars['String']>;
};


/** Query root */
export type QueryTagsByActiveDiscoveryIdArgs = {
  activeDiscoveryId?: InputMaybe<Scalars['Long']>;
  searchTerm?: InputMaybe<Scalars['String']>;
};


/** Query root */
export type QueryTagsByNodeIdArgs = {
  nodeId?: InputMaybe<Scalars['Long']>;
  searchTerm?: InputMaybe<Scalars['String']>;
};


/** Query root */
export type QueryTagsByNodeIdsArgs = {
  nodeIds?: InputMaybe<Array<InputMaybe<Scalars['Long']>>>;
};


/** Query root */
export type QueryTagsByPassiveDiscoveryIdArgs = {
  passiveDiscoveryId?: InputMaybe<Scalars['Long']>;
  searchTerm?: InputMaybe<Scalars['String']>;
};

export type RequestCriteriaInput = {
  applications?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
  count?: InputMaybe<Scalars['Int']>;
  exporter?: InputMaybe<Array<InputMaybe<ExporterFilterInput>>>;
  includeOther?: InputMaybe<Scalars['Boolean']>;
  step?: InputMaybe<Scalars['Int']>;
  timeRange?: InputMaybe<TimeRangeInput>;
};

export type SnmpConfig = {
  __typename?: 'SNMPConfig';
  ports?: Maybe<Array<Maybe<Scalars['Int']>>>;
  readCommunities?: Maybe<Array<Maybe<Scalars['String']>>>;
};

export type SnmpConfigInput = {
  ports?: InputMaybe<Array<InputMaybe<Scalars['Int']>>>;
  readCommunities?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
};

export enum Severity {
  Cleared = 'CLEARED',
  Critical = 'CRITICAL',
  Indeterminate = 'INDETERMINATE',
  Major = 'MAJOR',
  Minor = 'MINOR',
  Normal = 'NORMAL',
  SeverityUndefined = 'SEVERITY_UNDEFINED',
  Unrecognized = 'UNRECOGNIZED',
  Warning = 'WARNING'
}

export type SnmpInfo = {
  __typename?: 'SnmpInfo';
  community?: Maybe<Scalars['String']>;
  generic: Scalars['Int'];
  id?: Maybe<Scalars['String']>;
  specific: Scalars['Int'];
  trapOid?: Maybe<Scalars['String']>;
  version?: Maybe<Scalars['String']>;
};

export type SnmpInterface = {
  __typename?: 'SnmpInterface';
  id: Scalars['Long'];
  ifAdminStatus: Scalars['Int'];
  ifAlias?: Maybe<Scalars['String']>;
  ifDescr?: Maybe<Scalars['String']>;
  ifIndex: Scalars['Int'];
  ifName?: Maybe<Scalars['String']>;
  ifOperatorStatus: Scalars['Int'];
  ifSpeed: Scalars['Long'];
  ifType: Scalars['Int'];
  ipAddress?: Maybe<Scalars['String']>;
  nodeId: Scalars['Long'];
  physicalAddr?: Maybe<Scalars['String']>;
  tenantId?: Maybe<Scalars['String']>;
};

export type SnmpInterfaceLinkRef = {
  __typename?: 'SnmpInterfaceLinkRef';
  ifA?: Maybe<SnmpInterfaceRef>;
  ifB?: Maybe<SnmpInterfaceRef>;
};

export type SnmpInterfaceRef = {
  __typename?: 'SnmpInterfaceRef';
  ifIndex: Scalars['Long'];
  node?: Maybe<NodeRef>;
};

export type TsData = {
  __typename?: 'TSData';
  result?: Maybe<Array<Maybe<TsResult>>>;
  resultType?: Maybe<Scalars['String']>;
};

export type TsResult = {
  __typename?: 'TSResult';
  metric?: Maybe<Scalars['Map_String_StringScalar']>;
  value?: Maybe<Array<Maybe<Scalars['Float']>>>;
  values?: Maybe<Array<Maybe<Array<Maybe<Scalars['Float']>>>>>;
};

export type Tag = {
  __typename?: 'Tag';
  id: Scalars['Long'];
  name?: Maybe<Scalars['String']>;
  tenantId?: Maybe<Scalars['String']>;
};

export type TagCreateInput = {
  name?: InputMaybe<Scalars['String']>;
};

export type TagListNodesAddInput = {
  nodeIds?: InputMaybe<Array<InputMaybe<Scalars['Long']>>>;
  tags?: InputMaybe<Array<InputMaybe<TagCreateInput>>>;
};

export type TagListNodesRemoveInput = {
  nodeIds?: InputMaybe<Array<InputMaybe<Scalars['Long']>>>;
  tagIds?: InputMaybe<Array<InputMaybe<Scalars['Long']>>>;
};

export enum TimeRange {
  All = 'ALL',
  Last_24Hours = 'LAST_24_HOURS',
  SevenDays = 'SEVEN_DAYS',
  Today = 'TODAY'
}

export type TimeRangeInput = {
  endTime?: InputMaybe<Scalars['Instant']>;
  startTime?: InputMaybe<Scalars['Instant']>;
};

export enum TimeRangeUnit {
  Day = 'DAY',
  Hour = 'HOUR',
  Minute = 'MINUTE',
  Second = 'SECOND',
  Week = 'WEEK'
}

export type TimeSeriesQueryResult = {
  __typename?: 'TimeSeriesQueryResult';
  data?: Maybe<TsData>;
  status?: Maybe<Scalars['String']>;
};

export type TrafficSummary = {
  __typename?: 'TrafficSummary';
  bytesIn: Scalars['Long'];
  bytesOut: Scalars['Long'];
  label?: Maybe<Scalars['String']>;
};

export type TriggerEvent = {
  __typename?: 'TriggerEvent';
  clearEvent?: Maybe<Scalars['String']>;
  count?: Maybe<Scalars['Int']>;
  id?: Maybe<Scalars['Long']>;
  overtime?: Maybe<Scalars['Int']>;
  overtimeUnit?: Maybe<Scalars['String']>;
  severity?: Maybe<Scalars['String']>;
  tenantId?: Maybe<Scalars['String']>;
  triggerEvent?: Maybe<Scalars['String']>;
};

export type TriggerEventInput = {
  clearEvent?: InputMaybe<Scalars['String']>;
  count?: InputMaybe<Scalars['Int']>;
  id?: InputMaybe<Scalars['Long']>;
  overtime?: InputMaybe<Scalars['Int']>;
  overtimeUnit?: InputMaybe<Scalars['String']>;
  severity?: InputMaybe<Scalars['String']>;
  tenantId?: InputMaybe<Scalars['String']>;
  triggerEvent?: InputMaybe<Scalars['String']>;
};

export type AcknowledgeAlertsMutationVariables = Exact<{
  ids?: InputMaybe<Array<InputMaybe<Scalars['Long']>> | InputMaybe<Scalars['Long']>>;
}>;


export type AcknowledgeAlertsMutation = { __typename?: 'Mutation', acknowledgeAlert?: { __typename?: 'AlertResponse', alertList?: Array<{ __typename?: 'Alert', acknowledged: boolean, databaseId: any }>, alertErrorList?: Array<{ __typename?: 'AlertError', databaseId: any, error?: string }> } };

export type ClearAlertsMutationVariables = Exact<{
  ids?: InputMaybe<Array<InputMaybe<Scalars['Long']>> | InputMaybe<Scalars['Long']>>;
}>;


export type ClearAlertsMutation = { __typename?: 'Mutation', clearAlert?: { __typename?: 'AlertResponse', alertList?: Array<{ __typename?: 'Alert', acknowledged: boolean, databaseId: any }>, alertErrorList?: Array<{ __typename?: 'AlertError', databaseId: any, error?: string }> } };

export type AlertsPartsFragment = { __typename?: 'Query', findAllAlerts?: { __typename?: 'ListAlertResponse', lastPage: number, nextPage: number, totalAlerts: any, alerts?: Array<{ __typename?: 'Alert', acknowledged: boolean, description?: string, lastUpdateTimeMs: any, severity?: Severity, label?: string, nodeName?: string }> } };

export type AlertsListQueryVariables = Exact<{
  page: Scalars['Int'];
  pageSize?: InputMaybe<Scalars['Int']>;
  severities?: InputMaybe<Array<InputMaybe<Scalars['String']>> | InputMaybe<Scalars['String']>>;
  sortAscending: Scalars['Boolean'];
  sortBy?: InputMaybe<Scalars['String']>;
  timeRange: TimeRange;
  nodeLabel?: InputMaybe<Scalars['String']>;
}>;


export type AlertsListQuery = { __typename?: 'Query', findAllAlerts?: { __typename?: 'ListAlertResponse', lastPage: number, nextPage: number, totalAlerts: any, alerts?: Array<{ __typename?: 'Alert', acknowledged: boolean, description?: string, lastUpdateTimeMs: any, severity?: Severity, label?: string, nodeName?: string }> } };

export type CountAlertsQueryVariables = Exact<{
  severityFilters?: InputMaybe<Array<InputMaybe<Scalars['String']>> | InputMaybe<Scalars['String']>>;
  timeRange: TimeRange;
}>;


export type CountAlertsQuery = { __typename?: 'Query', countAlerts?: { __typename?: 'CountAlertResponse', count: number, error?: string } };

export type CreateAzureActiveDiscoveryMutationVariables = Exact<{
  discovery: AzureActiveDiscoveryCreateInput;
}>;


export type CreateAzureActiveDiscoveryMutation = { __typename?: 'Mutation', createAzureActiveDiscovery?: { __typename?: 'AzureActiveDiscovery', createTimeMsec?: any, locationId?: string, subscriptionId?: string, clientId?: string } };

export type CreateIcmpActiveDiscoveryMutationVariables = Exact<{
  request: IcmpActiveDiscoveryCreateInput;
}>;


export type CreateIcmpActiveDiscoveryMutation = { __typename?: 'Mutation', createIcmpActiveDiscovery?: { __typename?: 'IcmpActiveDiscovery', name?: string, ipAddresses?: Array<string>, locationId?: string, snmpConfig?: { __typename?: 'SNMPConfig', ports?: Array<number>, readCommunities?: Array<string> } } };

export type TogglePassiveDiscoveryMutationVariables = Exact<{
  toggle: PassiveDiscoveryToggleInput;
}>;


export type TogglePassiveDiscoveryMutation = { __typename?: 'Mutation', togglePassiveDiscovery?: { __typename?: 'PassiveDiscoveryToggle', id: any, toggle: boolean } };

export type UpsertPassiveDiscoveryMutationVariables = Exact<{
  passiveDiscovery: PassiveDiscoveryUpsertInput;
}>;


export type UpsertPassiveDiscoveryMutation = { __typename?: 'Mutation', upsertPassiveDiscovery?: { __typename?: 'PassiveDiscovery', id?: any, location?: string, name?: string, snmpCommunities?: Array<string>, snmpPorts?: Array<number>, toggle: boolean } };

export type FindApplicationSeriesQueryVariables = Exact<{
  requestCriteria: RequestCriteriaInput;
}>;


export type FindApplicationSeriesQuery = { __typename?: 'Query', findApplicationSeries?: Array<{ __typename?: 'FlowingPoint', timestamp?: any, label?: string, value: number, direction?: string }> };

export type FindApplicationSummariesQueryVariables = Exact<{
  requestCriteria: RequestCriteriaInput;
}>;


export type FindApplicationSummariesQuery = { __typename?: 'Query', findApplicationSummaries?: Array<{ __typename?: 'TrafficSummary', label?: string, bytesIn: any, bytesOut: any }> };

export type FindApplicationsQueryVariables = Exact<{
  requestCriteria: RequestCriteriaInput;
}>;


export type FindApplicationsQuery = { __typename?: 'Query', findApplications?: Array<string> };

export type FindExportersQueryVariables = Exact<{
  requestCriteria: RequestCriteriaInput;
}>;


export type FindExportersQuery = { __typename?: 'Query', findExporters?: Array<{ __typename?: 'Exporter', node?: { __typename?: 'Node', id: any, nodeLabel?: string }, ipInterface?: { __typename?: 'IpInterface', id: any, ipAddress?: string } }> };

export type CreateLocationMutationVariables = Exact<{
  location: MonitoringLocationCreateInput;
}>;


export type CreateLocationMutation = { __typename?: 'Mutation', createLocation?: { __typename?: 'MonitoringLocation', id: any, location?: string, address?: string, longitude?: number, latitude?: number, tenantId?: string } };

export type UpdateLocationMutationVariables = Exact<{
  location?: InputMaybe<MonitoringLocationUpdateInput>;
}>;


export type UpdateLocationMutation = { __typename?: 'Mutation', updateLocation?: { __typename?: 'MonitoringLocation', id: any, location?: string, address?: string, longitude?: number, latitude?: number, tenantId?: string } };

export type DeleteLocationMutationVariables = Exact<{
  id: Scalars['Long'];
}>;


export type DeleteLocationMutation = { __typename?: 'Mutation', deleteLocation?: boolean };

export type LocationsPartsFragment = { __typename?: 'Query', findAllLocations?: Array<{ __typename?: 'MonitoringLocation', id: any, location?: string, address?: string, longitude?: number, latitude?: number }> };

export type LocationsListQueryVariables = Exact<{ [key: string]: never; }>;


export type LocationsListQuery = { __typename?: 'Query', findAllLocations?: Array<{ __typename?: 'MonitoringLocation', id: any, location?: string, address?: string, longitude?: number, latitude?: number }> };

export type SearchLocationQueryVariables = Exact<{
  searchTerm?: InputMaybe<Scalars['String']>;
}>;


export type SearchLocationQuery = { __typename?: 'Query', searchLocation?: Array<{ __typename?: 'MonitoringLocation', id: any, location?: string, address?: string, longitude?: number, latitude?: number }> };

export type GetMinionCertificateQueryVariables = Exact<{
  location?: InputMaybe<Scalars['Long']>;
}>;


export type GetMinionCertificateQuery = { __typename?: 'Query', getMinionCertificate?: { __typename?: 'CertificateResponse', password?: string, certificate?: any } };

export type DeviceUptimePartsFragment = { __typename?: 'Query', deviceUptime?: { __typename?: 'TimeSeriesQueryResult', data?: { __typename?: 'TSData', result?: Array<{ __typename?: 'TSResult', metric?: any, values?: Array<Array<number>> }> } } };

export type DeviceLatencyPartsFragment = { __typename?: 'Query', deviceLatency?: { __typename?: 'TimeSeriesQueryResult', data?: { __typename?: 'TSData', result?: Array<{ __typename?: 'TSResult', metric?: any, values?: Array<Array<number>> }> } } };

export type MetricPartsFragment = { __typename?: 'TimeSeriesQueryResult', data?: { __typename?: 'TSData', result?: Array<{ __typename?: 'TSResult', metric?: any, values?: Array<Array<number>> }> } };

export type TimeSeriesMetricFragment = { __typename?: 'Query', metric?: { __typename?: 'TimeSeriesQueryResult', data?: { __typename?: 'TSData', result?: Array<{ __typename?: 'TSResult', metric?: any, values?: Array<Array<number>> }> } } };

export type MinionUptimePartsFragment = { __typename?: 'Query', minionUptime?: { __typename?: 'TimeSeriesQueryResult', data?: { __typename?: 'TSData', result?: Array<{ __typename?: 'TSResult', metric?: any, values?: Array<Array<number>> }> } } };

export type MinionLatencyPartsFragment = { __typename?: 'Query', minionLatency?: { __typename?: 'TimeSeriesQueryResult', status?: string, data?: { __typename?: 'TSData', result?: Array<{ __typename?: 'TSResult', metric?: any, values?: Array<Array<number>> }> } } };

export type NodeLatencyPartsFragment = { __typename?: 'Query', nodeLatency?: { __typename?: 'TimeSeriesQueryResult', status?: string, data?: { __typename?: 'TSData', result?: Array<{ __typename?: 'TSResult', metric?: any, values?: Array<Array<number>> }> } } };

export type DeleteMinionMutationVariables = Exact<{
  id: Scalars['String'];
}>;


export type DeleteMinionMutation = { __typename?: 'Mutation', deleteMinion?: boolean };

export type FindMinionsByLocationIdQueryVariables = Exact<{
  locationId: Scalars['Long'];
}>;


export type FindMinionsByLocationIdQuery = { __typename?: 'Query', findMinionsByLocationId?: Array<{ __typename?: 'Minion', id: any, label?: string, lastCheckedTime: any, status?: string, systemId?: string, location?: { __typename?: 'MonitoringLocation', id: any, location?: string } }> };

export type CreateMonitorPolicyMutationVariables = Exact<{
  policy: MonitorPolicyInput;
}>;


export type CreateMonitorPolicyMutation = { __typename?: 'Mutation', createMonitorPolicy?: { __typename?: 'MonitorPolicy', id?: any } };

export type AddNodeMutationVariables = Exact<{
  node: NodeCreateInput;
}>;


export type AddNodeMutation = { __typename?: 'Mutation', addNode?: { __typename?: 'Node', createTime: any, id: any, monitoringLocationId: any, nodeLabel?: string, tenantId?: string } };

export type DeleteNodeMutationVariables = Exact<{
  id: Scalars['Long'];
}>;


export type DeleteNodeMutation = { __typename?: 'Mutation', deleteNode?: boolean };

export type AddTagsToNodesMutationVariables = Exact<{
  tags: TagListNodesAddInput;
}>;


export type AddTagsToNodesMutation = { __typename?: 'Mutation', addTagsToNodes?: Array<{ __typename?: 'Tag', id: any, name?: string, tenantId?: string }> };

export type RemoveTagsFromNodesMutationVariables = Exact<{
  tags: TagListNodesRemoveInput;
}>;


export type RemoveTagsFromNodesMutation = { __typename?: 'Mutation', removeTagsFromNodes?: boolean };

export type NodeStatusPartsFragment = { __typename?: 'Query', nodeStatus?: { __typename?: 'NodeStatus', id: any, status?: string } };

export type NodesPartsFragment = { __typename?: 'Query', findAllNodes?: Array<{ __typename?: 'Node', createTime: any, id: any, monitoringLocationId: any, nodeLabel?: string, tenantId?: string, scanType?: string, ipInterfaces?: Array<{ __typename?: 'IpInterface', id: any, ipAddress?: string, nodeId: any, tenantId?: string, snmpPrimary?: boolean }>, location?: { __typename?: 'MonitoringLocation', id: any, location?: string, tenantId?: string } }> };

export type SavePagerDutyConfigMutationVariables = Exact<{
  config: PagerDutyConfigInput;
}>;


export type SavePagerDutyConfigMutation = { __typename?: 'Mutation', savePagerDutyConfig?: boolean };

export type TagsPartsFragment = { __typename?: 'Query', tags?: Array<{ __typename?: 'Tag', id: any, name?: string, tenantId?: string }> };

export type ListTagsQueryVariables = Exact<{ [key: string]: never; }>;


export type ListTagsQuery = { __typename?: 'Query', tags?: Array<{ __typename?: 'Tag', id: any, name?: string, tenantId?: string }> };

export type TagsSearchPartsFragment = { __typename?: 'Query', tags?: Array<{ __typename?: 'Tag', id: any, name?: string, tenantId?: string }> };

export type ListTagsSearchQueryVariables = Exact<{
  searchTerm?: InputMaybe<Scalars['String']>;
}>;


export type ListTagsSearchQuery = { __typename?: 'Query', tags?: Array<{ __typename?: 'Tag', id: any, name?: string, tenantId?: string }> };

export type ListTagsByNodeIdsQueryVariables = Exact<{
  nodeIds?: InputMaybe<Array<InputMaybe<Scalars['Long']>> | InputMaybe<Scalars['Long']>>;
}>;


export type ListTagsByNodeIdsQuery = { __typename?: 'Query', tagsByNodeIds?: Array<{ __typename?: 'NodeTags', nodeId: any, tags?: Array<{ __typename?: 'Tag', id: any, name?: string }> }> };

export type NodesTablePartsFragment = { __typename?: 'Query', findAllNodes?: Array<{ __typename?: 'Node', id: any, nodeLabel?: string, tenantId?: string, createTime: any, monitoringLocationId: any, scanType?: string, ipInterfaces?: Array<{ __typename?: 'IpInterface', ipAddress?: string, snmpPrimary?: boolean }> }> };

export type MinionsTablePartsFragment = { __typename?: 'Query', findAllMinions?: Array<{ __typename?: 'Minion', id: any, label?: string, lastCheckedTime: any, status?: string, systemId?: string, location?: { __typename?: 'MonitoringLocation', id: any, location?: string } }> };

export type ListNodesForTableQueryVariables = Exact<{ [key: string]: never; }>;


export type ListNodesForTableQuery = { __typename?: 'Query', findAllNodes?: Array<{ __typename?: 'Node', id: any, nodeLabel?: string, tenantId?: string, createTime: any, monitoringLocationId: any, scanType?: string, ipInterfaces?: Array<{ __typename?: 'IpInterface', ipAddress?: string, snmpPrimary?: boolean }> }> };

export type ListMinionsForTableQueryVariables = Exact<{ [key: string]: never; }>;


export type Query = { __typename?: 'Query', findAllMinions?: Array<{ __typename?: 'Minion', id: any, label?: string, lastCheckedTime: any, status?: string, systemId?: string, location?: { __typename?: 'MonitoringLocation', id: any, location?: string } }> };

export type ListMinionMetricsQueryVariables = Exact<{
  instance: Scalars['String'];
  monitor: Scalars['String'];
  timeRange: Scalars['Int'];
  timeRangeUnit: TimeRangeUnit;
}>;


export type ListMinionMetricsQuery = { __typename?: 'Query', minionLatency?: { __typename?: 'TimeSeriesQueryResult', status?: string, data?: { __typename?: 'TSData', result?: Array<{ __typename?: 'TSResult', metric?: any, values?: Array<Array<number>> }> } } };

export type ListNodeMetricsQueryVariables = Exact<{
  id: Scalars['Long'];
  monitor: Scalars['String'];
  instance: Scalars['String'];
  timeRange: Scalars['Int'];
  timeRangeUnit: TimeRangeUnit;
}>;


export type ListNodeMetricsQuery = { __typename?: 'Query', nodeLatency?: { __typename?: 'TimeSeriesQueryResult', status?: string, data?: { __typename?: 'TSData', result?: Array<{ __typename?: 'TSResult', metric?: any, values?: Array<Array<number>> }> } }, nodeStatus?: { __typename?: 'NodeStatus', id: any, status?: string } };

export type ListMinionsAndDevicesForTablesQueryVariables = Exact<{ [key: string]: never; }>;


export type ListMinionsAndDevicesForTablesQuery = { __typename?: 'Query', findAllNodes?: Array<{ __typename?: 'Node', id: any, nodeLabel?: string, tenantId?: string, createTime: any, monitoringLocationId: any, scanType?: string, ipInterfaces?: Array<{ __typename?: 'IpInterface', ipAddress?: string, snmpPrimary?: boolean }> }>, findAllMinions?: Array<{ __typename?: 'Minion', id: any, label?: string, lastCheckedTime: any, status?: string, systemId?: string, location?: { __typename?: 'MonitoringLocation', id: any, location?: string } }>, findAllLocations?: Array<{ __typename?: 'MonitoringLocation', id: any, location?: string, address?: string, longitude?: number, latitude?: number }> };

export type NetworkTrafficQueryVariables = Exact<{
  name: Scalars['String'];
  timeRange: Scalars['Int'];
  timeRangeUnit: TimeRangeUnit;
}>;


export type NetworkTrafficQuery = { __typename?: 'Query', metric?: { __typename?: 'TimeSeriesQueryResult', status?: string, data?: { __typename?: 'TSData', result?: Array<{ __typename?: 'TSResult', metric?: any, values?: Array<Array<number>> }> } } };

export type ListLocationsForDiscoveryQueryVariables = Exact<{ [key: string]: never; }>;


export type ListLocationsForDiscoveryQuery = { __typename?: 'Query', findAllLocations?: Array<{ __typename?: 'MonitoringLocation', id: any, location?: string, address?: string, longitude?: number, latitude?: number }> };

export type ListDiscoveriesQueryVariables = Exact<{ [key: string]: never; }>;


export type ListDiscoveriesQuery = { __typename?: 'Query', passiveDiscoveries?: Array<{ __typename?: 'PassiveDiscovery', id?: any, location?: string, name?: string, snmpCommunities?: Array<string>, snmpPorts?: Array<number>, toggle: boolean }>, listActiveDiscovery?: Array<{ __typename?: 'ActiveDiscovery', details?: any, discoveryType?: string }> };

export type TagsByActiveDiscoveryIdQueryVariables = Exact<{
  discoveryId: Scalars['Long'];
}>;


export type TagsByActiveDiscoveryIdQuery = { __typename?: 'Query', tagsByActiveDiscoveryId?: Array<{ __typename?: 'Tag', id: any, name?: string, tenantId?: string }> };

export type TagsByPassiveDiscoveryIdQueryVariables = Exact<{
  discoveryId: Scalars['Long'];
}>;


export type TagsByPassiveDiscoveryIdQuery = { __typename?: 'Query', tagsByPassiveDiscoveryId?: Array<{ __typename?: 'Tag', id: any, name?: string, tenantId?: string }> };

export type GetMetricQueryVariables = Exact<{
  metric: Scalars['String'];
}>;


export type GetMetricQuery = { __typename?: 'Query', metric?: { __typename?: 'TimeSeriesQueryResult', data?: { __typename?: 'TSData', result?: Array<{ __typename?: 'TSResult', metric?: any, values?: Array<Array<number>> }> } } };

export type GetTimeSeriesMetricQueryVariables = Exact<{
  name: Scalars['String'];
  monitor: Scalars['String'];
  nodeId?: InputMaybe<Scalars['String']>;
  timeRange: Scalars['Int'];
  timeRangeUnit: TimeRangeUnit;
  instance?: InputMaybe<Scalars['String']>;
}>;


export type GetTimeSeriesMetricQuery = { __typename?: 'Query', metric?: { __typename?: 'TimeSeriesQueryResult', data?: { __typename?: 'TSData', result?: Array<{ __typename?: 'TSResult', metric?: any, values?: Array<Array<number>> }> } } };

export type GetTimeSeriesMetricsWithIfNameQueryVariables = Exact<{
  name: Scalars['String'];
  monitor: Scalars['String'];
  nodeId?: InputMaybe<Scalars['String']>;
  timeRange: Scalars['Int'];
  timeRangeUnit: TimeRangeUnit;
  ifName?: InputMaybe<Scalars['String']>;
}>;


export type GetTimeSeriesMetricsWithIfNameQuery = { __typename?: 'Query', metric?: { __typename?: 'TimeSeriesQueryResult', data?: { __typename?: 'TSData', result?: Array<{ __typename?: 'TSResult', metric?: any, values?: Array<Array<number>> }> } } };

export type GetNodeForGraphsQueryVariables = Exact<{
  id?: InputMaybe<Scalars['Long']>;
}>;


export type GetNodeForGraphsQuery = { __typename?: 'Query', findNodeById?: { __typename?: 'Node', id: any, scanType?: string, ipInterfaces?: Array<{ __typename?: 'IpInterface', ipAddress?: string, snmpPrimary?: boolean }> } };

export type NodesListQueryVariables = Exact<{ [key: string]: never; }>;


export type NodesListQuery = { __typename?: 'Query', findAllNodes?: Array<{ __typename?: 'Node', createTime: any, id: any, monitoringLocationId: any, nodeLabel?: string, tenantId?: string, scanType?: string, ipInterfaces?: Array<{ __typename?: 'IpInterface', id: any, ipAddress?: string, nodeId: any, tenantId?: string, snmpPrimary?: boolean }>, location?: { __typename?: 'MonitoringLocation', id: any, location?: string, tenantId?: string } }> };

export type NodeLatencyMetricQueryVariables = Exact<{
  id: Scalars['Long'];
  monitor: Scalars['String'];
  instance: Scalars['String'];
  timeRange: Scalars['Int'];
  timeRangeUnit: TimeRangeUnit;
}>;


export type NodeLatencyMetricQuery = { __typename?: 'Query', nodeLatency?: { __typename?: 'TimeSeriesQueryResult', status?: string, data?: { __typename?: 'TSData', result?: Array<{ __typename?: 'TSResult', metric?: any, values?: Array<Array<number>> }> } }, nodeStatus?: { __typename?: 'NodeStatus', id: any, status?: string } };

export type FindAllNodesByNodeLabelSearchQueryVariables = Exact<{
  labelSearchTerm: Scalars['String'];
}>;


export type FindAllNodesByNodeLabelSearchQuery = { __typename?: 'Query', findAllNodesByNodeLabelSearch?: Array<{ __typename?: 'Node', id: any, monitoringLocationId: any, nodeLabel?: string, ipInterfaces?: Array<{ __typename?: 'IpInterface', id: any, ipAddress?: string, nodeId: any, snmpPrimary?: boolean }>, location?: { __typename?: 'MonitoringLocation', id: any, location?: string } }> };

export type FindAllNodesByTagsQueryVariables = Exact<{
  tags?: InputMaybe<Array<InputMaybe<Scalars['String']>> | InputMaybe<Scalars['String']>>;
}>;


export type FindAllNodesByTagsQuery = { __typename?: 'Query', findAllNodesByTags?: Array<{ __typename?: 'Node', id: any, monitoringLocationId: any, nodeLabel?: string, ipInterfaces?: Array<{ __typename?: 'IpInterface', id: any, ipAddress?: string, nodeId: any, snmpPrimary?: boolean }>, location?: { __typename?: 'MonitoringLocation', id: any, location?: string } }> };

export type FindAllNodesByMonitoredStateQueryVariables = Exact<{
  monitoredState: Scalars['String'];
}>;


export type FindAllNodesByMonitoredStateQuery = { __typename?: 'Query', findAllNodesByMonitoredState?: Array<{ __typename?: 'Node', id: any, monitoringLocationId: any, nodeLabel?: string, ipInterfaces?: Array<{ __typename?: 'IpInterface', id: any, ipAddress?: string, nodeId: any, snmpPrimary?: boolean }>, location?: { __typename?: 'MonitoringLocation', id: any, location?: string } }> };

export type NodesForMapQueryVariables = Exact<{ [key: string]: never; }>;


export type NodesForMapQuery = { __typename?: 'Query', findAllNodes?: Array<{ __typename?: 'Node', id: any, nodeLabel?: string }> };

export type MonitoringPolicyPartsFragment = { __typename?: 'MonitorPolicy', id?: any, memo?: string, name?: string, notifyByEmail?: boolean, notifyByPagerDuty?: boolean, notifyByWebhooks?: boolean, tags?: Array<string>, rules?: Array<{ __typename?: 'PolicyRule', id?: any, name?: string, componentType?: string, triggerEvents?: Array<{ __typename?: 'TriggerEvent', id?: any, count?: number, clearEvent?: string, overtime?: number, overtimeUnit?: string, severity?: string, triggerEvent?: string }> }> };

export type ListMonitoryPoliciesQueryVariables = Exact<{ [key: string]: never; }>;


export type ListMonitoryPoliciesQuery = { __typename?: 'Query', listMonitoryPolicies?: Array<{ __typename?: 'MonitorPolicy', id?: any, memo?: string, name?: string, notifyByEmail?: boolean, notifyByPagerDuty?: boolean, notifyByWebhooks?: boolean, tags?: Array<string>, rules?: Array<{ __typename?: 'PolicyRule', id?: any, name?: string, componentType?: string, triggerEvents?: Array<{ __typename?: 'TriggerEvent', id?: any, count?: number, clearEvent?: string, overtime?: number, overtimeUnit?: string, severity?: string, triggerEvent?: string }> }> }>, defaultPolicy?: { __typename?: 'MonitorPolicy', id?: any, memo?: string, name?: string, notifyByEmail?: boolean, notifyByPagerDuty?: boolean, notifyByWebhooks?: boolean, tags?: Array<string>, rules?: Array<{ __typename?: 'PolicyRule', id?: any, name?: string, componentType?: string, triggerEvents?: Array<{ __typename?: 'TriggerEvent', id?: any, count?: number, clearEvent?: string, overtime?: number, overtimeUnit?: string, severity?: string, triggerEvent?: string }> }> } };

export type EventsByNodeIdPartsFragment = { __typename?: 'Query', events?: Array<{ __typename?: 'Event', id: number, uei?: string, nodeId: number, ipAddress?: string, producedTime: any }> };

export type NodeByIdPartsFragment = { __typename?: 'Query', node?: { __typename?: 'Node', id: any, nodeLabel?: string, objectId?: string, systemContact?: string, systemDescr?: string, systemLocation?: string, systemName?: string, scanType?: string, location?: { __typename?: 'MonitoringLocation', location?: string }, ipInterfaces?: Array<{ __typename?: 'IpInterface', id: any, hostname?: string, ipAddress?: string, netmask?: string, nodeId: any, snmpPrimary?: boolean }>, snmpInterfaces?: Array<{ __typename?: 'SnmpInterface', id: any, ifAdminStatus: number, ifAlias?: string, ifDescr?: string, ifIndex: number, ifName?: string, ifOperatorStatus: number, ifSpeed: any, ifType: number, ipAddress?: string, nodeId: any, physicalAddr?: string }> } };

export type ListNodeStatusQueryVariables = Exact<{
  id?: InputMaybe<Scalars['Long']>;
}>;


export type ListNodeStatusQuery = { __typename?: 'Query', events?: Array<{ __typename?: 'Event', id: number, uei?: string, nodeId: number, ipAddress?: string, producedTime: any }>, node?: { __typename?: 'Node', id: any, nodeLabel?: string, objectId?: string, systemContact?: string, systemDescr?: string, systemLocation?: string, systemName?: string, scanType?: string, location?: { __typename?: 'MonitoringLocation', location?: string }, ipInterfaces?: Array<{ __typename?: 'IpInterface', id: any, hostname?: string, ipAddress?: string, netmask?: string, nodeId: any, snmpPrimary?: boolean }>, snmpInterfaces?: Array<{ __typename?: 'SnmpInterface', id: any, ifAdminStatus: number, ifAlias?: string, ifDescr?: string, ifIndex: number, ifName?: string, ifOperatorStatus: number, ifSpeed: any, ifType: number, ipAddress?: string, nodeId: any, physicalAddr?: string }> } };

export type FindLocationsForWelcomeQueryVariables = Exact<{ [key: string]: never; }>;


export type FindLocationsForWelcomeQuery = { __typename?: 'Query', findAllLocations?: Array<{ __typename?: 'MonitoringLocation', id: any, location?: string }> };

export type FindDevicesForWelcomeQueryVariables = Exact<{ [key: string]: never; }>;


export type FindDevicesForWelcomeQuery = { __typename?: 'Query', findAllNodes?: Array<{ __typename?: 'Node', id: any, nodeLabel?: string, createTime: any, ipInterfaces?: Array<{ __typename?: 'IpInterface', id: any, nodeId: any, ipAddress?: string, snmpPrimary?: boolean }> }> };

export type FindMinionsForWelcomeQueryVariables = Exact<{
  locationId: Scalars['Long'];
}>;


export type FindMinionsForWelcomeQuery = { __typename?: 'Query', findMinionsByLocationId?: Array<{ __typename?: 'Minion', id: any }> };

export type DownloadMinionCertificateForWelcomeQueryVariables = Exact<{
  location?: InputMaybe<Scalars['Long']>;
}>;


export type DownloadMinionCertificateForWelcomeQuery = { __typename?: 'Query', getMinionCertificate?: { __typename?: 'CertificateResponse', password?: string, certificate?: any } };
