import { BGColors } from '@/components/Appliances/utils'
import { MinionDto } from './graphql'

interface ExtendedMinionDTO extends MinionDto {
  icmp_latency: number
  snmp_uptime: number
}

interface ExtendedMinionDTOWithBGColors extends ExtendedMinionDTO, BGColors {}