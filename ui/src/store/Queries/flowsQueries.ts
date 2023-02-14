import { defineStore } from 'pinia'
import { useQuery } from 'villus'
import {Query, FlowSummary, TrafficSummary} from '@/types/graphql'

export const useFlowQueries = defineStore('flowQueries', () => {
  const variables = ref({})
  const GetFlowSummary = `
    {
      getFlowSummary{
        numFlows
      }
    }
  `
  const { data: flowSummaryData } = useQuery({
    query: GetFlowSummary,
    variables,
    cachePolicy: 'network-only'
  })
  const flowSummary = computed(() => (flowSummaryData.value?.getFlowSummary as FlowSummary))


  const GetTopNHostSummaries = `
    {
      getTopNHostSummaries{
        bytesIn
        bytesOut
        label
      }
    }
  `
  const { data: topHostsData } = useQuery({
    query: GetTopNHostSummaries,
    variables,
    cachePolicy: 'network-only'
  })
  const topHosts = computed(() => (topHostsData.value?.getTopNHostSummaries as TrafficSummary[]))


  return {
    flowSummary,
    topHosts
  }
})
