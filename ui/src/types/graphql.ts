import { TypedDocumentNode as DocumentNode } from '@graphql-typed-document-node/core'

export type Maybe<T> = T
export type InputMaybe<T> = T
export type Exact<T extends { [key: string]: unknown }> = { [K in keyof T]: T[K] }
export type MakeOptional<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]?: Maybe<T[SubKey]> }
export type MakeMaybe<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]: Maybe<T[SubKey]> }

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
  Map_String_LongScalar: any;
  Map_String_StringScalar: any;
}

export type ActiveDiscovery = {
  __typename?: 'ActiveDiscovery';
  details?: Maybe<Scalars['Json']>;
  discoveryType?: Maybe<Scalars['String']>;
}

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
  policyNameList?: Maybe<Array<Maybe<Scalars['String']>>>;
  reductionKey?: Maybe<Scalars['String']>;
  ruleNameList?: Maybe<Array<Maybe<Scalars['String']>>>;
  severity?: Maybe<Severity>;
  type?: Maybe<AlertType>;
  uei?: Maybe<Scalars['String']>;
}

export type AlertCondition = {
  __typename?: 'AlertCondition';
  clearEvent?: Maybe<AlertEventDefinition>;
  count?: Maybe<Scalars['Int']>;
  id?: Maybe<Scalars['Long']>;
  overtime?: Maybe<Scalars['Int']>;
  overtimeUnit?: Maybe<Scalars['String']>;
  severity?: Maybe<Scalars['String']>;
  triggerEvent?: Maybe<AlertEventDefinition>;
}

export type AlertConditionInput = {
  clearEvent?: InputMaybe<AlertEventDefinitionInput>;
  count?: InputMaybe<Scalars['Int']>;
  id?: InputMaybe<Scalars['Long']>;
  overtime?: InputMaybe<Scalars['Int']>;
  overtimeUnit?: InputMaybe<Scalars['String']>;
  severity?: InputMaybe<Scalars['String']>;
  triggerEvent?: InputMaybe<AlertEventDefinitionInput>;
}

export type AlertCount = {
  __typename?: 'AlertCount';
  acknowledgedCount?: Maybe<Scalars['Long']>;
  countBySeverity?: Maybe<Scalars['Map_String_LongScalar']>;
  totalAlertCount?: Maybe<Scalars['Long']>;
}

export type AlertError = {
  __typename?: 'AlertError';
  databaseId: Scalars['Long'];
  error?: Maybe<Scalars['String']>;
}

export type AlertEventDefinition = {
  __typename?: 'AlertEventDefinition';
  clearKey?: Maybe<Scalars['String']>;
  eventType?: Maybe<EventType>;
  id?: Maybe<Scalars['Long']>;
  name?: Maybe<Scalars['String']>;
  reductionKey?: Maybe<Scalars['String']>;
  uei?: Maybe<Scalars['String']>;
}

export type AlertEventDefinitionInput = {
  clearKey?: InputMaybe<Scalars['String']>;
  eventType?: InputMaybe<EventType>;
  id?: InputMaybe<Scalars['Long']>;
  name?: InputMaybe<Scalars['String']>;
  reductionKey?: InputMaybe<Scalars['String']>;
  uei?: InputMaybe<Scalars['String']>;
}

export type AlertResponse = {
  __typename?: 'AlertResponse';
  alertErrorList?: Maybe<Array<Maybe<AlertError>>>;
  alertList?: Maybe<Array<Maybe<Alert>>>;
}

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
}

export type AzureActiveDiscoveryCreateInput = {
  clientId?: InputMaybe<Scalars['String']>;
  clientSecret?: InputMaybe<Scalars['String']>;
  directoryId?: InputMaybe<Scalars['String']>;
  locationId?: InputMaybe<Scalars['String']>;
  name?: InputMaybe<Scalars['String']>;
  subscriptionId?: InputMaybe<Scalars['String']>;
  tags?: InputMaybe<Array<InputMaybe<TagCreateInput>>>;
}

export type AzureInterface = {
  __typename?: 'AzureInterface';
  id: Scalars['Long'];
  interfaceName?: Maybe<Scalars['String']>;
  location?: Maybe<Scalars['String']>;
  nodeId: Scalars['Long'];
  privateIpId?: Maybe<Scalars['String']>;
  publicIpAddress?: Maybe<Scalars['String']>;
  publicIpId?: Maybe<Scalars['String']>;
  tenantId?: Maybe<Scalars['String']>;
}

export type CertificateResponse = {
  __typename?: 'CertificateResponse';
  certificate?: Maybe<Scalars['Base64String']>;
  password?: Maybe<Scalars['String']>;
}

export type CountAlertResponse = {
  __typename?: 'CountAlertResponse';
  count: Scalars['Int'];
  error?: Maybe<Scalars['String']>;
}

export type DeleteAlertResponse = {
  __typename?: 'DeleteAlertResponse';
  alertDatabaseIdList?: Maybe<Array<Maybe<Scalars['Long']>>>;
  alertErrorList?: Maybe<Array<Maybe<AlertError>>>;
}

export enum DetectionMethod {
  Event = 'EVENT',
  Threshold = 'THRESHOLD',
  Unrecognized = 'UNRECOGNIZED'
}

export enum DownloadFormat {
  Csv = 'CSV'
}

export type Event = {
  __typename?: 'Event';
  eventInfo?: Maybe<EventInfo>;
  eventParams?: Maybe<Array<Maybe<EventParameter>>>;
  id: Scalars['Int'];
  ipAddress?: Maybe<Scalars['String']>;
  nodeId: Scalars['Int'];
  producedTime: Scalars['Long'];
  uei?: Maybe<Scalars['String']>;
}

export type EventInfo = {
  __typename?: 'EventInfo';
  snmp?: Maybe<SnmpInfo>;
}

export type EventParameter = {
  __typename?: 'EventParameter';
  encoding?: Maybe<Scalars['String']>;
  name?: Maybe<Scalars['String']>;
  type?: Maybe<Scalars['String']>;
  value?: Maybe<Scalars['String']>;
}

export enum EventType {
  Internal = 'INTERNAL',
  SnmpTrap = 'SNMP_TRAP',
  Unrecognized = 'UNRECOGNIZED'
}

export type Exporter = {
  __typename?: 'Exporter';
  ipInterface?: Maybe<IpInterface>;
  node?: Maybe<Node>;
  snmpInterface?: Maybe<SnmpInterface>;
}

export type ExporterFilterInput = {
  ipInterfaceId?: InputMaybe<Scalars['Long']>;
  nodeId?: InputMaybe<Scalars['Long']>;
}

export type FlowingPoint = {
  __typename?: 'FlowingPoint';
  direction?: Maybe<Scalars['String']>;
  label?: Maybe<Scalars['String']>;
  timestamp?: Maybe<Scalars['Instant']>;
  value: Scalars['Float'];
}

export type IcmpActiveDiscovery = {
  __typename?: 'IcmpActiveDiscovery';
  id: Scalars['Long'];
  ipAddresses?: Maybe<Array<Maybe<Scalars['String']>>>;
  locationId?: Maybe<Scalars['String']>;
  name?: Maybe<Scalars['String']>;
  snmpConfig?: Maybe<SnmpConfig>;
}

export type IcmpActiveDiscoveryCreateInput = {
  id?: InputMaybe<Scalars['Long']>;
  ipAddresses?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
  locationId?: InputMaybe<Scalars['String']>;
  name?: InputMaybe<Scalars['String']>;
  snmpConfig?: InputMaybe<SnmpConfigInput>;
  tags?: InputMaybe<Array<InputMaybe<TagCreateInput>>>;
}

export type IpInterface = {
  __typename?: 'IpInterface';
  azureInterfaceId?: Maybe<Scalars['Long']>;
  hostname?: Maybe<Scalars['String']>;
  id: Scalars['Long'];
  ipAddress?: Maybe<Scalars['String']>;
  netmask?: Maybe<Scalars['String']>;
  nodeId: Scalars['Long'];
  snmpPrimary?: Maybe<Scalars['Boolean']>;
}

export type ListAlertResponse = {
  __typename?: 'ListAlertResponse';
  alerts?: Maybe<Array<Maybe<Alert>>>;
  lastPage: Scalars['Int'];
  nextPage: Scalars['Int'];
  totalAlerts: Scalars['Long'];
}

export type ManagedObject = {
  __typename?: 'ManagedObject';
  instance?: Maybe<ManagedObjectInstance>;
  type?: Maybe<ManagedObjectType>;
}

export type ManagedObjectInstance = {
  __typename?: 'ManagedObjectInstance';
  nodeVal?: Maybe<NodeRef>;
  snmpInterfaceLinkVal?: Maybe<SnmpInterfaceLinkRef>;
  snmpInterfaceVal?: Maybe<SnmpInterfaceRef>;
}

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
}

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
  enabled?: Maybe<Scalars['Boolean']>;
}

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
}

export type MonitoringLocation = {
  __typename?: 'MonitoringLocation';
  address?: Maybe<Scalars['String']>;
  id: Scalars['Long'];
  latitude?: Maybe<Scalars['Float']>;
  location?: Maybe<Scalars['String']>;
  longitude?: Maybe<Scalars['Float']>;
}

export type MonitoringLocationCreateInput = {
  address?: InputMaybe<Scalars['String']>;
  latitude?: InputMaybe<Scalars['Float']>;
  location?: InputMaybe<Scalars['String']>;
  longitude?: InputMaybe<Scalars['Float']>;
}

export type MonitoringLocationUpdateInput = {
  address?: InputMaybe<Scalars['String']>;
  id: Scalars['Long'];
  latitude?: InputMaybe<Scalars['Float']>;
  location?: InputMaybe<Scalars['String']>;
  longitude?: InputMaybe<Scalars['Float']>;
}

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
  deleteIcmpActiveDiscovery?: Maybe<Scalars['Boolean']>;
  deleteLocation?: Maybe<Scalars['Boolean']>;
  deleteMinion?: Maybe<Scalars['Boolean']>;
  deleteNode?: Maybe<Scalars['Boolean']>;
  deletePassiveDiscovery?: Maybe<Scalars['Boolean']>;
  deletePolicyById?: Maybe<Scalars['Boolean']>;
  deleteRuleById?: Maybe<Scalars['Boolean']>;
  discoveryByNodeIds?: Maybe<Scalars['Boolean']>;
  escalateAlert?: Maybe<AlertResponse>;
  removeTagsFromNodes?: Maybe<Scalars['Boolean']>;
  revokeMinionCertificate?: Maybe<Scalars['Boolean']>;
  savePagerDutyConfig?: Maybe<Scalars['Boolean']>;
  togglePassiveDiscovery?: Maybe<PassiveDiscoveryToggle>;
  unacknowledgeAlert?: Maybe<AlertResponse>;
  updateLocation?: Maybe<MonitoringLocation>;
  updateNode?: Maybe<Scalars['Long']>;
  upsertIcmpActiveDiscovery?: Maybe<IcmpActiveDiscovery>;
  upsertPassiveDiscovery?: Maybe<PassiveDiscovery>;
}

/** Mutation root */
export type MutationAcknowledgeAlertArgs = {
  ids?: InputMaybe<Array<InputMaybe<Scalars['Long']>>>;
}

/** Mutation root */
export type MutationAddNodeArgs = {
  node?: InputMaybe<NodeCreateInput>;
}

/** Mutation root */
export type MutationAddTagsToNodesArgs = {
  tags?: InputMaybe<TagListNodesAddInput>;
}

/** Mutation root */
export type MutationClearAlertArgs = {
  ids?: InputMaybe<Array<InputMaybe<Scalars['Long']>>>;
}

/** Mutation root */
export type MutationCreateAzureActiveDiscoveryArgs = {
  discovery?: InputMaybe<AzureActiveDiscoveryCreateInput>;
}

/** Mutation root */
export type MutationCreateIcmpActiveDiscoveryArgs = {
  request?: InputMaybe<IcmpActiveDiscoveryCreateInput>;
}

/** Mutation root */
export type MutationCreateLocationArgs = {
  location?: InputMaybe<MonitoringLocationCreateInput>;
}

/** Mutation root */
export type MutationCreateMonitorPolicyArgs = {
  policy?: InputMaybe<MonitorPolicyInput>;
}

/** Mutation root */
export type MutationDeleteAlertArgs = {
  ids?: InputMaybe<Array<InputMaybe<Scalars['Long']>>>;
}

/** Mutation root */
export type MutationDeleteIcmpActiveDiscoveryArgs = {
  id?: InputMaybe<Scalars['Long']>;
}

/** Mutation root */
export type MutationDeleteLocationArgs = {
  id: Scalars['Long'];
}

/** Mutation root */
export type MutationDeleteMinionArgs = {
  id: Scalars['Long'];
}

/** Mutation root */
export type MutationDeleteNodeArgs = {
  id?: InputMaybe<Scalars['Long']>;
}

/** Mutation root */
export type MutationDeletePassiveDiscoveryArgs = {
  id?: InputMaybe<Scalars['Long']>;
}

/** Mutation root */
export type MutationDeletePolicyByIdArgs = {
  id?: InputMaybe<Scalars['Long']>;
}

/** Mutation root */
export type MutationDeleteRuleByIdArgs = {
  id?: InputMaybe<Scalars['Long']>;
}

/** Mutation root */
export type MutationDiscoveryByNodeIdsArgs = {
  ids?: InputMaybe<Array<InputMaybe<Scalars['Long']>>>;
}

/** Mutation root */
export type MutationEscalateAlertArgs = {
  ids?: InputMaybe<Array<InputMaybe<Scalars['Long']>>>;
}

/** Mutation root */
export type MutationRemoveTagsFromNodesArgs = {
  tags?: InputMaybe<TagListNodesRemoveInput>;
}

/** Mutation root */
export type MutationRevokeMinionCertificateArgs = {
  locationId?: InputMaybe<Scalars['Long']>;
}

/** Mutation root */
export type MutationSavePagerDutyConfigArgs = {
  config?: InputMaybe<PagerDutyConfigInput>;
}

/** Mutation root */
export type MutationTogglePassiveDiscoveryArgs = {
  toggle?: InputMaybe<PassiveDiscoveryToggleInput>;
}

/** Mutation root */
export type MutationUnacknowledgeAlertArgs = {
  ids?: InputMaybe<Array<InputMaybe<Scalars['Long']>>>;
}

/** Mutation root */
export type MutationUpdateLocationArgs = {
  location?: InputMaybe<MonitoringLocationUpdateInput>;
}

/** Mutation root */
export type MutationUpdateNodeArgs = {
  node?: InputMaybe<NodeUpdateInput>;
}

/** Mutation root */
export type MutationUpsertIcmpActiveDiscoveryArgs = {
  request?: InputMaybe<IcmpActiveDiscoveryCreateInput>;
}

/** Mutation root */
export type MutationUpsertPassiveDiscoveryArgs = {
  discovery?: InputMaybe<PassiveDiscoveryUpsertInput>;
}

export type Node = {
  __typename?: 'Node';
  azureInterfaces?: Maybe<Array<Maybe<AzureInterface>>>;
  createTime: Scalars['Long'];
  id: Scalars['Long'];
  ipInterfaces?: Maybe<Array<Maybe<IpInterface>>>;
  location?: Maybe<MonitoringLocation>;
  monitoredState?: Maybe<Scalars['String']>;
  monitoringLocationId: Scalars['Long'];
  nodeAlias?: Maybe<Scalars['String']>;
  nodeLabel?: Maybe<Scalars['String']>;
  objectId?: Maybe<Scalars['String']>;
  scanType?: Maybe<Scalars['String']>;
  snmpInterfaces?: Maybe<Array<Maybe<SnmpInterface>>>;
  systemContact?: Maybe<Scalars['String']>;
  systemDescr?: Maybe<Scalars['String']>;
  systemLocation?: Maybe<Scalars['String']>;
  systemName?: Maybe<Scalars['String']>;
  tags?: Maybe<Array<Maybe<Tag>>>;
}

export type NodeCreateInput = {
  label?: InputMaybe<Scalars['String']>;
  locationId?: InputMaybe<Scalars['String']>;
  managementIp?: InputMaybe<Scalars['String']>;
  tags?: InputMaybe<Array<InputMaybe<TagCreateInput>>>;
}

export type NodeRef = {
  __typename?: 'NodeRef';
  nodeID: Scalars['Long'];
}

export type NodeStatus = {
  __typename?: 'NodeStatus';
  id: Scalars['Long'];
  status?: Maybe<Scalars['String']>;
}

export type NodeTags = {
  __typename?: 'NodeTags';
  nodeId: Scalars['Long'];
  tags?: Maybe<Array<Maybe<Tag>>>;
}

export type NodeUpdateInput = {
  id: Scalars['Long'];
  nodeAlias?: InputMaybe<Scalars['String']>;
}

export type PagerDutyConfigInput = {
  integrationkey?: InputMaybe<Scalars['String']>;
}

export type PassiveDiscovery = {
  __typename?: 'PassiveDiscovery';
  createTimeMsec?: Maybe<Scalars['Long']>;
  id?: Maybe<Scalars['Long']>;
  locationId?: Maybe<Scalars['String']>;
  name?: Maybe<Scalars['String']>;
  snmpCommunities?: Maybe<Array<Maybe<Scalars['String']>>>;
  snmpPorts?: Maybe<Array<Maybe<Scalars['Int']>>>;
  toggle: Scalars['Boolean'];
}

export type PassiveDiscoveryToggle = {
  __typename?: 'PassiveDiscoveryToggle';
  id: Scalars['Long'];
  toggle: Scalars['Boolean'];
}

export type PassiveDiscoveryToggleInput = {
  id: Scalars['Long'];
  toggle: Scalars['Boolean'];
}

export type PassiveDiscoveryUpsertInput = {
  id?: InputMaybe<Scalars['Long']>;
  locationId?: InputMaybe<Scalars['String']>;
  name?: InputMaybe<Scalars['String']>;
  snmpCommunities?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
  snmpPorts?: InputMaybe<Array<InputMaybe<Scalars['Int']>>>;
  tags?: InputMaybe<Array<InputMaybe<TagCreateInput>>>;
}

export type PolicyRule = {
  __typename?: 'PolicyRule';
  alertConditions?: Maybe<Array<Maybe<AlertCondition>>>;
  componentType?: Maybe<ManagedObjectType>;
  detectionMethod?: Maybe<DetectionMethod>;
  eventType?: Maybe<EventType>;
  id?: Maybe<Scalars['Long']>;
  name?: Maybe<Scalars['String']>;
  thresholdMetricName?: Maybe<Scalars['String']>;
}

export type PolicyRuleInput = {
  alertConditions?: InputMaybe<Array<InputMaybe<AlertConditionInput>>>;
  componentType?: InputMaybe<ManagedObjectType>;
  detectionMethod?: InputMaybe<DetectionMethod>;
  eventType?: InputMaybe<EventType>;
  id?: InputMaybe<Scalars['Long']>;
  name?: InputMaybe<Scalars['String']>;
  thresholdMetricName?: InputMaybe<Scalars['String']>;
}

/** Query root */
export type Query = {
  __typename?: 'Query';
  alertCounts?: Maybe<AlertCount>;
  allNodeStatus?: Maybe<Array<Maybe<NodeStatus>>>;
  countAlertByPolicyId?: Maybe<Scalars['Long']>;
  countAlertByRuleId?: Maybe<Scalars['Long']>;
  /** Returns the total count of alerts filtered by severity and time. */
  countAlerts?: Maybe<CountAlertResponse>;
  defaultPolicy?: Maybe<MonitorPolicy>;
  downloadTopN?: Maybe<TopNResponse>;
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
  listAlertEventDefinitions?: Maybe<Array<Maybe<AlertEventDefinition>>>;
  listIcmpActiveDiscovery?: Maybe<Array<Maybe<IcmpActiveDiscovery>>>;
  listMonitoryPolicies?: Maybe<Array<Maybe<MonitorPolicy>>>;
  locationByName?: Maybe<MonitoringLocation>;
  metric?: Maybe<TimeSeriesQueryResult>;
  nodeCount?: Maybe<Scalars['Int']>;
  nodeStatus?: Maybe<NodeStatus>;
  passiveDiscoveries?: Maybe<Array<Maybe<PassiveDiscovery>>>;
  searchLocation?: Maybe<Array<Maybe<MonitoringLocation>>>;
  tags?: Maybe<Array<Maybe<Tag>>>;
  tagsByActiveDiscoveryId?: Maybe<Array<Maybe<Tag>>>;
  tagsByNodeId?: Maybe<Array<Maybe<Tag>>>;
  tagsByNodeIds?: Maybe<Array<Maybe<NodeTags>>>;
  tagsByPassiveDiscoveryId?: Maybe<Array<Maybe<Tag>>>;
  topNNode?: Maybe<Array<Maybe<TopNNode>>>;
}

/** Query root */
export type QueryCountAlertByPolicyIdArgs = {
  id?: InputMaybe<Scalars['Long']>;
}

/** Query root */
export type QueryCountAlertByRuleIdArgs = {
  id?: InputMaybe<Scalars['Long']>;
}

/** Query root */
export type QueryCountAlertsArgs = {
  severityFilters?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
  timeRange?: InputMaybe<TimeRange>;
}

/** Query root */
export type QueryDownloadTopNArgs = {
  downloadFormat?: InputMaybe<DownloadFormat>;
  sortAscending: Scalars['Boolean'];
  sortBy?: InputMaybe<Scalars['String']>;
  timeRange?: InputMaybe<Scalars['Int']>;
  timeRangeUnit?: InputMaybe<TimeRangeUnit>;
}

/** Query root */
export type QueryFindAllAlertsArgs = {
  nodeLabel?: InputMaybe<Scalars['String']>;
  page: Scalars['Int'];
  pageSize?: InputMaybe<Scalars['Int']>;
  severities?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
  sortAscending: Scalars['Boolean'];
  sortBy?: InputMaybe<Scalars['String']>;
  timeRange?: InputMaybe<TimeRange>;
}

/** Query root */
export type QueryFindAllNodesByMonitoredStateArgs = {
  monitoredState?: InputMaybe<Scalars['String']>;
}

/** Query root */
export type QueryFindAllNodesByNodeLabelSearchArgs = {
  labelSearchTerm?: InputMaybe<Scalars['String']>;
}

/** Query root */
export type QueryFindAllNodesByTagsArgs = {
  tags?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
}

/** Query root */
export type QueryFindApplicationSeriesArgs = {
  requestCriteria?: InputMaybe<RequestCriteriaInput>;
}

/** Query root */
export type QueryFindApplicationSummariesArgs = {
  requestCriteria?: InputMaybe<RequestCriteriaInput>;
}

/** Query root */
export type QueryFindApplicationsArgs = {
  requestCriteria?: InputMaybe<RequestCriteriaInput>;
}

/** Query root */
export type QueryFindEventsByNodeIdArgs = {
  id?: InputMaybe<Scalars['Long']>;
}

/** Query root */
export type QueryFindExportersArgs = {
  requestCriteria?: InputMaybe<RequestCriteriaInput>;
}

/** Query root */
export type QueryFindLocationByIdArgs = {
  id: Scalars['Long'];
}

/** Query root */
export type QueryFindMinionByIdArgs = {
  id: Scalars['Long'];
}

/** Query root */
export type QueryFindMinionsByLocationIdArgs = {
  locationId: Scalars['Long'];
}

/** Query root */
export type QueryFindMonitorPolicyByIdArgs = {
  id?: InputMaybe<Scalars['Long']>;
}

/** Query root */
export type QueryFindNodeByIdArgs = {
  id?: InputMaybe<Scalars['Long']>;
}

/** Query root */
export type QueryGetMinionCertificateArgs = {
  locationId?: InputMaybe<Scalars['Long']>;
}

/** Query root */
export type QueryIcmpActiveDiscoveryByIdArgs = {
  id?: InputMaybe<Scalars['Long']>;
}

/** Query root */
export type QueryListAlertEventDefinitionsArgs = {
  eventType?: InputMaybe<EventType>;
}

/** Query root */
export type QueryLocationByNameArgs = {
  locationName?: InputMaybe<Scalars['String']>;
}

/** Query root */
export type QueryMetricArgs = {
  labels?: InputMaybe<Scalars['Map_String_StringScalar']>;
  name?: InputMaybe<Scalars['String']>;
  timeRange?: InputMaybe<Scalars['Int']>;
  timeRangeUnit?: InputMaybe<TimeRangeUnit>;
}

/** Query root */
export type QueryNodeStatusArgs = {
  id?: InputMaybe<Scalars['Long']>;
}

/** Query root */
export type QuerySearchLocationArgs = {
  searchTerm?: InputMaybe<Scalars['String']>;
}

/** Query root */
export type QueryTagsArgs = {
  searchTerm?: InputMaybe<Scalars['String']>;
}

/** Query root */
export type QueryTagsByActiveDiscoveryIdArgs = {
  activeDiscoveryId?: InputMaybe<Scalars['Long']>;
  searchTerm?: InputMaybe<Scalars['String']>;
}

/** Query root */
export type QueryTagsByNodeIdArgs = {
  nodeId?: InputMaybe<Scalars['Long']>;
  searchTerm?: InputMaybe<Scalars['String']>;
}

/** Query root */
export type QueryTagsByNodeIdsArgs = {
  nodeIds?: InputMaybe<Array<InputMaybe<Scalars['Long']>>>;
}

/** Query root */
export type QueryTagsByPassiveDiscoveryIdArgs = {
  passiveDiscoveryId?: InputMaybe<Scalars['Long']>;
  searchTerm?: InputMaybe<Scalars['String']>;
}

/** Query root */
export type QueryTopNNodeArgs = {
  page?: InputMaybe<Scalars['Int']>;
  pageSize?: InputMaybe<Scalars['Int']>;
  sortAscending: Scalars['Boolean'];
  sortBy?: InputMaybe<Scalars['String']>;
  timeRange?: InputMaybe<Scalars['Int']>;
  timeRangeUnit?: InputMaybe<TimeRangeUnit>;
}

export type RequestCriteriaInput = {
  applications?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
  count?: InputMaybe<Scalars['Int']>;
  exporter?: InputMaybe<Array<InputMaybe<ExporterFilterInput>>>;
  includeOther?: InputMaybe<Scalars['Boolean']>;
  step?: InputMaybe<Scalars['Int']>;
  timeRange?: InputMaybe<TimeRangeInput>;
}

export type SnmpConfig = {
  __typename?: 'SNMPConfig';
  ports?: Maybe<Array<Maybe<Scalars['Int']>>>;
  readCommunities?: Maybe<Array<Maybe<Scalars['String']>>>;
}

export type SnmpConfigInput = {
  ports?: InputMaybe<Array<InputMaybe<Scalars['Int']>>>;
  readCommunities?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
}

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
}

export type SnmpInterface = {
  __typename?: 'SnmpInterface';
  id: Scalars['Long'];
  ifAdminStatus?: Maybe<Scalars['String']>;
  ifAlias?: Maybe<Scalars['String']>;
  ifDescr?: Maybe<Scalars['String']>;
  ifIndex: Scalars['Int'];
  ifName?: Maybe<Scalars['String']>;
  ifOperatorStatus?: Maybe<Scalars['String']>;
  ifSpeed: Scalars['Long'];
  ifType: Scalars['Int'];
  ipAddress?: Maybe<Scalars['String']>;
  nodeId: Scalars['Long'];
  physicalAddr?: Maybe<Scalars['String']>;
}

export type SnmpInterfaceLinkRef = {
  __typename?: 'SnmpInterfaceLinkRef';
  ifA?: Maybe<SnmpInterfaceRef>;
  ifB?: Maybe<SnmpInterfaceRef>;
}

export type SnmpInterfaceRef = {
  __typename?: 'SnmpInterfaceRef';
  ifIndex: Scalars['Long'];
  node?: Maybe<NodeRef>;
}

export type TsData = {
  __typename?: 'TSData';
  result?: Maybe<Array<Maybe<TsResult>>>;
  resultType?: Maybe<Scalars['String']>;
}

export type TsResult = {
  __typename?: 'TSResult';
  metric?: Maybe<Scalars['Map_String_StringScalar']>;
  value?: Maybe<Array<Maybe<Scalars['Float']>>>;
  values?: Maybe<Array<Maybe<Array<Maybe<Scalars['Float']>>>>>;
}

export type Tag = {
  __typename?: 'Tag';
  id: Scalars['Long'];
  name?: Maybe<Scalars['String']>;
}

export type TagCreateInput = {
  name?: InputMaybe<Scalars['String']>;
}

export type TagListNodesAddInput = {
  nodeIds?: InputMaybe<Array<InputMaybe<Scalars['Long']>>>;
  tags?: InputMaybe<Array<InputMaybe<TagCreateInput>>>;
}

export type TagListNodesRemoveInput = {
  nodeIds?: InputMaybe<Array<InputMaybe<Scalars['Long']>>>;
  tagIds?: InputMaybe<Array<InputMaybe<Scalars['Long']>>>;
}

export enum TimeRange {
  All = 'ALL',
  Last_24Hours = 'LAST_24_HOURS',
  SevenDays = 'SEVEN_DAYS',
  Today = 'TODAY'
}

export type TimeRangeInput = {
  endTime?: InputMaybe<Scalars['Instant']>;
  startTime?: InputMaybe<Scalars['Instant']>;
}

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
}

export type TopNNode = {
  __typename?: 'TopNNode';
  avgResponseTime: Scalars['Float'];
  location?: Maybe<Scalars['String']>;
  nodeLabel?: Maybe<Scalars['String']>;
  reachability: Scalars['Float'];
}

export type TopNResponse = {
  __typename?: 'TopNResponse';
  downloadFormat?: Maybe<DownloadFormat>;
  topNBytes?: Maybe<Scalars['Base64String']>;
}

export type IpInterfaceResponse = {
  __typename?: 'IpInterfaceResponse';
  ipInterfaces?: Maybe<Scalars['Base64String']>;
}

export type SNMPInterfaceResponse = {
  __typename?: 'SNMPInterfaceResponse';
  downloadFormat?: Scalars['String'];
  snmpInterfaceBytes?: Maybe<Scalars['Base64String']>;
}

export type RecentAlerts = {
  __typename?: 'RecentAlerts';
  alertsBytes?: Maybe<Scalars['Base64String']>;
}

export type searchEventsBytes = {
  __typename?: 'searchEventsBytes'
  searchEventsBytes?: Maybe<Scalars['Base64String']>;
}

export type TrafficSummary = {
  __typename?: 'TrafficSummary';
  bytesIn: Scalars['Long'];
  bytesOut: Scalars['Long'];
  label?: Maybe<Scalars['String']>;
}

export type AcknowledgeAlertsMutationVariables = Exact<{
  ids?: InputMaybe<Array<InputMaybe<Scalars['Long']>> | InputMaybe<Scalars['Long']>>;
}>

export type AcknowledgeAlertsMutation = { __typename?: 'Mutation', acknowledgeAlert?: { __typename?: 'AlertResponse', alertList?: Array<{ __typename?: 'Alert', acknowledged: boolean, databaseId: any }>, alertErrorList?: Array<{ __typename?: 'AlertError', databaseId: any, error?: string }> } }

export type ClearAlertsMutationVariables = Exact<{
  ids?: InputMaybe<Array<InputMaybe<Scalars['Long']>> | InputMaybe<Scalars['Long']>>;
}>

export type ClearAlertsMutation = { __typename?: 'Mutation', clearAlert?: { __typename?: 'AlertResponse', alertList?: Array<{ __typename?: 'Alert', acknowledged: boolean, databaseId: any }>, alertErrorList?: Array<{ __typename?: 'AlertError', databaseId: any, error?: string }> } }

export type AlertsPartsFragment = { __typename?: 'Query', findAllAlerts?: { __typename?: 'ListAlertResponse', lastPage: number, nextPage: number, totalAlerts: any, alerts?: Array<{ __typename?: 'Alert', acknowledged: boolean, description?: string, firstEventTimeMs: any, lastUpdateTimeMs: any, severity?: Severity, label?: string, nodeName?: string, databaseId: any, location?: string, ruleNameList?: Array<string>, policyNameList?: Array<string> }> } }

export type AlertsListQueryVariables = Exact<{
  page: Scalars['Int'];
  pageSize?: InputMaybe<Scalars['Int']>;
  severities?: InputMaybe<Array<InputMaybe<Scalars['String']>> | InputMaybe<Scalars['String']>>;
  sortAscending: Scalars['Boolean'];
  sortBy?: InputMaybe<Scalars['String']>;
  timeRange: TimeRange;
  nodeLabel?: InputMaybe<Scalars['String']>;
}>
export type AlertsNodeByListVariables = Exact<{
  page: Scalars['Int'];
  pageSize?: InputMaybe<Scalars['Int']>;
  severity?: InputMaybe<Array<InputMaybe<Scalars['String']>> | InputMaybe<Scalars['String']>>;
  sortAscending: Scalars['Boolean'];
  sortBy?: InputMaybe<Scalars['String']>;
  timeRange?: TimeRange;
  nodeLabel?: InputMaybe<Scalars['String']>;
  nodeId?: Scalars['Long'];
}>

export type AlertsListQuery = { __typename?: 'Query', findAllAlerts?: { __typename?: 'ListAlertResponse', lastPage: number, nextPage: number, totalAlerts: any, alerts?: Array<{ __typename?: 'Alert', acknowledged: boolean, description?: string, firstEventTimeMs: any, lastUpdateTimeMs: any, severity?: Severity, label?: string, nodeName?: string, databaseId: any, location?: string, ruleNameList?: Array<string>, policyNameList?: Array<string> }> } }

export type CountAlertsQueryVariables = Exact<{
  severityFilters?: InputMaybe<Array<InputMaybe<Scalars['String']>> | InputMaybe<Scalars['String']>>;
  timeRange: TimeRange;
}>

export type DownloadEventsByNodeAndSearchTermQuery = { __typename?: 'Query', downloadEvents?: { __typename?: 'searchEventsBytes', searchEventsBytes?: any } }

export type AlertsByNodeListQuery = { __typename?: 'Query', getAlertsByNode?: { __typename?: 'ListAlertResponse', lastPage: number, nextPage: number, totalAlerts: any, alerts?: Array<{ __typename?: 'Alert', acknowledged: boolean, type: string, description?: string, firstEventTimeMs: any, lastUpdateTimeMs: any, severity?: Severity, label?: string, nodeName?: string, databaseId: any, location?: string, ruleNameList?: Array<string>, policyNameList?: Array<string> }> } }
export type CountAlertsQuery = { __typename?: 'Query', countAlerts?: { __typename?: 'CountAlertResponse', count: number, error?: string } }

export type AlertCountsQueryVariables = Exact<{ [key: string]: never; }>

export type AlertCountsQuery = { __typename?: 'Query', alertCounts?: { __typename?: 'AlertCount', acknowledgedCount?: any, totalAlertCount?: any, countBySeverity?: any } }

export type CreateAzureActiveDiscoveryMutationVariables = Exact<{
  discovery: AzureActiveDiscoveryCreateInput;
}>

export type CreateAzureActiveDiscoveryMutation = { __typename?: 'Mutation', createAzureActiveDiscovery?: { __typename?: 'AzureActiveDiscovery', createTimeMsec?: any, locationId?: string, subscriptionId?: string, clientId?: string } }

export type CreateIcmpActiveDiscoveryMutationVariables = Exact<{
  request: IcmpActiveDiscoveryCreateInput;
}>

export type CreateIcmpActiveDiscoveryMutation = { __typename?: 'Mutation', createIcmpActiveDiscovery?: { __typename?: 'IcmpActiveDiscovery', name?: string, ipAddresses?: Array<string>, locationId?: string, snmpConfig?: { __typename?: 'SNMPConfig', ports?: Array<number>, readCommunities?: Array<string> } } }

export type CreateOrUpdateActiveIcmpDiscoveryMutationVariables = Exact<{
  request: IcmpActiveDiscoveryCreateInput;
}>

export type CreateOrUpdateActiveIcmpDiscoveryMutation = { __typename?: 'Mutation', upsertIcmpActiveDiscovery?: { __typename?: 'IcmpActiveDiscovery', id: any, name?: string, ipAddresses?: Array<string>, locationId?: string, snmpConfig?: { __typename?: 'SNMPConfig', ports?: Array<number>, readCommunities?: Array<string> } } }

export type DeleteActiveIcmpDiscoveryMutationVariables = Exact<{
  id: Scalars['Long'];
}>

export type DeleteActiveIcmpDiscoveryMutation = { __typename?: 'Mutation', deleteIcmpActiveDiscovery?: boolean }

export type TogglePassiveDiscoveryMutationVariables = Exact<{
  toggle: PassiveDiscoveryToggleInput;
}>

export type TogglePassiveDiscoveryMutation = { __typename?: 'Mutation', togglePassiveDiscovery?: { __typename?: 'PassiveDiscoveryToggle', id: any, toggle: boolean } }

export type UpsertPassiveDiscoveryMutationVariables = Exact<{
  passiveDiscovery: PassiveDiscoveryUpsertInput;
}>

export type UpsertPassiveDiscoveryMutation = { __typename?: 'Mutation', upsertPassiveDiscovery?: { __typename?: 'PassiveDiscovery', id?: any, locationId?: string, name?: string, snmpCommunities?: Array<string>, snmpPorts?: Array<number>, toggle: boolean } }

export type DeletePassiveDiscoveryMutationVariables = Exact<{
  id: Scalars['Long'];
}>

export type DeletePassiveDiscoveryMutation = { __typename?: 'Mutation', deletePassiveDiscovery?: boolean }

export type FindApplicationSeriesQueryVariables = Exact<{
  requestCriteria: RequestCriteriaInput;
}>

export type FindApplicationSeriesQuery = { __typename?: 'Query', findApplicationSeries?: Array<{ __typename?: 'FlowingPoint', timestamp?: any, label?: string, value: number, direction?: string }> }

export type FindApplicationSummariesQueryVariables = Exact<{
  requestCriteria: RequestCriteriaInput;
}>

export type FindApplicationSummariesQuery = { __typename?: 'Query', findApplicationSummaries?: Array<{ __typename?: 'TrafficSummary', label?: string, bytesIn: any, bytesOut: any }> }

export type FindApplicationsQueryVariables = Exact<{
  requestCriteria: RequestCriteriaInput;
}>

export type FindApplicationsQuery = { __typename?: 'Query', findApplications?: Array<string> }

export type FindExportersQueryVariables = Exact<{
  requestCriteria: RequestCriteriaInput;
}>

export type FindExportersQuery = { __typename?: 'Query', findExporters?: Array<{ __typename?: 'Exporter', node?: { __typename?: 'Node', id: any, nodeLabel?: string }, ipInterface?: { __typename?: 'IpInterface', id: any, ipAddress?: string } }> }

export type CreateLocationMutationVariables = Exact<{
  location: MonitoringLocationCreateInput;
}>

export type CreateLocationMutation = { __typename?: 'Mutation', createLocation?: { __typename?: 'MonitoringLocation', id: any, location?: string, address?: string, longitude?: number, latitude?: number } }

export type UpdateLocationMutationVariables = Exact<{
  location?: InputMaybe<MonitoringLocationUpdateInput>;
}>

export type UpdateLocationMutation = { __typename?: 'Mutation', updateLocation?: { __typename?: 'MonitoringLocation', id: any, location?: string, address?: string, longitude?: number, latitude?: number } }

export type DeleteLocationMutationVariables = Exact<{
  id: Scalars['Long'];
}>

export type DeleteLocationMutation = { __typename?: 'Mutation', deleteLocation?: boolean }

export type RevokeMinionCertificateMutationVariables = Exact<{
  locationId: Scalars['Long'];
}>

export type RevokeMinionCertificateMutation = { __typename?: 'Mutation', revokeMinionCertificate?: boolean }

export type LocationsPartsFragment = { __typename?: 'Query', findAllLocations?: Array<{ __typename?: 'MonitoringLocation', id: any, location?: string, address?: string, longitude?: number, latitude?: number }> }

export type LocationsListQueryVariables = Exact<{ [key: string]: never; }>

export type LocationsListQuery = { __typename?: 'Query', findAllLocations?: Array<{ __typename?: 'MonitoringLocation', id: any, location?: string, address?: string, longitude?: number, latitude?: number }> }

export type SearchLocationQueryVariables = Exact<{
  searchTerm?: InputMaybe<Scalars['String']>;
}>

export type SearchLocationQuery = { __typename?: 'Query', searchLocation?: Array<{ __typename?: 'MonitoringLocation', id: any, location?: string, address?: string, longitude?: number, latitude?: number }> }

export type GetMinionCertificateQueryVariables = Exact<{
  location?: InputMaybe<Scalars['Long']>;
}>

export type GetMinionCertificateQuery = { __typename?: 'Query', getMinionCertificate?: { __typename?: 'CertificateResponse', password?: string, certificate?: any } }

export type DeviceUptimePartsFragment = { __typename?: 'Query', deviceUptime?: { __typename?: 'TimeSeriesQueryResult', data?: { __typename?: 'TSData', result?: Array<{ __typename?: 'TSResult', metric?: any, values?: Array<Array<number>> }> } } }

export type DeviceLatencyPartsFragment = { __typename?: 'Query', deviceLatency?: { __typename?: 'TimeSeriesQueryResult', data?: { __typename?: 'TSData', result?: Array<{ __typename?: 'TSResult', metric?: any, values?: Array<Array<number>> }> } } }

export type MetricPartsFragment = { __typename?: 'TimeSeriesQueryResult', data?: { __typename?: 'TSData', result?: Array<{ __typename?: 'TSResult', metric?: any, values?: Array<Array<number>> }> } }

export type TimeSeriesMetricFragment = { __typename?: 'Query', metric?: { __typename?: 'TimeSeriesQueryResult', data?: { __typename?: 'TSData', result?: Array<{ __typename?: 'TSResult', metric?: any, values?: Array<Array<number>> }> } } }

export type MinionUptimePartsFragment = { __typename?: 'Query', minionUptime?: { __typename?: 'TimeSeriesQueryResult', data?: { __typename?: 'TSData', result?: Array<{ __typename?: 'TSResult', metric?: any, values?: Array<Array<number>> }> } } }

export type MinionLatencyPartsFragment = { __typename?: 'Query', minionLatency?: { __typename?: 'TimeSeriesQueryResult', status?: string, data?: { __typename?: 'TSData', result?: Array<{ __typename?: 'TSResult', metric?: any, values?: Array<Array<number>> }> } } }

export type NodeLatencyPartsFragment = { __typename?: 'Query', nodeLatency?: { __typename?: 'TimeSeriesQueryResult', status?: string, data?: { __typename?: 'TSData', result?: Array<{ __typename?: 'TSResult', metric?: any, values?: Array<Array<number>> }> } } }

export type DeleteMinionMutationVariables = Exact<{
  id: Scalars['Long'];
}>

export type DeleteMinionMutation = { __typename?: 'Mutation', deleteMinion?: boolean }

export type FindMinionsByLocationIdQueryVariables = Exact<{
  locationId: Scalars['Long'];
}>

export type FindMinionsByLocationIdQuery = { __typename?: 'Query', findMinionsByLocationId?: Array<{ __typename?: 'Minion', id: any, label?: string, lastCheckedTime: any, status?: string, systemId?: string, location?: { __typename?: 'MonitoringLocation', id: any, location?: string } }> }

export type CreateMonitorPolicyMutationVariables = Exact<{
  policy: MonitorPolicyInput;
}>

export type CreateMonitorPolicyMutation = { __typename?: 'Mutation', createMonitorPolicy?: { __typename?: 'MonitorPolicy', id?: any } }

export type DeletePolicyByIdMutationVariables = Exact<{
  id: Scalars['Long'];
}>

export type DeletePolicyByIdMutation = { __typename?: 'Mutation', deletePolicyById?: boolean }

export type DeleteRuleByIdMutationVariables = Exact<{
  id: Scalars['Long'];
}>

export type DeleteRuleByIdMutation = { __typename?: 'Mutation', deleteRuleById?: boolean }

export type AddNodeMutationVariables = Exact<{
  node: NodeCreateInput;
}>

export type AddNodeMutation = { __typename?: 'Mutation', addNode?: { __typename?: 'Node', createTime: any, id: any, monitoringLocationId: any, nodeLabel?: string } }

export type DeleteNodeMutationVariables = Exact<{
  id: Scalars['Long'];
}>

export type DeleteNodeMutation = { __typename?: 'Mutation', deleteNode?: boolean }

export type AddTagsToNodesMutationVariables = Exact<{
  tags: TagListNodesAddInput;
}>

export type AddTagsToNodesMutation = { __typename?: 'Mutation', addTagsToNodes?: Array<{ __typename?: 'Tag', id: any, name?: string }> }

export type RemoveTagsFromNodesMutationVariables = Exact<{
  tags: TagListNodesRemoveInput;
}>

export type RemoveTagsFromNodesMutation = { __typename?: 'Mutation', removeTagsFromNodes?: boolean }

export type UpdateNodeMutationVariables = Exact<{
  node: NodeUpdateInput;
}>

export type UpdateNodeMutation = { __typename?: 'Mutation', updateNode?: any }

export type NodeStatusPartsFragment = { __typename?: 'Query', nodeStatus?: { __typename?: 'NodeStatus', id: any, status?: string } }

export type NodesPartsFragment = { __typename?: 'Query', findAllNodes?: Array<{ __typename?: 'Node', createTime: any, id: any, monitoringLocationId: any, nodeLabel?: string, scanType?: string, ipInterfaces?: Array<{ __typename?: 'IpInterface', id: any, ipAddress?: string, nodeId: any, snmpPrimary?: boolean }>, location?: { __typename?: 'MonitoringLocation', id: any, location?: string } }> }

export type SavePagerDutyConfigMutationVariables = Exact<{
  config: PagerDutyConfigInput;
}>

export type SavePagerDutyConfigMutation = { __typename?: 'Mutation', savePagerDutyConfig?: boolean }

export type TagsPartsFragment = { __typename?: 'Query', tags?: Array<{ __typename?: 'Tag', id: any, name?: string }> }

export type ListTagsQueryVariables = Exact<{ [key: string]: never; }>

export type ListTagsQuery = { __typename?: 'Query', tags?: Array<{ __typename?: 'Tag', id: any, name?: string }> }

export type TagsSearchPartsFragment = { __typename?: 'Query', tags?: Array<{ __typename?: 'Tag', id: any, name?: string }> }

export type ListTagsSearchQueryVariables = Exact<{
  searchTerm?: InputMaybe<Scalars['String']>;
}>

export type ListTagsSearchQuery = { __typename?: 'Query', tags?: Array<{ __typename?: 'Tag', id: any, name?: string }> }

export type ListTagsByNodeIdsQueryVariables = Exact<{
  nodeIds?: InputMaybe<Array<InputMaybe<Scalars['Long']>> | InputMaybe<Scalars['Long']>>;
}>

export type ListTagsByNodeIdsQuery = { __typename?: 'Query', tagsByNodeIds?: Array<{ __typename?: 'NodeTags', nodeId: any, tags?: Array<{ __typename?: 'Tag', id: any, name?: string }> }> }

export type ListAlertEventDefinitionsQueryVariables = Exact<{
  eventType?: InputMaybe<EventType>;
}>

export type ListAlertEventDefinitionsQuery = { __typename?: 'Query', listAlertEventDefinitions?: Array<{ __typename?: 'AlertEventDefinition', id?: any, name?: string, eventType?: EventType }> }

export type NodesTablePartsFragment = { __typename?: 'Query', findAllNodes?: Array<{ __typename?: 'Node', id: any, nodeLabel?: string, createTime: any, monitoringLocationId: any, scanType?: string, ipInterfaces?: Array<{ __typename?: 'IpInterface', ipAddress?: string, snmpPrimary?: boolean }> }> }

export type MinionsTablePartsFragment = { __typename?: 'Query', findAllMinions?: Array<{ __typename?: 'Minion', id: any, label?: string, lastCheckedTime: any, status?: string, systemId?: string, location?: { __typename?: 'MonitoringLocation', id: any, location?: string } }> }

export type ListNodesForTableQueryVariables = Exact<{ [key: string]: never; }>

export type ListNodesForTableQuery = { __typename?: 'Query', findAllNodes?: Array<{ __typename?: 'Node', id: any, nodeLabel?: string, createTime: any, monitoringLocationId: any, scanType?: string, ipInterfaces?: Array<{ __typename?: 'IpInterface', ipAddress?: string, snmpPrimary?: boolean }> }> }

export type ListMinionsForTableQueryVariables = Exact<{ [key: string]: never; }>

export type ListMinionsForTableQuery = { __typename?: 'Query', findAllMinions?: Array<{ __typename?: 'Minion', id: any, label?: string, lastCheckedTime: any, status?: string, systemId?: string, location?: { __typename?: 'MonitoringLocation', id: any, location?: string } }> }

export type ListMinionMetricsQueryVariables = Exact<{
  instance: Scalars['String'];
  monitor: Scalars['String'];
  timeRange: Scalars['Int'];
  timeRangeUnit: TimeRangeUnit;
}>

export type ListMinionMetricsQuery = { __typename?: 'Query', minionLatency?: { __typename?: 'TimeSeriesQueryResult', status?: string, data?: { __typename?: 'TSData', result?: Array<{ __typename?: 'TSResult', metric?: any, values?: Array<Array<number>> }> } } }

export type ListNodeMetricsQueryVariables = Exact<{
  id: Scalars['Long'];
  monitor: Scalars['String'];
  instance: Scalars['String'];
  timeRange: Scalars['Int'];
  timeRangeUnit: TimeRangeUnit;
}>

export type ListNodeMetricsQuery = { __typename?: 'Query', nodeLatency?: { __typename?: 'TimeSeriesQueryResult', status?: string, data?: { __typename?: 'TSData', result?: Array<{ __typename?: 'TSResult', metric?: any, values?: Array<Array<number>> }> } }, nodeStatus?: { __typename?: 'NodeStatus', id: any, status?: string } }

export type ListMinionsAndDevicesForTablesQueryVariables = Exact<{ [key: string]: never; }>

export type ListMinionsAndDevicesForTablesQuery = { __typename?: 'Query', findAllNodes?: Array<{ __typename?: 'Node', id: any, nodeLabel?: string, createTime: any, monitoringLocationId: any, scanType?: string, ipInterfaces?: Array<{ __typename?: 'IpInterface', ipAddress?: string, snmpPrimary?: boolean }> }>, findAllMinions?: Array<{ __typename?: 'Minion', id: any, label?: string, lastCheckedTime: any, status?: string, systemId?: string, location?: { __typename?: 'MonitoringLocation', id: any, location?: string } }>, findAllLocations?: Array<{ __typename?: 'MonitoringLocation', id: any, location?: string, address?: string, longitude?: number, latitude?: number }> }

export type NetworkTrafficQueryVariables = Exact<{
  name: Scalars['String'];
  timeRange: Scalars['Int'];
  timeRangeUnit: TimeRangeUnit;
}>

export type NetworkTrafficQuery = { __typename?: 'Query', metric?: { __typename?: 'TimeSeriesQueryResult', status?: string, data?: { __typename?: 'TSData', result?: Array<{ __typename?: 'TSResult', metric?: any, values?: Array<Array<number>> }> } } }

export type TopNNodesQueryVariables = Exact<{
  timeRange: Scalars['Int'];
  timeRangeUnit: TimeRangeUnit;
  sortAscending: Scalars['Boolean'];
  pageSize?: InputMaybe<Scalars['Int']>;
  sortBy?: InputMaybe<Scalars['String']>;
  page?: InputMaybe<Scalars['Int']>;
}>

export type TopNNodesQuery = { __typename?: 'Query', nodeCount?: number, topNNode?: Array<{ __typename?: 'TopNNode', nodeLabel?: string, location?: string, avgResponseTime: number, reachability: number }>, allNodeStatus?: Array<{ __typename?: 'NodeStatus', id: any, status?: string }> }

export type DownloadTopNQueryVariables = Exact<{
  downloadFormat: DownloadFormat;
  timeRange: Scalars['Int'];
  timeRangeUnit: TimeRangeUnit;
  sortAscending: Scalars['Boolean'];
  sortBy?: InputMaybe<Scalars['String']>;
}>

export type DownloadCsvVariables = Exact<{
  nodeId?: Scalars['Long'];
  searchTerm?: InputMaybe<Scalars['String']>;
  downloadFormat?: DownloadFormat;
}>

export type AlertsNodeByListVariablesExtended = AlertsNodeByListVariables & {
  downloadFormat?: DownloadFormat;
}

export type DownloadTopNQuery = { __typename?: 'Query', downloadTopN?: { __typename?: 'TopNResponse', topNBytes?: any } }

export type DownloadIpInterfacesByNodeAndSearchTermQuery = { __typename?: 'Query', downloadIpInterfacesByNodeAndSearchTerm?: { __typename?: 'IpInterfaceResponse', ipInterfaces?: any } }

export type DownloadSNMPInterfacesByNodeAndSearchTermQuery = { __typename?: 'Query', downloadSnmpInterfaces?: { __typename?: 'SNMPInterfaceResponse', downloadFormat?: string, snmpInterfaceBytes?: any } }

export type ListLocationsForDiscoveryQueryVariables = Exact<{ [key: string]: never; }>

export type ListLocationsForDiscoveryQuery = { __typename?: 'Query', findAllLocations?: Array<{ __typename?: 'MonitoringLocation', id: any, location?: string, address?: string, longitude?: number, latitude?: number }> }

export type DownloadAlertsByNodeAndSearchTermQuery = { __typename?: 'Query', downloadRecentAlertsByNode?: { __typename?: 'RecentAlerts', alertsBytes?: any } }

export type ListDiscoveriesQueryVariables = Exact<{ [key: string]: never; }>

export type ListDiscoveriesQuery = { __typename?: 'Query', passiveDiscoveries?: Array<{ __typename?: 'PassiveDiscovery', id?: any, locationId?: string, name?: string, snmpCommunities?: Array<string>, snmpPorts?: Array<number>, toggle: boolean }>, listActiveDiscovery?: Array<{ __typename?: 'ActiveDiscovery', details?: any, discoveryType?: string }> }

export type TagsByActiveDiscoveryIdQueryVariables = Exact<{
  discoveryId: Scalars['Long'];
}>

export type TagsByActiveDiscoveryIdQuery = { __typename?: 'Query', tagsByActiveDiscoveryId?: Array<{ __typename?: 'Tag', id: any, name?: string }> }

export type TagsByPassiveDiscoveryIdQueryVariables = Exact<{
  discoveryId: Scalars['Long'];
}>

export type TagsByPassiveDiscoveryIdQuery = { __typename?: 'Query', tagsByPassiveDiscoveryId?: Array<{ __typename?: 'Tag', id: any, name?: string }> }

export type GetMetricQueryVariables = Exact<{
  metric: Scalars['String'];
}>

export type GetMetricQuery = { __typename?: 'Query', metric?: { __typename?: 'TimeSeriesQueryResult', data?: { __typename?: 'TSData', result?: Array<{ __typename?: 'TSResult', metric?: any, values?: Array<Array<number>> }> } } }

export type GetTimeSeriesMetricQueryVariables = Exact<{
  name: Scalars['String'];
  monitor: Scalars['String'];
  nodeId?: InputMaybe<Scalars['String']>;
  timeRange: Scalars['Int'];
  timeRangeUnit: TimeRangeUnit;
  instance?: InputMaybe<Scalars['String']>;
}>

export type GetTimeSeriesMetricQuery = { __typename?: 'Query', metric?: { __typename?: 'TimeSeriesQueryResult', data?: { __typename?: 'TSData', result?: Array<{ __typename?: 'TSResult', metric?: any, values?: Array<Array<number>> }> } } }

export type GetTimeSeriesMetricsWithIfNameQueryVariables = Exact<{
  name: Scalars['String'];
  monitor: Scalars['String'];
  nodeId?: InputMaybe<Scalars['String']>;
  timeRange: Scalars['Int'];
  timeRangeUnit: TimeRangeUnit;
  ifName?: InputMaybe<Scalars['String']>;
}>

export type GetTimeSeriesMetricsWithIfNameQuery = { __typename?: 'Query', metric?: { __typename?: 'TimeSeriesQueryResult', data?: { __typename?: 'TSData', result?: Array<{ __typename?: 'TSResult', metric?: any, values?: Array<Array<number>> }> } } }

export type GetNodeForGraphsQueryVariables = Exact<{
  id?: InputMaybe<Scalars['Long']>;
}>

export type GetNodeForGraphsQuery = { __typename?: 'Query', findNodeById?: { __typename?: 'Node', id: any, scanType?: string, ipInterfaces?: Array<{ __typename?: 'IpInterface', ipAddress?: string, snmpPrimary?: boolean }> } }

export type NodesListQueryVariables = Exact<{ [key: string]: never; }>

export type NodesListQuery = { __typename?: 'Query', findAllNodes?: Array<{ __typename?: 'Node', createTime: any, id: any, monitoringLocationId: any, nodeLabel?: string, scanType?: string, ipInterfaces?: Array<{ __typename?: 'IpInterface', id: any, ipAddress?: string, nodeId: any, snmpPrimary?: boolean }>, location?: { __typename?: 'MonitoringLocation', id: any, location?: string } }> }

export type NodeLatencyMetricQueryVariables = Exact<{
  id: Scalars['Long'];
  monitor: Scalars['String'];
  instance: Scalars['String'];
  timeRange: Scalars['Int'];
  timeRangeUnit: TimeRangeUnit;
}>

export type NodeLatencyMetricQuery = { __typename?: 'Query', nodeLatency?: { __typename?: 'TimeSeriesQueryResult', status?: string, data?: { __typename?: 'TSData', result?: Array<{ __typename?: 'TSResult', metric?: any, values?: Array<Array<number>> }> } }, nodeStatus?: { __typename?: 'NodeStatus', id: any, status?: string } }

export type FindAllNodesByNodeLabelSearchQueryVariables = Exact<{
  labelSearchTerm: Scalars['String'];
}>

export type FindAllNodesByNodeLabelSearchQuery = { __typename?: 'Query', findAllNodesByNodeLabelSearch?: Array<{ __typename?: 'Node', id: any, monitoredState?: string, monitoringLocationId: any, nodeLabel?: string, scanType?: string, ipInterfaces?: Array<{ __typename?: 'IpInterface', id: any, ipAddress?: string, nodeId: any, snmpPrimary?: boolean }>, location?: { __typename?: 'MonitoringLocation', id: any, location?: string }, tags?: Array<{ __typename?: 'Tag', id: any, name?: string }> }>, allMetrics?: { __typename?: 'TimeSeriesQueryResult', status?: string, data?: { __typename?: 'TSData', resultType?: string, result?: Array<{ __typename?: 'TSResult', metric?: any, value?: Array<number>, values?: Array<Array<number>> }> } } }

export type FindAllNodesByTagsQueryVariables = Exact<{
  tags?: InputMaybe<Array<InputMaybe<Scalars['String']>> | InputMaybe<Scalars['String']>>;
}>

export type FindAllNodesByTagsQuery = { __typename?: 'Query', findAllNodesByTags?: Array<{ __typename?: 'Node', id: any, monitoredState?: string, monitoringLocationId: any, nodeLabel?: string, scanType?: string, ipInterfaces?: Array<{ __typename?: 'IpInterface', id: any, ipAddress?: string, nodeId: any, snmpPrimary?: boolean }>, location?: { __typename?: 'MonitoringLocation', id: any, location?: string }, tags?: Array<{ __typename?: 'Tag', id: any, name?: string }> }>, allMetrics?: { __typename?: 'TimeSeriesQueryResult', status?: string, data?: { __typename?: 'TSData', resultType?: string, result?: Array<{ __typename?: 'TSResult', metric?: any, value?: Array<number>, values?: Array<Array<number>> }> } } }

export type FindAllNodesByMonitoredStateQueryVariables = Exact<{
  monitoredState: Scalars['String'];
}>

export type FindAllNodesByMonitoredStateQuery = { __typename?: 'Query', findAllNodesByMonitoredState?: Array<{ __typename?: 'Node', id: any, monitoringLocationId: any, nodeLabel?: string, scanType?: string, ipInterfaces?: Array<{ __typename?: 'IpInterface', id: any, ipAddress?: string, nodeId: any, snmpPrimary?: boolean }>, location?: { __typename?: 'MonitoringLocation', id: any, location?: string } }> }

export type BuildNetworkInventoryPageQueryVariables = Exact<{ [key: string]: never; }>

export type BuildNetworkInventoryPageQuery = { __typename?: 'Query', findAllNodes?: Array<{ __typename?: 'Node', id: any, monitoredState?: string, monitoringLocationId: any, nodeLabel?: string, scanType?: string, nodeAlias?: string, ipInterfaces?: Array<{ __typename?: 'IpInterface', id: any, ipAddress?: string, nodeId: any, snmpPrimary?: boolean }>, location?: { __typename?: 'MonitoringLocation', id: any, location?: string }, tags?: Array<{ __typename?: 'Tag', id: any, name?: string }> }>, allMetrics?: { __typename?: 'TimeSeriesQueryResult', status?: string, data?: { __typename?: 'TSData', resultType?: string, result?: Array<{ __typename?: 'TSResult', metric?: any, value?: Array<number>, values?: Array<Array<number>> }> } } }

export type NodesForMapQueryVariables = Exact<{ [key: string]: never; }>

export type NodesForMapQuery = { __typename?: 'Query', findAllNodes?: Array<{ __typename?: 'Node', id: any, nodeLabel?: string, location?: { __typename?: 'MonitoringLocation', latitude?: number, longitude?: number, location?: string } }> }

export type MonitoringPolicyPartsFragment = { __typename?: 'MonitorPolicy', id?: any, memo?: string, name?: string, notifyByEmail?: boolean, notifyByPagerDuty?: boolean, notifyByWebhooks?: boolean, tags?: Array<string>, rules?: Array<{ __typename?: 'PolicyRule', id?: any, name?: string, componentType?: ManagedObjectType, detectionMethod?: DetectionMethod, eventType?: EventType, thresholdMetricName?: string, alertConditions?: Array<{ __typename?: 'AlertCondition', id?: any, count?: number, overtime?: number, overtimeUnit?: string, severity?: string, clearEvent?: { __typename?: 'AlertEventDefinition', id?: any, name?: string, eventType?: EventType }, triggerEvent?: { __typename?: 'AlertEventDefinition', id?: any, name?: string, eventType?: EventType } }> }> }

export type ListMonitoryPoliciesQueryVariables = Exact<{ [key: string]: never; }>

export type ListMonitoryPoliciesQuery = { __typename?: 'Query', listMonitoryPolicies?: Array<{ __typename?: 'MonitorPolicy', id?: any, memo?: string, name?: string, notifyByEmail?: boolean, notifyByPagerDuty?: boolean, notifyByWebhooks?: boolean, tags?: Array<string>, rules?: Array<{ __typename?: 'PolicyRule', id?: any, name?: string, componentType?: ManagedObjectType, detectionMethod?: DetectionMethod, eventType?: EventType, thresholdMetricName?: string, alertConditions?: Array<{ __typename?: 'AlertCondition', id?: any, count?: number, overtime?: number, overtimeUnit?: string, severity?: string, clearEvent?: { __typename?: 'AlertEventDefinition', id?: any, name?: string, eventType?: EventType }, triggerEvent?: { __typename?: 'AlertEventDefinition', id?: any, name?: string, eventType?: EventType } }> }> }>, defaultPolicy?: { __typename?: 'MonitorPolicy', id?: any, memo?: string, name?: string, notifyByEmail?: boolean, notifyByPagerDuty?: boolean, notifyByWebhooks?: boolean, tags?: Array<string>, rules?: Array<{ __typename?: 'PolicyRule', id?: any, name?: string, componentType?: ManagedObjectType, detectionMethod?: DetectionMethod, eventType?: EventType, thresholdMetricName?: string, alertConditions?: Array<{ __typename?: 'AlertCondition', id?: any, count?: number, overtime?: number, overtimeUnit?: string, severity?: string, clearEvent?: { __typename?: 'AlertEventDefinition', id?: any, name?: string, eventType?: EventType }, triggerEvent?: { __typename?: 'AlertEventDefinition', id?: any, name?: string, eventType?: EventType } }> }> } }

export type CountAlertByPolicyIdQueryVariables = Exact<{
  id: Scalars['Long'];
}>

export type CountAlertByPolicyIdQuery = { __typename?: 'Query', countAlertByPolicyId?: any }

export type CountAlertByRuleIdQueryVariables = Exact<{
  id: Scalars['Long'];
}>

export type CountAlertByRuleIdQuery = { __typename?: 'Query', countAlertByRuleId?: any }

export type EventsByNodeIdPartsFragment = { __typename?: 'Query', events?: Array<{ __typename?: 'Event', id: number, uei?: string, nodeId: number, ipAddress?: string, producedTime: any }> }

export type NodeByIdPartsFragment = { __typename?: 'Query', node?: { __typename?: 'Node', id: any, nodeLabel?: string, nodeAlias?: string, objectId?: string, systemContact?: string, systemDescr?: string, systemLocation?: string, systemName?: string, scanType?: string, location?: { __typename?: 'MonitoringLocation', location?: string }, ipInterfaces?: Array<{ __typename?: 'IpInterface', id: any, hostname?: string, ipAddress?: string, netmask?: string, nodeId: any, snmpPrimary?: boolean, azureInterfaceId?: any }>, snmpInterfaces?: Array<{ __typename?: 'SnmpInterface', id: any, ifAdminStatus?: string, ifAlias?: string, ifDescr?: string, ifIndex: number, ifName?: string, ifOperatorStatus?: string, ifSpeed: any, ifType: number, ipAddress?: string, nodeId: any, physicalAddr?: string }>, azureInterfaces?: Array<{ __typename?: 'AzureInterface', id: any, nodeId: any, interfaceName?: string, privateIpId?: string, publicIpAddress?: string, publicIpId?: string, location?: string }> } }

export type ListNodeStatusQueryVariables = Exact<{
  id?: InputMaybe<Scalars['Long']>;
}>
export type ListEventsQueryVariables = Exact<{
  id?: InputMaybe<Scalars['Long']>;
}>

export type ListNodeStatusQuery = { __typename?: 'Query', node?: { __typename?: 'Node', id: any, nodeLabel?: string, nodeAlias?: string, objectId?: string, systemContact?: string, systemDescr?: string, systemLocation?: string, systemName?: string, scanType?: string, location?: { __typename?: 'MonitoringLocation', location?: string }, ipInterfaces?: Array<{ __typename?: 'IpInterface', id: any, hostname?: string, ipAddress?: string, netmask?: string, nodeId: any, snmpPrimary?: boolean, azureInterfaceId?: any }>, snmpInterfaces?: Array<{ __typename?: 'SnmpInterface', id: any, ifAdminStatus?: string, ifAlias?: string, ifDescr?: string, ifIndex: number, ifName?: string, ifOperatorStatus?: string, ifSpeed: any, ifType: number, ipAddress?: string, nodeId: any, physicalAddr?: string }>, azureInterfaces?: Array<{ __typename?: 'AzureInterface', id: any, nodeId: any, interfaceName?: string, privateIpId?: string, publicIpAddress?: string, publicIpId?: string, location?: string }> } }

export type FindExportersForNodeStatusQueryVariables = Exact<{
  requestCriteria: RequestCriteriaInput;
}>

export type FindExportersForNodeStatusQuery = { __typename?: 'Query', findExporters?: Array<{ __typename?: 'Exporter', snmpInterface?: { __typename?: 'SnmpInterface', ifIndex: number, ifName?: string }, ipInterface?: { __typename?: 'IpInterface', id: any, ipAddress?: string } }> }

export type FindLocationsForWelcomeQueryVariables = Exact<{ [key: string]: never; }>

export type FindLocationsForWelcomeQuery = { __typename?: 'Query', findAllLocations?: Array<{ __typename?: 'MonitoringLocation', id: any, location?: string }> }

export type FindDevicesForWelcomeQueryVariables = Exact<{ [key: string]: never; }>

export type FindDevicesForWelcomeQuery = { __typename?: 'Query', findAllNodes?: Array<{ __typename?: 'Node', id: any, nodeLabel?: string, createTime: any, ipInterfaces?: Array<{ __typename?: 'IpInterface', id: any, nodeId: any, ipAddress?: string, snmpPrimary?: boolean }> }> }

export type FindMinionsForWelcomeQueryVariables = Exact<{
  locationId: Scalars['Long'];
}>

export type FindMinionsForWelcomeQuery = { __typename?: 'Query', findMinionsByLocationId?: Array<{ __typename?: 'Minion', id: any }> }

export type DownloadMinionCertificateForWelcomeQueryVariables = Exact<{
  location?: InputMaybe<Scalars['Long']>;
}>

export type DownloadMinionCertificateForWelcomeQuery = { __typename?: 'Query', getMinionCertificate?: { __typename?: 'CertificateResponse', password?: string, certificate?: any } }

/* eslint-disable @stylistic/comma-spacing, @stylistic/key-spacing */
export const AlertsPartsList = { kind: 'Document', definitions: [{ kind: 'FragmentDefinition', name: { kind: 'Name', value: 'AlertsByNode' }, typeCondition: { kind: 'NamedType', name: { kind: 'Name', value: 'Query' } }, selectionSet: { kind: 'SelectionSet', selections: [{ kind: 'Field', name: { kind: 'Name', value: 'getAlertsByNode' }, arguments: [{ kind: 'Argument', name: { kind: 'Name', value: 'page' }, value: { kind: 'Variable', name: { kind: 'Name', value: 'page' } } }, { kind: 'Argument', name: { kind: 'Name', value: 'pageSize' }, value: { kind: 'Variable', name: { kind: 'Name', value: 'pageSize' } } }, { kind: 'Argument', name: { kind: 'Name', value: 'sortAscending' }, value: { kind: 'Variable', name: { kind: 'Name', value: 'sortAscending' } } }, { kind: 'Argument', name: { kind: 'Name', value: 'sortBy' }, value: { kind: 'Variable', name: { kind: 'Name', value: 'sortBy' } } }, { kind: 'Argument', name: { kind: 'Name', value: 'nodeId' }, value: { kind: 'Variable', name: { kind: 'Name', value: 'nodeId' } } }], selectionSet: { kind: 'SelectionSet', selections: [{ kind: 'Field', name: { kind: 'Name', value: 'lastPage' } }, { kind: 'Field', name: { kind: 'Name', value: 'nextPage' } }, { kind: 'Field', name: { kind: 'Name', value: 'totalAlerts' } }, { kind: 'Field', name: { kind: 'Name', value: 'alerts' }, selectionSet: { kind: 'SelectionSet', selections: [{ kind: 'Field', name: { kind: 'Name', value: 'acknowledged' } },{ kind: 'Field', name: { kind: 'Name', value: 'type' } }, { kind: 'Field', name: { kind: 'Name', value: 'description' } }, { kind: 'Field', name: { kind: 'Name', value: 'firstEventTimeMs' } }, { kind: 'Field', name: { kind: 'Name', value: 'lastUpdateTimeMs' } }, { kind: 'Field', name: { kind: 'Name', value: 'severity' } }, { kind: 'Field', name: { kind: 'Name', value: 'label' } }, { kind: 'Field', name: { kind: 'Name', value: 'nodeName' } }, { kind: 'Field', name: { kind: 'Name', value: 'databaseId' } }, { kind: 'Field', name: { kind: 'Name', value: 'location' } }, { kind: 'Field', name: { kind: 'Name', value: 'ruleNameList' } }, { kind: 'Field', name: { kind: 'Name', value: 'policyNameList' } }] } }] } }] } }] } as unknown as DocumentNode<AlertsByNodeListQuery, unknown>
export const AlertsPartsFragmentDoc = {'kind':'Document','definitions':[{'kind':'FragmentDefinition','name':{'kind':'Name','value':'AlertsParts'},'typeCondition':{'kind':'NamedType','name':{'kind':'Name','value':'Query'}},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'findAllAlerts'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'page'},'value':{'kind':'Variable','name':{'kind':'Name','value':'page'}}},{'kind':'Argument','name':{'kind':'Name','value':'pageSize'},'value':{'kind':'Variable','name':{'kind':'Name','value':'pageSize'}}},{'kind':'Argument','name':{'kind':'Name','value':'severities'},'value':{'kind':'Variable','name':{'kind':'Name','value':'severities'}}},{'kind':'Argument','name':{'kind':'Name','value':'sortAscending'},'value':{'kind':'Variable','name':{'kind':'Name','value':'sortAscending'}}},{'kind':'Argument','name':{'kind':'Name','value':'sortBy'},'value':{'kind':'Variable','name':{'kind':'Name','value':'sortBy'}}},{'kind':'Argument','name':{'kind':'Name','value':'timeRange'},'value':{'kind':'Variable','name':{'kind':'Name','value':'timeRange'}}},{'kind':'Argument','name':{'kind':'Name','value':'nodeLabel'},'value':{'kind':'Variable','name':{'kind':'Name','value':'nodeLabel'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'lastPage'}},{'kind':'Field','name':{'kind':'Name','value':'nextPage'}},{'kind':'Field','name':{'kind':'Name','value':'totalAlerts'}},{'kind':'Field','name':{'kind':'Name','value':'alerts'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'acknowledged'}},{'kind':'Field','name':{'kind':'Name','value':'description'}},{'kind':'Field','name':{'kind':'Name','value':'firstEventTimeMs'}},{'kind':'Field','name':{'kind':'Name','value':'lastUpdateTimeMs'}},{'kind':'Field','name':{'kind':'Name','value':'severity'}},{'kind':'Field','name':{'kind':'Name','value':'label'}},{'kind':'Field','name':{'kind':'Name','value':'nodeName'}},{'kind':'Field','name':{'kind':'Name','value':'databaseId'}},{'kind':'Field','name':{'kind':'Name','value':'location'}},{'kind':'Field','name':{'kind':'Name','value':'ruleNameList'}},{'kind':'Field','name':{'kind':'Name','value':'policyNameList'}}]}}]}}]}}]} as unknown as DocumentNode<AlertsPartsFragment, unknown>
export const LocationsPartsFragmentDoc = {'kind':'Document','definitions':[{'kind':'FragmentDefinition','name':{'kind':'Name','value':'LocationsParts'},'typeCondition':{'kind':'NamedType','name':{'kind':'Name','value':'Query'}},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'findAllLocations'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'location'}},{'kind':'Field','name':{'kind':'Name','value':'address'}},{'kind':'Field','name':{'kind':'Name','value':'longitude'}},{'kind':'Field','name':{'kind':'Name','value':'latitude'}}]}}]}}]} as unknown as DocumentNode<LocationsPartsFragment, unknown>
export const MetricPartsFragmentDoc = {'kind':'Document','definitions':[{'kind':'FragmentDefinition','name':{'kind':'Name','value':'MetricParts'},'typeCondition':{'kind':'NamedType','name':{'kind':'Name','value':'TimeSeriesQueryResult'}},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'data'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'result'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'metric'}},{'kind':'Field','name':{'kind':'Name','value':'values'}}]}}]}}]}}]} as unknown as DocumentNode<MetricPartsFragment, unknown>
export const DeviceUptimePartsFragmentDoc = {'kind':'Document','definitions':[{'kind':'FragmentDefinition','name':{'kind':'Name','value':'DeviceUptimeParts'},'typeCondition':{'kind':'NamedType','name':{'kind':'Name','value':'Query'}},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','alias':{'kind':'Name','value':'deviceUptime'},'name':{'kind':'Name','value':'metric'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'name'},'value':{'kind':'StringValue','value':'snmp_uptime_sec','block':false}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'FragmentSpread','name':{'kind':'Name','value':'MetricParts'}}]}}]}}]} as unknown as DocumentNode<DeviceUptimePartsFragment, unknown>
export const DeviceLatencyPartsFragmentDoc = {'kind':'Document','definitions':[{'kind':'FragmentDefinition','name':{'kind':'Name','value':'DeviceLatencyParts'},'typeCondition':{'kind':'NamedType','name':{'kind':'Name','value':'Query'}},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','alias':{'kind':'Name','value':'deviceLatency'},'name':{'kind':'Name','value':'metric'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'name'},'value':{'kind':'StringValue','value':'response_time_msec','block':false}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'FragmentSpread','name':{'kind':'Name','value':'MetricParts'}}]}}]}}]} as unknown as DocumentNode<DeviceLatencyPartsFragment, unknown>
export const TimeSeriesMetricFragmentDoc = {'kind':'Document','definitions':[{'kind':'FragmentDefinition','name':{'kind':'Name','value':'TimeSeriesMetric'},'typeCondition':{'kind':'NamedType','name':{'kind':'Name','value':'Query'}},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','alias':{'kind':'Name','value':'metric'},'name':{'kind':'Name','value':'metric'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'name'},'value':{'kind':'Variable','name':{'kind':'Name','value':'metric'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'FragmentSpread','name':{'kind':'Name','value':'MetricParts'}}]}}]}}]} as unknown as DocumentNode<TimeSeriesMetricFragment, unknown>
export const MinionUptimePartsFragmentDoc = {'kind':'Document','definitions':[{'kind':'FragmentDefinition','name':{'kind':'Name','value':'MinionUptimeParts'},'typeCondition':{'kind':'NamedType','name':{'kind':'Name','value':'Query'}},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','alias':{'kind':'Name','value':'minionUptime'},'name':{'kind':'Name','value':'metric'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'name'},'value':{'kind':'StringValue','value':'minion_uptime_sec','block':false}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'FragmentSpread','name':{'kind':'Name','value':'MetricParts'}}]}}]}}]} as unknown as DocumentNode<MinionUptimePartsFragment, unknown>
export const MinionLatencyPartsFragmentDoc = {'kind':'Document','definitions':[{'kind':'FragmentDefinition','name':{'kind':'Name','value':'MinionLatencyParts'},'typeCondition':{'kind':'NamedType','name':{'kind':'Name','value':'Query'}},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','alias':{'kind':'Name','value':'minionLatency'},'name':{'kind':'Name','value':'metric'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'name'},'value':{'kind':'StringValue','value':'response_time_msec','block':false}},{'kind':'Argument','name':{'kind':'Name','value':'labels'},'value':{'kind':'ObjectValue','fields':[{'kind':'ObjectField','name':{'kind':'Name','value':'instance'},'value':{'kind':'Variable','name':{'kind':'Name','value':'instance'}}},{'kind':'ObjectField','name':{'kind':'Name','value':'monitor'},'value':{'kind':'Variable','name':{'kind':'Name','value':'monitor'}}}]}},{'kind':'Argument','name':{'kind':'Name','value':'timeRange'},'value':{'kind':'Variable','name':{'kind':'Name','value':'timeRange'}}},{'kind':'Argument','name':{'kind':'Name','value':'timeRangeUnit'},'value':{'kind':'Variable','name':{'kind':'Name','value':'timeRangeUnit'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'status'}},{'kind':'FragmentSpread','name':{'kind':'Name','value':'MetricParts'}}]}}]}}]} as unknown as DocumentNode<MinionLatencyPartsFragment, unknown>
export const NodeLatencyPartsFragmentDoc = {'kind':'Document','definitions':[{'kind':'FragmentDefinition','name':{'kind':'Name','value':'NodeLatencyParts'},'typeCondition':{'kind':'NamedType','name':{'kind':'Name','value':'Query'}},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','alias':{'kind':'Name','value':'nodeLatency'},'name':{'kind':'Name','value':'metric'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'name'},'value':{'kind':'StringValue','value':'response_time_msec','block':false}},{'kind':'Argument','name':{'kind':'Name','value':'labels'},'value':{'kind':'ObjectValue','fields':[{'kind':'ObjectField','name':{'kind':'Name','value':'node_id'},'value':{'kind':'Variable','name':{'kind':'Name','value':'id'}}},{'kind':'ObjectField','name':{'kind':'Name','value':'monitor'},'value':{'kind':'Variable','name':{'kind':'Name','value':'monitor'}}},{'kind':'ObjectField','name':{'kind':'Name','value':'instance'},'value':{'kind':'Variable','name':{'kind':'Name','value':'instance'}}}]}},{'kind':'Argument','name':{'kind':'Name','value':'timeRange'},'value':{'kind':'Variable','name':{'kind':'Name','value':'timeRange'}}},{'kind':'Argument','name':{'kind':'Name','value':'timeRangeUnit'},'value':{'kind':'Variable','name':{'kind':'Name','value':'timeRangeUnit'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'status'}},{'kind':'FragmentSpread','name':{'kind':'Name','value':'MetricParts'}}]}}]}}]} as unknown as DocumentNode<NodeLatencyPartsFragment, unknown>
export const NodeStatusPartsFragmentDoc = {'kind':'Document','definitions':[{'kind':'FragmentDefinition','name':{'kind':'Name','value':'NodeStatusParts'},'typeCondition':{'kind':'NamedType','name':{'kind':'Name','value':'Query'}},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'nodeStatus'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'id'},'value':{'kind':'Variable','name':{'kind':'Name','value':'id'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'status'}}]}}]}}]} as unknown as DocumentNode<NodeStatusPartsFragment, unknown>
export const NodesPartsFragmentDoc = {'kind':'Document','definitions':[{'kind':'FragmentDefinition','name':{'kind':'Name','value':'NodesParts'},'typeCondition':{'kind':'NamedType','name':{'kind':'Name','value':'Query'}},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'findAllNodes'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'createTime'}},{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'ipInterfaces'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'ipAddress'}},{'kind':'Field','name':{'kind':'Name','value':'nodeId'}},{'kind':'Field','name':{'kind':'Name','value':'snmpPrimary'}}]}},{'kind':'Field','name':{'kind':'Name','value':'location'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'location'}}]}},{'kind':'Field','name':{'kind':'Name','value':'monitoringLocationId'}},{'kind':'Field','name':{'kind':'Name','value':'nodeLabel'}},{'kind':'Field','name':{'kind':'Name','value':'scanType'}}]}}]}}]} as unknown as DocumentNode<NodesPartsFragment, unknown>
export const TagsPartsFragmentDoc = {'kind':'Document','definitions':[{'kind':'FragmentDefinition','name':{'kind':'Name','value':'TagsParts'},'typeCondition':{'kind':'NamedType','name':{'kind':'Name','value':'Query'}},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'tags'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'name'}}]}}]}}]} as unknown as DocumentNode<TagsPartsFragment, unknown>
export const TagsSearchPartsFragmentDoc = {'kind':'Document','definitions':[{'kind':'FragmentDefinition','name':{'kind':'Name','value':'TagsSearchParts'},'typeCondition':{'kind':'NamedType','name':{'kind':'Name','value':'Query'}},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'tags'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'searchTerm'},'value':{'kind':'Variable','name':{'kind':'Name','value':'searchTerm'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'name'}}]}}]}}]} as unknown as DocumentNode<TagsSearchPartsFragment, unknown>
export const NodesTablePartsFragmentDoc = {'kind':'Document','definitions':[{'kind':'FragmentDefinition','name':{'kind':'Name','value':'NodesTableParts'},'typeCondition':{'kind':'NamedType','name':{'kind':'Name','value':'Query'}},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'findAllNodes'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'nodeLabel'}},{'kind':'Field','name':{'kind':'Name','value':'createTime'}},{'kind':'Field','name':{'kind':'Name','value':'monitoringLocationId'}},{'kind':'Field','name':{'kind':'Name','value':'ipInterfaces'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'ipAddress'}},{'kind':'Field','name':{'kind':'Name','value':'snmpPrimary'}}]}},{'kind':'Field','name':{'kind':'Name','value':'scanType'}}]}}]}}]} as unknown as DocumentNode<NodesTablePartsFragment, unknown>
export const MinionsTablePartsFragmentDoc = {'kind':'Document','definitions':[{'kind':'FragmentDefinition','name':{'kind':'Name','value':'MinionsTableParts'},'typeCondition':{'kind':'NamedType','name':{'kind':'Name','value':'Query'}},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'findAllMinions'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'label'}},{'kind':'Field','name':{'kind':'Name','value':'lastCheckedTime'}},{'kind':'Field','name':{'kind':'Name','value':'location'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'location'}}]}},{'kind':'Field','name':{'kind':'Name','value':'status'}},{'kind':'Field','name':{'kind':'Name','value':'systemId'}}]}}]}}]} as unknown as DocumentNode<MinionsTablePartsFragment, unknown>
export const MonitoringPolicyPartsFragmentDoc = {'kind':'Document','definitions':[{'kind':'FragmentDefinition','name':{'kind':'Name','value':'MonitoringPolicyParts'},'typeCondition':{'kind':'NamedType','name':{'kind':'Name','value':'MonitorPolicy'}},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'memo'}},{'kind':'Field','name':{'kind':'Name','value':'name'}},{'kind':'Field','name':{'kind':'Name','value':'notifyByEmail'}},{'kind':'Field','name':{'kind':'Name','value':'notifyByPagerDuty'}},{'kind':'Field','name':{'kind':'Name','value':'notifyByWebhooks'}},{'kind':'Field','name':{'kind':'Name','value':'rules'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'name'}},{'kind':'Field','name':{'kind':'Name','value':'componentType'}},{'kind':'Field','name':{'kind':'Name','value':'detectionMethod'}},{'kind':'Field','name':{'kind':'Name','value':'eventType'}},{'kind':'Field','name':{'kind':'Name','value':'thresholdMetricName'}},{'kind':'Field','name':{'kind':'Name','value':'alertConditions'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'count'}},{'kind':'Field','name':{'kind':'Name','value':'clearEvent'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'name'}},{'kind':'Field','name':{'kind':'Name','value':'eventType'}}]}},{'kind':'Field','name':{'kind':'Name','value':'overtime'}},{'kind':'Field','name':{'kind':'Name','value':'overtimeUnit'}},{'kind':'Field','name':{'kind':'Name','value':'severity'}},{'kind':'Field','name':{'kind':'Name','value':'triggerEvent'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'name'}},{'kind':'Field','name':{'kind':'Name','value':'eventType'}}]}}]}}]}},{'kind':'Field','name':{'kind':'Name','value':'tags'}}]}}]} as unknown as DocumentNode<MonitoringPolicyPartsFragment, unknown>
export const EventsByNodeIdPartsFragmentDoc = {'kind':'Document','definitions':[{'kind':'FragmentDefinition','name':{'kind':'Name','value':'EventsByNodeIdParts'},'typeCondition':{'kind':'NamedType','name':{'kind':'Name','value':'Query'}},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','alias':{'kind':'Name','value':'events'},'name':{'kind':'Name','value':'findEventsByNodeId'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'id'},'value':{'kind':'Variable','name':{'kind':'Name','value':'id'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'uei'}},{'kind':'Field','name':{'kind':'Name','value':'nodeId'}},{'kind':'Field','name':{'kind':'Name','value':'ipAddress'}},{'kind':'Field','name':{'kind':'Name','value':'producedTime'}}]}}]}}]} as unknown as DocumentNode<EventsByNodeIdPartsFragment, unknown>
export const NodeByIdPartsFragmentDoc = {'kind':'Document','definitions':[{'kind':'FragmentDefinition','name':{'kind':'Name','value':'NodeByIdParts'},'typeCondition':{'kind':'NamedType','name':{'kind':'Name','value':'Query'}},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','alias':{'kind':'Name','value':'node'},'name':{'kind':'Name','value':'findNodeById'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'id'},'value':{'kind':'Variable','name':{'kind':'Name','value':'id'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'nodeLabel'}},{'kind':'Field','name':{'kind':'Name','value':'nodeAlias'}},{'kind':'Field','name':{'kind':'Name','value':'objectId'}},{'kind':'Field','name':{'kind':'Name','value':'systemContact'}},{'kind':'Field','name':{'kind':'Name','value':'systemDescr'}},{'kind':'Field','name':{'kind':'Name','value':'systemLocation'}},{'kind':'Field','name':{'kind':'Name','value':'systemName'}},{'kind':'Field','name':{'kind':'Name','value':'scanType'}},{'kind':'Field','name':{'kind':'Name','value':'location'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'location'}}]}},{'kind':'Field','name':{'kind':'Name','value':'ipInterfaces'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'hostname'}},{'kind':'Field','name':{'kind':'Name','value':'ipAddress'}},{'kind':'Field','name':{'kind':'Name','value':'netmask'}},{'kind':'Field','name':{'kind':'Name','value':'nodeId'}},{'kind':'Field','name':{'kind':'Name','value':'snmpPrimary'}},{'kind':'Field','name':{'kind':'Name','value':'azureInterfaceId'}}]}},{'kind':'Field','name':{'kind':'Name','value':'snmpInterfaces'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'ifAdminStatus'}},{'kind':'Field','name':{'kind':'Name','value':'ifAlias'}},{'kind':'Field','name':{'kind':'Name','value':'ifDescr'}},{'kind':'Field','name':{'kind':'Name','value':'ifIndex'}},{'kind':'Field','name':{'kind':'Name','value':'ifName'}},{'kind':'Field','name':{'kind':'Name','value':'ifOperatorStatus'}},{'kind':'Field','name':{'kind':'Name','value':'ifSpeed'}},{'kind':'Field','name':{'kind':'Name','value':'ifType'}},{'kind':'Field','name':{'kind':'Name','value':'ipAddress'}},{'kind':'Field','name':{'kind':'Name','value':'nodeId'}},{'kind':'Field','name':{'kind':'Name','value':'physicalAddr'}}]}},{'kind':'Field','name':{'kind':'Name','value':'azureInterfaces'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'nodeId'}},{'kind':'Field','name':{'kind':'Name','value':'interfaceName'}},{'kind':'Field','name':{'kind':'Name','value':'privateIpId'}},{'kind':'Field','name':{'kind':'Name','value':'publicIpAddress'}},{'kind':'Field','name':{'kind':'Name','value':'publicIpId'}},{'kind':'Field','name':{'kind':'Name','value':'location'}}]}}]}}]}}]} as unknown as DocumentNode<NodeByIdPartsFragment, unknown>
export const AcknowledgeAlertsDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'mutation','name':{'kind':'Name','value':'AcknowledgeAlerts'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'ids'}},'type':{'kind':'ListType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Long'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'acknowledgeAlert'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'ids'},'value':{'kind':'Variable','name':{'kind':'Name','value':'ids'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'alertList'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'acknowledged'}},{'kind':'Field','name':{'kind':'Name','value':'databaseId'}}]}},{'kind':'Field','name':{'kind':'Name','value':'alertErrorList'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'databaseId'}},{'kind':'Field','name':{'kind':'Name','value':'error'}}]}}]}}]}}]} as unknown as DocumentNode<AcknowledgeAlertsMutation, AcknowledgeAlertsMutationVariables>
export const ClearAlertsDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'mutation','name':{'kind':'Name','value':'ClearAlerts'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'ids'}},'type':{'kind':'ListType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Long'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'clearAlert'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'ids'},'value':{'kind':'Variable','name':{'kind':'Name','value':'ids'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'alertList'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'acknowledged'}},{'kind':'Field','name':{'kind':'Name','value':'databaseId'}}]}},{'kind':'Field','name':{'kind':'Name','value':'alertErrorList'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'databaseId'}},{'kind':'Field','name':{'kind':'Name','value':'error'}}]}}]}}]}}]} as unknown as DocumentNode<ClearAlertsMutation, ClearAlertsMutationVariables>
export const AlertsListDocument = {'kind':'Document', 'definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'AlertsList'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'page'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Int'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'pageSize'}},'type':{'kind':'NamedType','name':{'kind':'Name','value':'Int'}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'severities'}},'type':{'kind':'ListType','type':{'kind':'NamedType','name':{'kind':'Name','value':'String'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'sortAscending'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Boolean'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'sortBy'}},'type':{'kind':'NamedType','name':{'kind':'Name','value':'String'}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'timeRange'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'TimeRange'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'nodeLabel'}},'type':{'kind':'NamedType','name':{'kind':'Name','value':'String'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'FragmentSpread','name':{'kind':'Name','value':'AlertsParts'}}]}},...AlertsPartsFragmentDoc.definitions]} as unknown as DocumentNode<AlertsListQuery, AlertsListQueryVariables>
export const CountAlertsDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'CountAlerts'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'severityFilters'}},'type':{'kind':'ListType','type':{'kind':'NamedType','name':{'kind':'Name','value':'String'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'timeRange'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'TimeRange'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'countAlerts'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'severityFilters'},'value':{'kind':'Variable','name':{'kind':'Name','value':'severityFilters'}}},{'kind':'Argument','name':{'kind':'Name','value':'timeRange'},'value':{'kind':'Variable','name':{'kind':'Name','value':'timeRange'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'count'}},{'kind':'Field','name':{'kind':'Name','value':'error'}}]}}]}}]} as unknown as DocumentNode<CountAlertsQuery, CountAlertsQueryVariables>
export const AlertCountsDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'AlertCounts'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'alertCounts'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'acknowledgedCount'}},{'kind':'Field','name':{'kind':'Name','value':'totalAlertCount'}},{'kind':'Field','name':{'kind':'Name','value':'countBySeverity'}}]}}]}}]} as unknown as DocumentNode<AlertCountsQuery, AlertCountsQueryVariables>
export const CreateAzureActiveDiscoveryDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'mutation','name':{'kind':'Name','value':'CreateAzureActiveDiscovery'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'discovery'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'AzureActiveDiscoveryCreateInput'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'createAzureActiveDiscovery'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'discovery'},'value':{'kind':'Variable','name':{'kind':'Name','value':'discovery'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'createTimeMsec'}},{'kind':'Field','name':{'kind':'Name','value':'locationId'}},{'kind':'Field','name':{'kind':'Name','value':'subscriptionId'}},{'kind':'Field','name':{'kind':'Name','value':'clientId'}}]}}]}}]} as unknown as DocumentNode<CreateAzureActiveDiscoveryMutation, CreateAzureActiveDiscoveryMutationVariables>
export const CreateIcmpActiveDiscoveryDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'mutation','name':{'kind':'Name','value':'CreateIcmpActiveDiscovery'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'request'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'IcmpActiveDiscoveryCreateInput'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'createIcmpActiveDiscovery'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'request'},'value':{'kind':'Variable','name':{'kind':'Name','value':'request'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'name'}},{'kind':'Field','name':{'kind':'Name','value':'ipAddresses'}},{'kind':'Field','name':{'kind':'Name','value':'locationId'}},{'kind':'Field','name':{'kind':'Name','value':'snmpConfig'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'ports'}},{'kind':'Field','name':{'kind':'Name','value':'readCommunities'}}]}}]}}]}}]} as unknown as DocumentNode<CreateIcmpActiveDiscoveryMutation, CreateIcmpActiveDiscoveryMutationVariables>
export const CreateOrUpdateActiveIcmpDiscoveryDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'mutation','name':{'kind':'Name','value':'CreateOrUpdateActiveIcmpDiscovery'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'request'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'IcmpActiveDiscoveryCreateInput'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'upsertIcmpActiveDiscovery'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'request'},'value':{'kind':'Variable','name':{'kind':'Name','value':'request'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'name'}},{'kind':'Field','name':{'kind':'Name','value':'ipAddresses'}},{'kind':'Field','name':{'kind':'Name','value':'locationId'}},{'kind':'Field','name':{'kind':'Name','value':'snmpConfig'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'ports'}},{'kind':'Field','name':{'kind':'Name','value':'readCommunities'}}]}}]}}]}}]} as unknown as DocumentNode<CreateOrUpdateActiveIcmpDiscoveryMutation, CreateOrUpdateActiveIcmpDiscoveryMutationVariables>
export const DeleteActiveIcmpDiscoveryDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'mutation','name':{'kind':'Name','value':'DeleteActiveIcmpDiscovery'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'id'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Long'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'deleteIcmpActiveDiscovery'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'id'},'value':{'kind':'Variable','name':{'kind':'Name','value':'id'}}}]}]}}]} as unknown as DocumentNode<DeleteActiveIcmpDiscoveryMutation, DeleteActiveIcmpDiscoveryMutationVariables>
export const TogglePassiveDiscoveryDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'mutation','name':{'kind':'Name','value':'TogglePassiveDiscovery'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'toggle'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'PassiveDiscoveryToggleInput'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'togglePassiveDiscovery'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'toggle'},'value':{'kind':'Variable','name':{'kind':'Name','value':'toggle'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'toggle'}}]}}]}}]} as unknown as DocumentNode<TogglePassiveDiscoveryMutation, TogglePassiveDiscoveryMutationVariables>
export const UpsertPassiveDiscoveryDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'mutation','name':{'kind':'Name','value':'UpsertPassiveDiscovery'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'passiveDiscovery'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'PassiveDiscoveryUpsertInput'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'upsertPassiveDiscovery'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'discovery'},'value':{'kind':'Variable','name':{'kind':'Name','value':'passiveDiscovery'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'locationId'}},{'kind':'Field','name':{'kind':'Name','value':'name'}},{'kind':'Field','name':{'kind':'Name','value':'snmpCommunities'}},{'kind':'Field','name':{'kind':'Name','value':'snmpPorts'}},{'kind':'Field','name':{'kind':'Name','value':'toggle'}}]}}]}}]} as unknown as DocumentNode<UpsertPassiveDiscoveryMutation, UpsertPassiveDiscoveryMutationVariables>
export const DeletePassiveDiscoveryDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'mutation','name':{'kind':'Name','value':'DeletePassiveDiscovery'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'id'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Long'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'deletePassiveDiscovery'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'id'},'value':{'kind':'Variable','name':{'kind':'Name','value':'id'}}}]}]}}]} as unknown as DocumentNode<DeletePassiveDiscoveryMutation, DeletePassiveDiscoveryMutationVariables>
export const FindApplicationSeriesDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'findApplicationSeries'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'requestCriteria'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'RequestCriteriaInput'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'findApplicationSeries'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'requestCriteria'},'value':{'kind':'Variable','name':{'kind':'Name','value':'requestCriteria'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'timestamp'}},{'kind':'Field','name':{'kind':'Name','value':'label'}},{'kind':'Field','name':{'kind':'Name','value':'value'}},{'kind':'Field','name':{'kind':'Name','value':'direction'}}]}}]}}]} as unknown as DocumentNode<FindApplicationSeriesQuery, FindApplicationSeriesQueryVariables>
export const FindApplicationSummariesDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'findApplicationSummaries'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'requestCriteria'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'RequestCriteriaInput'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'findApplicationSummaries'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'requestCriteria'},'value':{'kind':'Variable','name':{'kind':'Name','value':'requestCriteria'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'label'}},{'kind':'Field','name':{'kind':'Name','value':'bytesIn'}},{'kind':'Field','name':{'kind':'Name','value':'bytesOut'}}]}}]}}]} as unknown as DocumentNode<FindApplicationSummariesQuery, FindApplicationSummariesQueryVariables>
export const FindApplicationsDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'findApplications'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'requestCriteria'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'RequestCriteriaInput'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'findApplications'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'requestCriteria'},'value':{'kind':'Variable','name':{'kind':'Name','value':'requestCriteria'}}}]}]}}]} as unknown as DocumentNode<FindApplicationsQuery, FindApplicationsQueryVariables>
export const FindExportersDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'findExporters'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'requestCriteria'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'RequestCriteriaInput'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'findExporters'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'requestCriteria'},'value':{'kind':'Variable','name':{'kind':'Name','value':'requestCriteria'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'node'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'nodeLabel'}}]}},{'kind':'Field','name':{'kind':'Name','value':'ipInterface'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'ipAddress'}}]}}]}}]}}]} as unknown as DocumentNode<FindExportersQuery, FindExportersQueryVariables>
export const CreateLocationDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'mutation','name':{'kind':'Name','value':'CreateLocation'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'location'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'MonitoringLocationCreateInput'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'createLocation'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'location'},'value':{'kind':'Variable','name':{'kind':'Name','value':'location'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'location'}},{'kind':'Field','name':{'kind':'Name','value':'address'}},{'kind':'Field','name':{'kind':'Name','value':'longitude'}},{'kind':'Field','name':{'kind':'Name','value':'latitude'}}]}}]}}]} as unknown as DocumentNode<CreateLocationMutation, CreateLocationMutationVariables>
export const UpdateLocationDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'mutation','name':{'kind':'Name','value':'UpdateLocation'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'location'}},'type':{'kind':'NamedType','name':{'kind':'Name','value':'MonitoringLocationUpdateInput'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'updateLocation'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'location'},'value':{'kind':'Variable','name':{'kind':'Name','value':'location'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'location'}},{'kind':'Field','name':{'kind':'Name','value':'address'}},{'kind':'Field','name':{'kind':'Name','value':'longitude'}},{'kind':'Field','name':{'kind':'Name','value':'latitude'}}]}}]}}]} as unknown as DocumentNode<UpdateLocationMutation, UpdateLocationMutationVariables>
export const DeleteLocationDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'mutation','name':{'kind':'Name','value':'DeleteLocation'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'id'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Long'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'deleteLocation'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'id'},'value':{'kind':'Variable','name':{'kind':'Name','value':'id'}}}]}]}}]} as unknown as DocumentNode<DeleteLocationMutation, DeleteLocationMutationVariables>
export const RevokeMinionCertificateDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'mutation','name':{'kind':'Name','value':'revokeMinionCertificate'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'locationId'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Long'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'revokeMinionCertificate'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'locationId'},'value':{'kind':'Variable','name':{'kind':'Name','value':'locationId'}}}]}]}}]} as unknown as DocumentNode<RevokeMinionCertificateMutation, RevokeMinionCertificateMutationVariables>
export const LocationsListDocument = {'kind':'Document', 'definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'LocationsList'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'FragmentSpread','name':{'kind':'Name','value':'LocationsParts'}}]}},...LocationsPartsFragmentDoc.definitions]} as unknown as DocumentNode<LocationsListQuery, LocationsListQueryVariables>
export const SearchLocationDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'SearchLocation'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'searchTerm'}},'type':{'kind':'NamedType','name':{'kind':'Name','value':'String'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'searchLocation'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'searchTerm'},'value':{'kind':'Variable','name':{'kind':'Name','value':'searchTerm'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'location'}},{'kind':'Field','name':{'kind':'Name','value':'address'}},{'kind':'Field','name':{'kind':'Name','value':'longitude'}},{'kind':'Field','name':{'kind':'Name','value':'latitude'}}]}}]}}]} as unknown as DocumentNode<SearchLocationQuery, SearchLocationQueryVariables>
export const GetMinionCertificateDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'getMinionCertificate'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'location'}},'type':{'kind':'NamedType','name':{'kind':'Name','value':'Long'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'getMinionCertificate'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'locationId'},'value':{'kind':'Variable','name':{'kind':'Name','value':'location'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'password'}},{'kind':'Field','name':{'kind':'Name','value':'certificate'}}]}}]}}]} as unknown as DocumentNode<GetMinionCertificateQuery, GetMinionCertificateQueryVariables>
export const DeleteMinionDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'mutation','name':{'kind':'Name','value':'DeleteMinion'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'id'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Long'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'deleteMinion'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'id'},'value':{'kind':'Variable','name':{'kind':'Name','value':'id'}}}]}]}}]} as unknown as DocumentNode<DeleteMinionMutation, DeleteMinionMutationVariables>
export const FindMinionsByLocationIdDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'FindMinionsByLocationId'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'locationId'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Long'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'findMinionsByLocationId'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'locationId'},'value':{'kind':'Variable','name':{'kind':'Name','value':'locationId'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'label'}},{'kind':'Field','name':{'kind':'Name','value':'lastCheckedTime'}},{'kind':'Field','name':{'kind':'Name','value':'status'}},{'kind':'Field','name':{'kind':'Name','value':'systemId'}},{'kind':'Field','name':{'kind':'Name','value':'location'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'location'}}]}}]}}]}}]} as unknown as DocumentNode<FindMinionsByLocationIdQuery, FindMinionsByLocationIdQueryVariables>
export const CreateMonitorPolicyDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'mutation','name':{'kind':'Name','value':'CreateMonitorPolicy'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'policy'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'MonitorPolicyInput'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'createMonitorPolicy'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'policy'},'value':{'kind':'Variable','name':{'kind':'Name','value':'policy'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}}]}}]}}]} as unknown as DocumentNode<CreateMonitorPolicyMutation, CreateMonitorPolicyMutationVariables>
export const DeletePolicyByIdDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'mutation','name':{'kind':'Name','value':'DeletePolicyById'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'id'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Long'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'deletePolicyById'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'id'},'value':{'kind':'Variable','name':{'kind':'Name','value':'id'}}}]}]}}]} as unknown as DocumentNode<DeletePolicyByIdMutation, DeletePolicyByIdMutationVariables>
export const DeleteRuleByIdDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'mutation','name':{'kind':'Name','value':'DeleteRuleById'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'id'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Long'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'deleteRuleById'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'id'},'value':{'kind':'Variable','name':{'kind':'Name','value':'id'}}}]}]}}]} as unknown as DocumentNode<DeleteRuleByIdMutation, DeleteRuleByIdMutationVariables>
export const AddNodeDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'mutation','name':{'kind':'Name','value':'AddNode'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'node'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'NodeCreateInput'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'addNode'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'node'},'value':{'kind':'Variable','name':{'kind':'Name','value':'node'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'createTime'}},{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'monitoringLocationId'}},{'kind':'Field','name':{'kind':'Name','value':'nodeLabel'}}]}}]}}]} as unknown as DocumentNode<AddNodeMutation, AddNodeMutationVariables>
export const DeleteNodeDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'mutation','name':{'kind':'Name','value':'DeleteNode'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'id'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Long'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'deleteNode'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'id'},'value':{'kind':'Variable','name':{'kind':'Name','value':'id'}}}]}]}}]} as unknown as DocumentNode<DeleteNodeMutation, DeleteNodeMutationVariables>
export const AddTagsToNodesDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'mutation','name':{'kind':'Name','value':'AddTagsToNodes'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'tags'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'TagListNodesAddInput'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'addTagsToNodes'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'tags'},'value':{'kind':'Variable','name':{'kind':'Name','value':'tags'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'name'}}]}}]}}]} as unknown as DocumentNode<AddTagsToNodesMutation, AddTagsToNodesMutationVariables>
export const RemoveTagsFromNodesDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'mutation','name':{'kind':'Name','value':'RemoveTagsFromNodes'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'tags'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'TagListNodesRemoveInput'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'removeTagsFromNodes'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'tags'},'value':{'kind':'Variable','name':{'kind':'Name','value':'tags'}}}]}]}}]} as unknown as DocumentNode<RemoveTagsFromNodesMutation, RemoveTagsFromNodesMutationVariables>
export const UpdateNodeDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'mutation','name':{'kind':'Name','value':'UpdateNode'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'node'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'NodeUpdateInput'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'updateNode'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'node'},'value':{'kind':'Variable','name':{'kind':'Name','value':'node'}}}]}]}}]} as unknown as DocumentNode<UpdateNodeMutation, UpdateNodeMutationVariables>
export const SavePagerDutyConfigDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'mutation','name':{'kind':'Name','value':'SavePagerDutyConfig'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'config'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'PagerDutyConfigInput'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'savePagerDutyConfig'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'config'},'value':{'kind':'Variable','name':{'kind':'Name','value':'config'}}}]}]}}]} as unknown as DocumentNode<SavePagerDutyConfigMutation, SavePagerDutyConfigMutationVariables>
export const ListTagsDocument = {'kind':'Document', 'definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'ListTags'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'FragmentSpread','name':{'kind':'Name','value':'TagsParts'}}]}},...TagsPartsFragmentDoc.definitions]} as unknown as DocumentNode<ListTagsQuery, ListTagsQueryVariables>
export const ListTagsSearchDocument = {'kind':'Document', 'definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'ListTagsSearch'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'searchTerm'}},'type':{'kind':'NamedType','name':{'kind':'Name','value':'String'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'FragmentSpread','name':{'kind':'Name','value':'TagsSearchParts'}}]}},...TagsSearchPartsFragmentDoc.definitions]} as unknown as DocumentNode<ListTagsSearchQuery, ListTagsSearchQueryVariables>
export const ListTagsByNodeIdsDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'ListTagsByNodeIds'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'nodeIds'}},'type':{'kind':'ListType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Long'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'tagsByNodeIds'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'nodeIds'},'value':{'kind':'Variable','name':{'kind':'Name','value':'nodeIds'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'nodeId'}},{'kind':'Field','name':{'kind':'Name','value':'tags'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'name'}}]}}]}}]}}]} as unknown as DocumentNode<ListTagsByNodeIdsQuery, ListTagsByNodeIdsQueryVariables>
export const ListAlertEventDefinitionsDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'ListAlertEventDefinitions'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'eventType'}},'type':{'kind':'NamedType','name':{'kind':'Name','value':'EventType'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'listAlertEventDefinitions'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'eventType'},'value':{'kind':'Variable','name':{'kind':'Name','value':'eventType'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'name'}},{'kind':'Field','name':{'kind':'Name','value':'eventType'}}]}}]}}]} as unknown as DocumentNode<ListAlertEventDefinitionsQuery, ListAlertEventDefinitionsQueryVariables>
export const ListNodesForTableDocument = {'kind':'Document', 'definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'ListNodesForTable'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'FragmentSpread','name':{'kind':'Name','value':'NodesTableParts'}}]}},...NodesTablePartsFragmentDoc.definitions]} as unknown as DocumentNode<ListNodesForTableQuery, ListNodesForTableQueryVariables>
export const ListMinionsForTableDocument = {'kind':'Document', 'definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'ListMinionsForTable'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'FragmentSpread','name':{'kind':'Name','value':'MinionsTableParts'}}]}},...MinionsTablePartsFragmentDoc.definitions]} as unknown as DocumentNode<ListMinionsForTableQuery, ListMinionsForTableQueryVariables>
export const ListMinionMetricsDocument = {'kind':'Document', 'definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'ListMinionMetrics'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'instance'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'String'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'monitor'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'String'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'timeRange'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Int'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'timeRangeUnit'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'TimeRangeUnit'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'FragmentSpread','name':{'kind':'Name','value':'MinionLatencyParts'}}]}},...MinionLatencyPartsFragmentDoc.definitions,...MetricPartsFragmentDoc.definitions]} as unknown as DocumentNode<ListMinionMetricsQuery, ListMinionMetricsQueryVariables>
export const ListNodeMetricsDocument = {'kind':'Document', 'definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'ListNodeMetrics'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'id'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Long'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'monitor'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'String'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'instance'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'String'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'timeRange'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Int'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'timeRangeUnit'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'TimeRangeUnit'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'FragmentSpread','name':{'kind':'Name','value':'NodeLatencyParts'}},{'kind':'FragmentSpread','name':{'kind':'Name','value':'NodeStatusParts'}}]}},...NodeLatencyPartsFragmentDoc.definitions,...MetricPartsFragmentDoc.definitions,...NodeStatusPartsFragmentDoc.definitions]} as unknown as DocumentNode<ListNodeMetricsQuery, ListNodeMetricsQueryVariables>
export const ListMinionsAndDevicesForTablesDocument = {'kind':'Document', 'definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'ListMinionsAndDevicesForTables'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'FragmentSpread','name':{'kind':'Name','value':'NodesTableParts'}},{'kind':'FragmentSpread','name':{'kind':'Name','value':'MinionsTableParts'}},{'kind':'FragmentSpread','name':{'kind':'Name','value':'LocationsParts'}}]}},...NodesTablePartsFragmentDoc.definitions,...MinionsTablePartsFragmentDoc.definitions,...LocationsPartsFragmentDoc.definitions]} as unknown as DocumentNode<ListMinionsAndDevicesForTablesQuery, ListMinionsAndDevicesForTablesQueryVariables>
export const NetworkTrafficDocument = {'kind':'Document', 'definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'NetworkTraffic'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'name'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'String'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'timeRange'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Int'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'timeRangeUnit'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'TimeRangeUnit'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'metric'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'name'},'value':{'kind':'Variable','name':{'kind':'Name','value':'name'}}},{'kind':'Argument','name':{'kind':'Name','value':'timeRange'},'value':{'kind':'Variable','name':{'kind':'Name','value':'timeRange'}}},{'kind':'Argument','name':{'kind':'Name','value':'timeRangeUnit'},'value':{'kind':'Variable','name':{'kind':'Name','value':'timeRangeUnit'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'status'}},{'kind':'FragmentSpread','name':{'kind':'Name','value':'MetricParts'}}]}}]}},...MetricPartsFragmentDoc.definitions]} as unknown as DocumentNode<NetworkTrafficQuery, NetworkTrafficQueryVariables>
export const TopNNodesDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'TopNNodes'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'timeRange'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Int'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'timeRangeUnit'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'TimeRangeUnit'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'sortAscending'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Boolean'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'pageSize'}},'type':{'kind':'NamedType','name':{'kind':'Name','value':'Int'}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'sortBy'}},'type':{'kind':'NamedType','name':{'kind':'Name','value':'String'}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'page'}},'type':{'kind':'NamedType','name':{'kind':'Name','value':'Int'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'topNNode'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'timeRange'},'value':{'kind':'Variable','name':{'kind':'Name','value':'timeRange'}}},{'kind':'Argument','name':{'kind':'Name','value':'timeRangeUnit'},'value':{'kind':'Variable','name':{'kind':'Name','value':'timeRangeUnit'}}},{'kind':'Argument','name':{'kind':'Name','value':'sortAscending'},'value':{'kind':'Variable','name':{'kind':'Name','value':'sortAscending'}}},{'kind':'Argument','name':{'kind':'Name','value':'pageSize'},'value':{'kind':'Variable','name':{'kind':'Name','value':'pageSize'}}},{'kind':'Argument','name':{'kind':'Name','value':'sortBy'},'value':{'kind':'Variable','name':{'kind':'Name','value':'sortBy'}}},{'kind':'Argument','name':{'kind':'Name','value':'page'},'value':{'kind':'Variable','name':{'kind':'Name','value':'page'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'nodeLabel'}},{'kind':'Field','name':{'kind':'Name','value':'location'}},{'kind':'Field','name':{'kind':'Name','value':'avgResponseTime'}},{'kind':'Field','name':{'kind':'Name','value':'reachability'}}]}},{'kind':'Field','name':{'kind':'Name','value':'allNodeStatus'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'status'}}]}},{'kind':'Field','name':{'kind':'Name','value':'nodeCount'}}]}}]} as unknown as DocumentNode<TopNNodesQuery, TopNNodesQueryVariables>
export const DownloadTopNDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'DownloadTopN'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'downloadFormat'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'DownloadFormat'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'timeRange'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Int'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'timeRangeUnit'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'TimeRangeUnit'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'sortAscending'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Boolean'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'sortBy'}},'type':{'kind':'NamedType','name':{'kind':'Name','value':'String'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'downloadTopN'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'timeRange'},'value':{'kind':'Variable','name':{'kind':'Name','value':'timeRange'}}},{'kind':'Argument','name':{'kind':'Name','value':'timeRangeUnit'},'value':{'kind':'Variable','name':{'kind':'Name','value':'timeRangeUnit'}}},{'kind':'Argument','name':{'kind':'Name','value':'sortAscending'},'value':{'kind':'Variable','name':{'kind':'Name','value':'sortAscending'}}},{'kind':'Argument','name':{'kind':'Name','value':'sortBy'},'value':{'kind':'Variable','name':{'kind':'Name','value':'sortBy'}}},{'kind':'Argument','name':{'kind':'Name','value':'downloadFormat'},'value':{'kind':'Variable','name':{'kind':'Name','value':'downloadFormat'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'topNBytes'}}]}}]}}]} as unknown as DocumentNode<DownloadTopNQuery, DownloadTopNQueryVariables>
export const DownloadIpInterfacesDocument = {'kind': 'Document','definitions': [{'kind': 'OperationDefinition','operation': 'query','name': {'kind': 'Name','value': 'DownloadIpInterfacesByNodeAndSearchTerm'},'variableDefinitions': [{'kind': 'VariableDefinition','variable': {'kind': 'Variable','name': {'kind': 'Name','value': 'nodeId'}},'type': {'kind': 'NonNullType','type': {'kind': 'NamedType','name': {'kind': 'Name','value': 'Long'}}}},{'kind': 'VariableDefinition','variable': {'kind': 'Variable','name': {'kind': 'Name','value': 'searchTerm'}},'type': {'kind': 'NonNullType','type': {'kind': 'NamedType','name': {'kind': 'Name','value': 'String'}}}},{'kind': 'VariableDefinition','variable': {'kind': 'Variable','name': {'kind': 'Name','value': 'downloadFormat'}},'type': {'kind': 'NonNullType','type': {'kind': 'NamedType','name': {'kind': 'Name','value': 'DownloadFormat'}}}}],'selectionSet': {'kind': 'SelectionSet','selections': [{'kind': 'Field','name': {'kind': 'Name','value': 'downloadIpInterfacesByNodeAndSearchTerm'},'arguments': [{'kind': 'Argument','name': {'kind': 'Name','value': 'nodeId'},'value': {'kind': 'Variable','name': {'kind': 'Name','value': 'nodeId'}}},{'kind': 'Argument','name': {'kind': 'Name','value': 'searchTerm'},'value': {'kind': 'Variable','name': {'kind': 'Name','value': 'searchTerm'}}},{'kind': 'Argument','name': {'kind': 'Name','value': 'downloadFormat'},'value': {'kind': 'Variable','name': {'kind': 'Name','value': 'downloadFormat'}}}],'selectionSet': {'kind': 'SelectionSet','selections': [{'kind': 'Field','name': {'kind': 'Name','value': 'ipInterfaces'}}]}}]}}]} as unknown as DocumentNode<DownloadIpInterfacesByNodeAndSearchTermQuery, DownloadCsvVariables>
export const DownloadSNMPInterfacesDocument = {'kind': 'Document','definitions': [{'kind': 'OperationDefinition','operation': 'query','name': {'kind': 'Name','value': 'DownloadSnmpInterfaces'},'variableDefinitions': [{'kind': 'VariableDefinition','variable': {'kind': 'Variable','name': {'kind': 'Name','value': 'nodeId'}},'type': {'kind': 'NonNullType','type': {'kind': 'NamedType','name': {'kind': 'Name','value': 'Long'}}}},{'kind': 'VariableDefinition','variable': {'kind': 'Variable','name': {'kind': 'Name','value': 'searchTerm'}},'type': {'kind': 'NonNullType','type': {'kind': 'NamedType','name': {'kind': 'Name','value': 'String'}}}},{'kind': 'VariableDefinition','variable': {'kind': 'Variable','name': {'kind': 'Name','value': 'downloadFormat'}},'type': {'kind': 'NonNullType','type': {'kind': 'NamedType','name': {'kind': 'Name','value': 'DownloadFormat'}}}}],'selectionSet': {'kind': 'SelectionSet','selections': [{'kind': 'Field','name': {'kind': 'Name','value': 'downloadSnmpInterfaces'},'arguments': [{'kind': 'Argument','name': {'kind': 'Name','value': 'nodeId'},'value': {'kind': 'Variable','name': {'kind': 'Name','value': 'nodeId'}}}, {'kind': 'Argument','name': {'kind': 'Name','value': 'searchTerm'},'value': {'kind': 'Variable','name': {'kind': 'Name','value': 'searchTerm'}}}, {'kind': 'Argument','name': {'kind': 'Name','value': 'downloadFormat'},'value': {'kind': 'Variable','name': {'kind': 'Name','value': 'downloadFormat'}}}],'selectionSet': {'kind': 'SelectionSet','selections': [{'kind': 'Field','name': {'kind': 'Name','value': 'downloadFormat'}}, {'kind': 'Field','name': {'kind': 'Name','value': 'snmpInterfaceBytes'}}]}}]}}]} as unknown as DocumentNode < DownloadSNMPInterfacesByNodeAndSearchTermQuery, DownloadCsvVariables >
export const ListLocationsForDiscoveryDocument = {'kind':'Document', 'definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'ListLocationsForDiscovery'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'FragmentSpread','name':{'kind':'Name','value':'LocationsParts'}}]}},...LocationsPartsFragmentDoc.definitions]} as unknown as DocumentNode<ListLocationsForDiscoveryQuery, ListLocationsForDiscoveryQueryVariables>
export const ListDiscoveriesDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'ListDiscoveries'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'passiveDiscoveries'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'locationId'}},{'kind':'Field','name':{'kind':'Name','value':'name'}},{'kind':'Field','name':{'kind':'Name','value':'snmpCommunities'}},{'kind':'Field','name':{'kind':'Name','value':'snmpPorts'}},{'kind':'Field','name':{'kind':'Name','value':'toggle'}}]}},{'kind':'Field','name':{'kind':'Name','value':'listActiveDiscovery'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'details'}},{'kind':'Field','name':{'kind':'Name','value':'discoveryType'}}]}}]}}]} as unknown as DocumentNode<ListDiscoveriesQuery, ListDiscoveriesQueryVariables>
export const TagsByActiveDiscoveryIdDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'TagsByActiveDiscoveryId'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'discoveryId'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Long'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'tagsByActiveDiscoveryId'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'activeDiscoveryId'},'value':{'kind':'Variable','name':{'kind':'Name','value':'discoveryId'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'name'}}]}}]}}]} as unknown as DocumentNode<TagsByActiveDiscoveryIdQuery, TagsByActiveDiscoveryIdQueryVariables>
export const TagsByPassiveDiscoveryIdDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'TagsByPassiveDiscoveryId'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'discoveryId'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Long'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'tagsByPassiveDiscoveryId'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'passiveDiscoveryId'},'value':{'kind':'Variable','name':{'kind':'Name','value':'discoveryId'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'name'}}]}}]}}]} as unknown as DocumentNode<TagsByPassiveDiscoveryIdQuery, TagsByPassiveDiscoveryIdQueryVariables>
export const GetMetricDocument = {'kind':'Document', 'definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'GetMetric'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'metric'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'String'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'FragmentSpread','name':{'kind':'Name','value':'TimeSeriesMetric'}}]}},...TimeSeriesMetricFragmentDoc.definitions,...MetricPartsFragmentDoc.definitions]} as unknown as DocumentNode<GetMetricQuery, GetMetricQueryVariables>
export const GetTimeSeriesMetricDocument = {'kind':'Document', 'definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'GetTimeSeriesMetric'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'name'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'String'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'monitor'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'String'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'nodeId'}},'type':{'kind':'NamedType','name':{'kind':'Name','value':'String'}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'timeRange'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Int'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'timeRangeUnit'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'TimeRangeUnit'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'instance'}},'type':{'kind':'NamedType','name':{'kind':'Name','value':'String'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','alias':{'kind':'Name','value':'metric'},'name':{'kind':'Name','value':'metric'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'name'},'value':{'kind':'Variable','name':{'kind':'Name','value':'name'}}},{'kind':'Argument','name':{'kind':'Name','value':'labels'},'value':{'kind':'ObjectValue','fields':[{'kind':'ObjectField','name':{'kind':'Name','value':'monitor'},'value':{'kind':'Variable','name':{'kind':'Name','value':'monitor'}}},{'kind':'ObjectField','name':{'kind':'Name','value':'node_id'},'value':{'kind':'Variable','name':{'kind':'Name','value':'nodeId'}}},{'kind':'ObjectField','name':{'kind':'Name','value':'instance'},'value':{'kind':'Variable','name':{'kind':'Name','value':'instance'}}}]}},{'kind':'Argument','name':{'kind':'Name','value':'timeRange'},'value':{'kind':'Variable','name':{'kind':'Name','value':'timeRange'}}},{'kind':'Argument','name':{'kind':'Name','value':'timeRangeUnit'},'value':{'kind':'Variable','name':{'kind':'Name','value':'timeRangeUnit'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'FragmentSpread','name':{'kind':'Name','value':'MetricParts'}}]}}]}},...MetricPartsFragmentDoc.definitions]} as unknown as DocumentNode<GetTimeSeriesMetricQuery, GetTimeSeriesMetricQueryVariables>
export const GetTimeSeriesMetricsWithIfNameDocument = {'kind':'Document', 'definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'GetTimeSeriesMetricsWithIfName'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'name'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'String'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'monitor'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'String'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'nodeId'}},'type':{'kind':'NamedType','name':{'kind':'Name','value':'String'}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'timeRange'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Int'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'timeRangeUnit'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'TimeRangeUnit'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'ifName'}},'type':{'kind':'NamedType','name':{'kind':'Name','value':'String'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','alias':{'kind':'Name','value':'metric'},'name':{'kind':'Name','value':'metric'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'name'},'value':{'kind':'Variable','name':{'kind':'Name','value':'name'}}},{'kind':'Argument','name':{'kind':'Name','value':'labels'},'value':{'kind':'ObjectValue','fields':[{'kind':'ObjectField','name':{'kind':'Name','value':'monitor'},'value':{'kind':'Variable','name':{'kind':'Name','value':'monitor'}}},{'kind':'ObjectField','name':{'kind':'Name','value':'node_id'},'value':{'kind':'Variable','name':{'kind':'Name','value':'nodeId'}}},{'kind':'ObjectField','name':{'kind':'Name','value':'if_name'},'value':{'kind':'Variable','name':{'kind':'Name','value':'ifName'}}}]}},{'kind':'Argument','name':{'kind':'Name','value':'timeRange'},'value':{'kind':'Variable','name':{'kind':'Name','value':'timeRange'}}},{'kind':'Argument','name':{'kind':'Name','value':'timeRangeUnit'},'value':{'kind':'Variable','name':{'kind':'Name','value':'timeRangeUnit'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'FragmentSpread','name':{'kind':'Name','value':'MetricParts'}}]}}]}},...MetricPartsFragmentDoc.definitions]} as unknown as DocumentNode<GetTimeSeriesMetricsWithIfNameQuery, GetTimeSeriesMetricsWithIfNameQueryVariables>
export const GetNodeForGraphsDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'GetNodeForGraphs'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'id'}},'type':{'kind':'NamedType','name':{'kind':'Name','value':'Long'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'findNodeById'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'id'},'value':{'kind':'Variable','name':{'kind':'Name','value':'id'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'ipInterfaces'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'ipAddress'}},{'kind':'Field','name':{'kind':'Name','value':'snmpPrimary'}}]}},{'kind':'Field','name':{'kind':'Name','value':'scanType'}}]}}]}}]} as unknown as DocumentNode<GetNodeForGraphsQuery, GetNodeForGraphsQueryVariables>
export const NodesListDocument = {'kind':'Document', 'definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'NodesList'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'FragmentSpread','name':{'kind':'Name','value':'NodesParts'}}]}},...NodesPartsFragmentDoc.definitions]} as unknown as DocumentNode<NodesListQuery, NodesListQueryVariables>
export const NodeLatencyMetricDocument = {'kind':'Document', 'definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'NodeLatencyMetric'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'id'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Long'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'monitor'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'String'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'instance'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'String'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'timeRange'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Int'}}}},{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'timeRangeUnit'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'TimeRangeUnit'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'FragmentSpread','name':{'kind':'Name','value':'NodeLatencyParts'}},{'kind':'FragmentSpread','name':{'kind':'Name','value':'NodeStatusParts'}}]}},...NodeLatencyPartsFragmentDoc.definitions,...MetricPartsFragmentDoc.definitions,...NodeStatusPartsFragmentDoc.definitions]} as unknown as DocumentNode<NodeLatencyMetricQuery, NodeLatencyMetricQueryVariables>
export const FindAllNodesByNodeLabelSearchDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'FindAllNodesByNodeLabelSearch'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'labelSearchTerm'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'String'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'findAllNodesByNodeLabelSearch'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'labelSearchTerm'},'value':{'kind':'Variable','name':{'kind':'Name','value':'labelSearchTerm'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'ipInterfaces'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'ipAddress'}},{'kind':'Field','name':{'kind':'Name','value':'nodeId'}},{'kind':'Field','name':{'kind':'Name','value':'snmpPrimary'}}]}},{'kind':'Field','name':{'kind':'Name','value':'location'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'location'}}]}},{'kind':'Field','name':{'kind':'Name','value':'tags'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'name'}}]}},{'kind':'Field','name':{'kind':'Name','value':'monitoredState'}},{'kind':'Field','name':{'kind':'Name','value':'monitoringLocationId'}},{'kind':'Field','name':{'kind':'Name','value':'nodeLabel'}},{'kind':'Field','name':{'kind':'Name','value':'scanType'}}]}},{'kind':'Field','alias':{'kind':'Name','value':'allMetrics'},'name':{'kind':'Name','value':'metric'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'name'},'value':{'kind':'StringValue','value':'response_time_msec','block':false}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'status'}},{'kind':'Field','name':{'kind':'Name','value':'data'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'resultType'}},{'kind':'Field','name':{'kind':'Name','value':'result'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'metric'}},{'kind':'Field','name':{'kind':'Name','value':'value'}},{'kind':'Field','name':{'kind':'Name','value':'values'}}]}}]}}]}}]}}]} as unknown as DocumentNode<FindAllNodesByNodeLabelSearchQuery, FindAllNodesByNodeLabelSearchQueryVariables>
export const FindAllNodesByTagsDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'FindAllNodesByTags'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'tags'}},'type':{'kind':'ListType','type':{'kind':'NamedType','name':{'kind':'Name','value':'String'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'findAllNodesByTags'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'tags'},'value':{'kind':'Variable','name':{'kind':'Name','value':'tags'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'ipInterfaces'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'ipAddress'}},{'kind':'Field','name':{'kind':'Name','value':'nodeId'}},{'kind':'Field','name':{'kind':'Name','value':'snmpPrimary'}}]}},{'kind':'Field','name':{'kind':'Name','value':'location'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'location'}}]}},{'kind':'Field','name':{'kind':'Name','value':'tags'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'name'}}]}},{'kind':'Field','name':{'kind':'Name','value':'monitoredState'}},{'kind':'Field','name':{'kind':'Name','value':'monitoringLocationId'}},{'kind':'Field','name':{'kind':'Name','value':'nodeLabel'}},{'kind':'Field','name':{'kind':'Name','value':'scanType'}}]}},{'kind':'Field','alias':{'kind':'Name','value':'allMetrics'},'name':{'kind':'Name','value':'metric'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'name'},'value':{'kind':'StringValue','value':'response_time_msec','block':false}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'status'}},{'kind':'Field','name':{'kind':'Name','value':'data'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'resultType'}},{'kind':'Field','name':{'kind':'Name','value':'result'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'metric'}},{'kind':'Field','name':{'kind':'Name','value':'value'}},{'kind':'Field','name':{'kind':'Name','value':'values'}}]}}]}}]}}]}}]} as unknown as DocumentNode<FindAllNodesByTagsQuery, FindAllNodesByTagsQueryVariables>
export const FindAllNodesByMonitoredStateDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'FindAllNodesByMonitoredState'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'monitoredState'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'String'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'findAllNodesByMonitoredState'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'monitoredState'},'value':{'kind':'Variable','name':{'kind':'Name','value':'monitoredState'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'ipInterfaces'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'ipAddress'}},{'kind':'Field','name':{'kind':'Name','value':'nodeId'}},{'kind':'Field','name':{'kind':'Name','value':'snmpPrimary'}}]}},{'kind':'Field','name':{'kind':'Name','value':'location'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'location'}}]}},{'kind':'Field','name':{'kind':'Name','value':'monitoringLocationId'}},{'kind':'Field','name':{'kind':'Name','value':'nodeLabel'}},{'kind':'Field','name':{'kind':'Name','value':'scanType'}}]}}]}}]} as unknown as DocumentNode<FindAllNodesByMonitoredStateQuery, FindAllNodesByMonitoredStateQueryVariables>
export const BuildNetworkInventoryPageDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'BuildNetworkInventoryPage'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'findAllNodes'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'ipInterfaces'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'ipAddress'}},{'kind':'Field','name':{'kind':'Name','value':'nodeId'}},{'kind':'Field','name':{'kind':'Name','value':'snmpPrimary'}}]}},{'kind':'Field','name':{'kind':'Name','value':'location'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'location'}}]}},{'kind':'Field','name':{'kind':'Name','value':'tags'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'name'}}]}},{'kind':'Field','name':{'kind':'Name','value':'monitoredState'}},{'kind':'Field','name':{'kind':'Name','value':'monitoringLocationId'}},{'kind':'Field','name':{'kind':'Name','value':'nodeLabel'}},{'kind':'Field','name':{'kind':'Name','value':'scanType'}},{'kind':'Field','name':{'kind':'Name','value':'nodeAlias'}}]}},{'kind':'Field','alias':{'kind':'Name','value':'allMetrics'},'name':{'kind':'Name','value':'metric'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'name'},'value':{'kind':'StringValue','value':'response_time_msec','block':false}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'status'}},{'kind':'Field','name':{'kind':'Name','value':'data'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'resultType'}},{'kind':'Field','name':{'kind':'Name','value':'result'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'metric'}},{'kind':'Field','name':{'kind':'Name','value':'value'}},{'kind':'Field','name':{'kind':'Name','value':'values'}}]}}]}}]}}]}}]} as unknown as DocumentNode<BuildNetworkInventoryPageQuery, BuildNetworkInventoryPageQueryVariables>
export const NodesForMapDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'NodesForMap'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'findAllNodes'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'nodeLabel'}},{'kind':'Field','name':{'kind':'Name','value':'location'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'latitude'}},{'kind':'Field','name':{'kind':'Name','value':'longitude'}},{'kind':'Field','name':{'kind':'Name','value':'location'}}]}}]}}]}}]} as unknown as DocumentNode<NodesForMapQuery, NodesForMapQueryVariables>
export const ListMonitoryPoliciesDocument = {'kind':'Document', 'definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'ListMonitoryPolicies'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'listMonitoryPolicies'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'FragmentSpread','name':{'kind':'Name','value':'MonitoringPolicyParts'}}]}},{'kind':'Field','name':{'kind':'Name','value':'defaultPolicy'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'FragmentSpread','name':{'kind':'Name','value':'MonitoringPolicyParts'}}]}}]}},...MonitoringPolicyPartsFragmentDoc.definitions]} as unknown as DocumentNode<ListMonitoryPoliciesQuery, ListMonitoryPoliciesQueryVariables>
export const CountAlertByPolicyIdDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'CountAlertByPolicyId'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'id'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Long'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'countAlertByPolicyId'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'id'},'value':{'kind':'Variable','name':{'kind':'Name','value':'id'}}}]}]}}]} as unknown as DocumentNode<CountAlertByPolicyIdQuery, CountAlertByPolicyIdQueryVariables>
export const CountAlertByRuleIdDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'CountAlertByRuleId'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'id'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Long'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'countAlertByRuleId'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'id'},'value':{'kind':'Variable','name':{'kind':'Name','value':'id'}}}]}]}}]} as unknown as DocumentNode<CountAlertByRuleIdQuery, CountAlertByRuleIdQueryVariables>
export const ListNodeStatusDocument = {'kind':'Document', 'definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'ListNodeStatus'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'id'}},'type':{'kind':'NamedType','name':{'kind':'Name','value':'Long'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'FragmentSpread','name':{'kind':'Name','value':'NodeByIdParts'}}]}},...NodeByIdPartsFragmentDoc.definitions]} as unknown as DocumentNode<ListNodeStatusQuery, ListNodeStatusQueryVariables>
export const ListNodeEventsDocument = {'kind': 'Document','definitions': [{'kind': 'OperationDefinition','operation': 'query','name': {'kind': 'Name','value': 'ListEvents'},'variableDefinitions': [{'kind': 'VariableDefinition','variable': {'kind': 'Variable','name': {'kind': 'Name','value': 'id'}},'type': {'kind': 'NamedType','name': {'kind': 'Name','value': 'Long'}}}],'selectionSet': {'kind': 'SelectionSet','selections': [{'kind': 'FragmentSpread','name': {'kind': 'Name','value': 'EventsByNodeIdParts'}}]}}, ...EventsByNodeIdPartsFragmentDoc.definitions]} as unknown as DocumentNode<EventsByNodeIdPartsFragment, ListEventsQueryVariables>
export const FindExportersForNodeStatusDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'findExportersForNodeStatus'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'requestCriteria'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'RequestCriteriaInput'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'findExporters'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'requestCriteria'},'value':{'kind':'Variable','name':{'kind':'Name','value':'requestCriteria'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'snmpInterface'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'ifIndex'}},{'kind':'Field','name':{'kind':'Name','value':'ifName'}}]}},{'kind':'Field','name':{'kind':'Name','value':'ipInterface'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'ipAddress'}}]}}]}}]}}]} as unknown as DocumentNode<FindExportersForNodeStatusQuery, FindExportersForNodeStatusQueryVariables>
export const FindLocationsForWelcomeDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'FindLocationsForWelcome'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'findAllLocations'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'location'}}]}}]}}]} as unknown as DocumentNode<FindLocationsForWelcomeQuery, FindLocationsForWelcomeQueryVariables>
export const FindDevicesForWelcomeDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'FindDevicesForWelcome'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'findAllNodes'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'nodeLabel'}},{'kind':'Field','name':{'kind':'Name','value':'createTime'}},{'kind':'Field','name':{'kind':'Name','value':'ipInterfaces'},'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}},{'kind':'Field','name':{'kind':'Name','value':'nodeId'}},{'kind':'Field','name':{'kind':'Name','value':'ipAddress'}},{'kind':'Field','name':{'kind':'Name','value':'snmpPrimary'}}]}}]}}]}}]} as unknown as DocumentNode<FindDevicesForWelcomeQuery, FindDevicesForWelcomeQueryVariables>
export const FindMinionsForWelcomeDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'FindMinionsForWelcome'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'locationId'}},'type':{'kind':'NonNullType','type':{'kind':'NamedType','name':{'kind':'Name','value':'Long'}}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'findMinionsByLocationId'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'locationId'},'value':{'kind':'Variable','name':{'kind':'Name','value':'locationId'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'id'}}]}}]}}]} as unknown as DocumentNode<FindMinionsForWelcomeQuery, FindMinionsForWelcomeQueryVariables>
export const DownloadMinionCertificateForWelcomeDocument = {'kind':'Document','definitions':[{'kind':'OperationDefinition','operation':'query','name':{'kind':'Name','value':'DownloadMinionCertificateForWelcome'},'variableDefinitions':[{'kind':'VariableDefinition','variable':{'kind':'Variable','name':{'kind':'Name','value':'location'}},'type':{'kind':'NamedType','name':{'kind':'Name','value':'Long'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'getMinionCertificate'},'arguments':[{'kind':'Argument','name':{'kind':'Name','value':'locationId'},'value':{'kind':'Variable','name':{'kind':'Name','value':'location'}}}],'selectionSet':{'kind':'SelectionSet','selections':[{'kind':'Field','name':{'kind':'Name','value':'password'}},{'kind':'Field','name':{'kind':'Name','value':'certificate'}}]}}]}}]} as unknown as DocumentNode<DownloadMinionCertificateForWelcomeQuery, DownloadMinionCertificateForWelcomeQueryVariables>
export const AlertsByNodeDocument = { 'kind': 'Document', 'definitions': [{ 'kind': 'OperationDefinition', 'operation': 'query', 'name': { 'kind': 'Name', 'value': 'AlertsByNode' }, 'variableDefinitions': [{ 'kind': 'VariableDefinition', 'variable': { 'kind': 'Variable', 'name': { 'kind': 'Name', 'value': 'page' } }, 'type': { 'kind': 'NonNullType', 'type': { 'kind': 'NamedType', 'name': { 'kind': 'Name', 'value': 'Int' } } } }, { 'kind': 'VariableDefinition', 'variable': { 'kind': 'Variable', 'name': { 'kind': 'Name', 'value': 'pageSize' } }, 'type': { 'kind': 'NamedType', 'name': { 'kind': 'Name', 'value': 'Int' } } }, { 'kind': 'VariableDefinition', 'variable': { 'kind': 'Variable', 'name': { 'kind': 'Name', 'value': 'sortAscending' } }, 'type': { 'kind': 'NonNullType', 'type': { 'kind': 'NamedType', 'name': { 'kind': 'Name', 'value': 'Boolean' } } } }, { 'kind': 'VariableDefinition', 'variable': { 'kind': 'Variable', 'name': { 'kind': 'Name', 'value': 'sortBy' } }, 'type': { 'kind': 'NamedType', 'name': { 'kind': 'Name', 'value': 'String' } } }, { kind: 'VariableDefinition', variable: { kind: 'Variable', name: { kind: 'Name', value: 'nodeId' } }, type: { kind: 'NonNullType', type: { kind: 'NamedType', name: { kind: 'Name', value: 'Long' } } } }], 'selectionSet': { 'kind': 'SelectionSet', 'selections': [{ 'kind': 'FragmentSpread', 'name': { 'kind': 'Name', 'value': 'AlertsByNode' } }] } }, ...AlertsPartsList.definitions] } as unknown as DocumentNode<AlertsByNodeListQuery, AlertsNodeByListVariables>
export const DownloadAlertsByNodeDocument = { 'kind': 'Document', 'definitions': [{ 'kind': 'OperationDefinition', 'operation': 'query', 'name': { 'kind': 'Name', 'value': 'downloadRecentAlertsByNode' }, 'variableDefinitions': [{ 'kind': 'VariableDefinition', 'variable': { 'kind': 'Variable', 'name': { 'kind': 'Name', 'value': 'nodeId' } }, 'type': { 'kind': 'NonNullType', 'type': { 'kind': 'NamedType', 'name': { 'kind': 'Name', 'value': 'Long' } } } }, { 'kind': 'VariableDefinition', 'variable': { 'kind': 'Variable', 'name': { 'kind': 'Name', 'value': 'page' } }, 'type': { 'kind': 'NonNullType', 'type': { 'kind': 'NamedType', 'name': { 'kind': 'Name', 'value': 'Int' } } } }, { 'kind': 'VariableDefinition', 'variable': { 'kind': 'Variable', 'name': { 'kind': 'Name', 'value': 'pageSize' } }, 'type': { 'kind': 'NonNullType', 'type': { 'kind': 'NamedType', 'name': { 'kind': 'Name', 'value': 'Int' } } } }, { kind: 'VariableDefinition', variable: { kind: 'Variable', name: { kind: 'Name', value: 'sortBy' } }, type: { kind: 'NonNullType', type: { kind: 'NamedType', name: { kind: 'Name', value: 'String' } } } }, { kind: 'VariableDefinition', variable: { kind: 'Variable', name: { kind: 'Name', value: 'downloadFormat' } }, type: { kind: 'NonNullType', type: { kind: 'NamedType', name: { kind: 'Name', value: 'DownloadFormat' } } } }, { 'kind': 'VariableDefinition', 'variable': { 'kind': 'Variable', 'name': { 'kind': 'Name', 'value': 'sortAscending' } }, 'type': { 'kind': 'NonNullType', 'type': { 'kind': 'NamedType', 'name': { 'kind': 'Name', 'value': 'Boolean' } } } }], 'selectionSet': { 'kind': 'SelectionSet', 'selections': [{ 'kind': 'Field', 'name': { 'kind': 'Name', 'value': 'downloadRecentAlertsByNode' }, 'arguments': [{ 'kind': 'Argument', 'name': { 'kind': 'Name', 'value': 'nodeId' }, 'value': { 'kind': 'Variable', 'name': { 'kind': 'Name', 'value': 'nodeId' } } }, { kind: 'Argument', name: { kind: 'Name', value: 'pageSize' }, value: { kind: 'Variable', name: { kind: 'Name', value: 'pageSize' } } }, { kind: 'Argument', name: { kind: 'Name', value: 'sortBy' }, value: { kind: 'Variable', name: { kind: 'Name', value: 'sortBy' } } }, { kind: 'Argument', name: { kind: 'Name', value: 'sortAscending' }, value: { kind: 'Variable', name: { kind: 'Name', value: 'sortAscending' } } }, { 'kind': 'Argument', 'name': { 'kind': 'Name', 'value': 'page' }, 'value': { 'kind': 'Variable', 'name': { 'kind': 'Name', 'value': 'page' } } }, { 'kind': 'Argument', 'name': { 'kind': 'Name', 'value': 'downloadFormat' }, 'value': { 'kind': 'Variable', 'name': { 'kind': 'Name', 'value': 'downloadFormat' } } }], 'selectionSet': { 'kind': 'SelectionSet', 'selections': [{ 'kind': 'Field', 'name': { 'kind': 'Name', 'value': 'alertsBytes' } }] } }] } }] } as unknown as DocumentNode<DownloadAlertsByNodeAndSearchTermQuery, AlertsNodeByListVariablesExtended>
export const DownloadEventsDocument = {'kind': 'Document','definitions': [{'kind': 'OperationDefinition','operation': 'query','name': {'kind': 'Name','value': 'downloadEvents'},'variableDefinitions': [{'kind': 'VariableDefinition','variable': {'kind': 'Variable','name': {'kind': 'Name','value': 'nodeId'}},'type': {'kind': 'NonNullType','type': {'kind': 'NamedType','name': {'kind': 'Name','value': 'Long'}}}},{'kind': 'VariableDefinition','variable': {'kind': 'Variable','name': {'kind': 'Name','value': 'searchTerm'}},'type': {'kind': 'NonNullType','type': {'kind': 'NamedType','name': {'kind': 'Name','value': 'String'}}}},{'kind': 'VariableDefinition','variable': {'kind': 'Variable','name': {'kind': 'Name','value': 'downloadFormat'}},'type': {'kind': 'NonNullType','type': {'kind': 'NamedType','name': {'kind': 'Name','value': 'DownloadFormat'}}}}],'selectionSet': {'kind': 'SelectionSet','selections': [{'kind': 'Field','name': {'kind': 'Name','value': 'downloadEvents'},'arguments': [{'kind': 'Argument','name': {'kind': 'Name','value': 'nodeId'},'value': {'kind': 'Variable','name': {'kind': 'Name','value': 'nodeId'}}},{'kind': 'Argument','name': {'kind': 'Name','value': 'searchTerm'},'value': {'kind': 'Variable','name': {'kind': 'Name','value': 'searchTerm'}}},{'kind': 'Argument','name': {'kind': 'Name','value': 'downloadFormat'},'value': {'kind': 'Variable','name': {'kind': 'Name','value': 'downloadFormat'}}}],'selectionSet': {'kind': 'SelectionSet','selections': [{'kind': 'Field','name': {'kind': 'Name','value': 'searchEventsBytes'}}]}}]}}]} as unknown as DocumentNode<DownloadEventsByNodeAndSearchTermQuery, DownloadCsvVariables>
/* eslint-enable @stylistic/comma-spacing, @stylistic/key-spacing */
