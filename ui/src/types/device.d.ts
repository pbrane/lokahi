import { BGColors } from '@/components/Appliances/utils'
import { DeviceDto } from './graphql'

interface ExtendedDeviceDTO extends DeviceDto {
  icmp_latency: number
  snmp_uptime: number
  status: string
}

interface ExtendedDeviceDTOWithBGColors extends ExtendedDeviceDTO, BGColors {}