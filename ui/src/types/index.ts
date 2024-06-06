import { SORT } from '@featherds/table'
import { PointerAlignment, PopoverPlacement } from '@featherds/popover'

export * from './flows.d'
export * from './inventory.d'
export * from './locations.d'
export declare type fncVoid = () => void
export declare type fncArgVoid = (...args: any[]) => void

export interface KeyValueStringType {
  [key: string]: string
}

export interface SnackbarProps {
  msg: string
  center?: boolean
  error?: boolean
}

export interface IdLabelProps {
  id: string
  label: string
}

export interface FeatherSortObject {
  property: string
  value: SORT | any
}

export interface FeatherRadioObject {
  name: string
  value: any
}

export enum TimeUnit {
  Secs,
  MSecs
}

export interface IIcon {
  image: any
  title?: string
  tooltip?: string
  size?: number // rem
  cursorHover?: boolean
}

export interface IInputButtonPopover {
  placement?: PopoverPlacement
  alignment?: PointerAlignment
  icon: IIcon
  label?: string
  handler: fncVoid
}

export interface ChartData {
  labels?: any[]
  datasets: any[]
}

export const enum Monitor {
  ICMP = 'ICMP',
  SNMP = 'SNMP',
  ECHO = 'ECHO',
  AZURE = 'AZURE'
}

export interface TagSelectItem {
  name: string
  id?: string
  _text?: string
}

export interface ContextMenuItem {
  label: string
  handler: fncVoid
}

export interface IButtonTextIcon {
  label: string | undefined
  type?: string
}

export const AZURE_SCAN = 'AZURE_SCAN'

export type DeepPartial<T> = T extends object ? {
  [P in keyof T]?: DeepPartial<T[P]>
} : T

export enum Status {
  UP = 'UP',
  DOWN = 'DOWN'
}

export enum CreateEditMode {
  None = 0,
  Create = 1,
  Edit = 2
}

export enum Comparators {
  EQ = 'EQ',
  NE = 'NE',
  GT = 'GT',
  GTE = 'GTE',
  LT = 'LT',
  LTE = 'LTE'
}
