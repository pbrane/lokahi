import { InventoryNode, MonitoredStates, RawMetric } from '@/types'
import { BadgeTypes } from '../Common/commonTypes'
import { useInventoryStore } from '@/store/Views/inventoryStore'

export const isMonitored = (node: InventoryNode): node is InventoryNode => {
  return node.type === MonitoredStates.MONITORED
}

export const metricsAsTextBadges = (metrics?: RawMetric, nodeId?: string) => {

  const inventoryStore = useInventoryStore()
  const nodeStatus = computed(() => inventoryStore.nodeStatusMap)

  if (!nodeId) {
    return []
  }
  const badges = []
  const label = nodeStatus.value.get(nodeId)

  if (label?.trim().toLowerCase() === 'up') {
    badges.push({ type: BadgeTypes.success, label: metrics?.value?.[1] + 'ms' },
      { type: BadgeTypes.success,  label})
  } else {
    badges.push({ type: BadgeTypes.error,  label})
  }

  return badges
}
