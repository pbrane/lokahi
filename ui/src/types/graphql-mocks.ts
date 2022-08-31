import { TypedDocumentNode as DocumentNode } from '@graphql-typed-document-node/core';
export type Maybe<T> = T | null;
export type InputMaybe<T> = Maybe<T>;
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
};

export type Device = {
  __typename?: 'Device';
  createTime: Scalars['String'];
  icmp_latency: Scalars['String'];
  id: Scalars['String'];
  label: Scalars['String'];
  snmp_uptime: Scalars['String'];
  status: Scalars['String'];
};

export type DeviceInput = {
  createTime?: InputMaybe<Scalars['String']>;
  domainName?: InputMaybe<Scalars['String']>;
  foreignId?: InputMaybe<Scalars['String']>;
  foreignSource?: InputMaybe<Scalars['String']>;
  id?: InputMaybe<Scalars['Int']>;
  label?: InputMaybe<Scalars['String']>;
  labelSource?: InputMaybe<Scalars['String']>;
  lastEgressFlow?: InputMaybe<Scalars['String']>;
  lastIngressFlow?: InputMaybe<Scalars['String']>;
  lastPoll?: InputMaybe<Scalars['String']>;
  location?: InputMaybe<LocationDtoInput>;
  netBiosName?: InputMaybe<Scalars['String']>;
  operatingSystem?: InputMaybe<Scalars['String']>;
  parentId?: InputMaybe<Scalars['Int']>;
  sysContact?: InputMaybe<Scalars['String']>;
  sysDescription?: InputMaybe<Scalars['String']>;
  sysLocation?: InputMaybe<Scalars['String']>;
  sysName?: InputMaybe<Scalars['String']>;
  sysOid?: InputMaybe<Scalars['String']>;
  type?: InputMaybe<Scalars['String']>;
};

export type ListDevices = {
  __typename?: 'ListDevices';
  devices: Array<Device>;
};

export type ListMinions = {
  __typename?: 'ListMinions';
  minions: Array<Minion>;
};

export type LocationDtoInput = {
  geolocation?: InputMaybe<Scalars['String']>;
  latitude?: InputMaybe<Scalars['Float']>;
  locationName?: InputMaybe<Scalars['String']>;
  longitude?: InputMaybe<Scalars['Float']>;
  monitoringArea?: InputMaybe<Scalars['String']>;
  priority?: InputMaybe<Scalars['Int']>;
  tags?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
};

export type Minion = {
  __typename?: 'Minion';
  id: Scalars['String'];
  label: Scalars['String'];
  lastUpdated: Scalars['String'];
  location: Scalars['String'];
  status: Scalars['String'];
};

export type Mutation = {
  __typename?: 'Mutation';
  addDevice?: Maybe<Scalars['String']>;
  saveRoutingKey?: Maybe<Scalars['String']>;
};


export type MutationAddDeviceArgs = {
  device: DeviceInput;
};


export type MutationSaveRoutingKeyArgs = {
  key: Scalars['String'];
};

export type Query = {
  __typename?: 'Query';
  device: Device;
  listDevices: ListDevices;
  listMinions: ListMinions;
  minion: Minion;
};

export type AddDeviceMutationVariables = Exact<{
  device: DeviceInput;
}>;


export type AddDeviceMutation = { __typename?: 'Mutation', addDevice?: string | null };

export type SaveRoutingKeyMutationVariables = Exact<{
  key: Scalars['String'];
}>;


export type SaveRoutingKeyMutation = { __typename?: 'Mutation', saveRoutingKey?: string | null };

export type ListDevicesQueryVariables = Exact<{ [key: string]: never; }>;


export type ListDevicesQuery = { __typename?: 'Query', listDevices: { __typename?: 'ListDevices', devices: Array<{ __typename?: 'Device', id: string, label: string, status: string, icmp_latency: string, snmp_uptime: string, createTime: string }> } };

export type ListMinionsQueryVariables = Exact<{ [key: string]: never; }>;


export type ListMinionsQuery = { __typename?: 'Query', listMinions: { __typename?: 'ListMinions', minions: Array<{ __typename?: 'Minion', id: string, label: string, status: string, location: string, lastUpdated: string }> } };


export const AddDeviceDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"mutation","name":{"kind":"Name","value":"AddDevice"},"variableDefinitions":[{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"device"}},"type":{"kind":"NonNullType","type":{"kind":"NamedType","name":{"kind":"Name","value":"DeviceInput"}}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"addDevice"},"arguments":[{"kind":"Argument","name":{"kind":"Name","value":"device"},"value":{"kind":"Variable","name":{"kind":"Name","value":"device"}}}]}]}}]} as unknown as DocumentNode<AddDeviceMutation, AddDeviceMutationVariables>;
export const SaveRoutingKeyDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"mutation","name":{"kind":"Name","value":"SaveRoutingKey"},"variableDefinitions":[{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"key"}},"type":{"kind":"NonNullType","type":{"kind":"NamedType","name":{"kind":"Name","value":"String"}}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"saveRoutingKey"},"arguments":[{"kind":"Argument","name":{"kind":"Name","value":"key"},"value":{"kind":"Variable","name":{"kind":"Name","value":"key"}}}]}]}}]} as unknown as DocumentNode<SaveRoutingKeyMutation, SaveRoutingKeyMutationVariables>;
export const ListDevicesDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"query","name":{"kind":"Name","value":"ListDevices"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"listDevices"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"devices"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"id"}},{"kind":"Field","name":{"kind":"Name","value":"label"}},{"kind":"Field","name":{"kind":"Name","value":"status"}},{"kind":"Field","name":{"kind":"Name","value":"icmp_latency"}},{"kind":"Field","name":{"kind":"Name","value":"snmp_uptime"}},{"kind":"Field","name":{"kind":"Name","value":"createTime"}}]}}]}}]}}]} as unknown as DocumentNode<ListDevicesQuery, ListDevicesQueryVariables>;
export const ListMinionsDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"query","name":{"kind":"Name","value":"ListMinions"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"listMinions"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"minions"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"id"}},{"kind":"Field","name":{"kind":"Name","value":"label"}},{"kind":"Field","name":{"kind":"Name","value":"status"}},{"kind":"Field","name":{"kind":"Name","value":"location"}},{"kind":"Field","name":{"kind":"Name","value":"lastUpdated"}}]}}]}}]}}]} as unknown as DocumentNode<ListMinionsQuery, ListMinionsQueryVariables>;
