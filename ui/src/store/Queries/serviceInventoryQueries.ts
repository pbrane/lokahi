import { ListGetAllMonitoredEntityStatesDocument, ServiceMetricsDocument, ServiceMetricsVariable } from '@/types/graphql'
import { defineStore } from 'pinia'
import { useQuery } from 'villus'

export const useServiceInventoryQueries = defineStore('serviceInventoryQueries', () => {
  const getMonitoredEntityStatesList = async () => {
    const { data, execute } = useQuery({
      query: ListGetAllMonitoredEntityStatesDocument,
      cachePolicy: 'network-only'
    })

    await execute()

    return data.value?.listAllSimpleMonitors
  }

  const getServiceMetric = async (variables: ServiceMetricsVariable) => {
    const { data, execute } = useQuery({
      query: ServiceMetricsDocument,
      variables: variables,
      cachePolicy: 'network-only'
    })

    await execute()

    return data.value?.metric
  }

  return {
    getMonitoredEntityStatesList,
    getServiceMetric
  }
})
