import { ServiceInventoryItem } from '@/types'

export const tabContentOfAllServices = <ServiceInventoryItem[]>([
  {
    id: 1,
    type: 'Type 1',
    service: 'Service 1',
    node: 12333,
    description: 'Description 1',
    reachability: '98%',
    latency: '10ms',
    uptime: '12h'
  },
  {
    id: 2,
    type: 'Type 2',
    service: 'Service 2',
    node: 232,
    description: 'Description 2',
    reachability: '42%',
    latency: 'N/A',
    uptime: '6h'
  }
])

export const tabContentOfWindowsServices = <ServiceInventoryItem[]>([
  {
    id: 1,
    type: 'Type 43',
    service: 'Service 15',
    node: 127,
    description: 'Description 1',
    reachability: '98%',
    latency: '10ms',
    uptime: '12h'
  },
  {
    id: 2,
    type: 'Type 5',
    service: 'Service 90',
    node: 128,
    description: 'Description no signal',
    reachability: '82%%',
    latency: 'N/A',
    uptime: '213123h'
  }
])

export const tabContentOfHostedServices = <ServiceInventoryItem[]>([
  {
    id: 1,
    type: 'Type 23423',
    service: 'Service 3423',
    node: 12333,
    description: 'Description 1sdfsfswe',
    reachability: '5%',
    latency: '104432ms',
    uptime: '12h'
  },
  {
    id: 2,
    type: 'Type 1212',
    service: 'Service 56',
    node: 232,
    description: 'Description have have',
    reachability: '4%',
    latency: '34.23423',
    uptime: '3423h'
  }
])

export const tabContentOfStandAloneServices = <ServiceInventoryItem[]>([
  {
    id: 1,
    type: 'Type 65',
    service: 'Service f443fd',
    node: 12333,
    description: 'Description more than 400',
    reachability: '9%',
    latency: '104ms',
    uptime: '12h'
  },
  {
    id: 2,
    type: 'Type 000',
    service: 'Service llfe',
    node: 23888,
    description: 'Description 2',
    reachability: '02%',
    latency: '90393422',
    uptime: '699934h'
  }
])
