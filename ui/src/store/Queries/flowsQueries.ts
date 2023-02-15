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


  const GetTopNApplicationSummaries = `
    {
      getTopNApplicationSummaries{
        bytesIn
        bytesOut
        label
      }
    }
  `
  const { data: topApplicationsData } = useQuery({
    query: GetTopNApplicationSummaries,
    variables,
    cachePolicy: 'network-only'
  })
  const topApplications = computed(() => (topApplicationsData.value?.getTopNApplicationSummaries as TrafficSummary[]))

  const GetTopNConversationSummaries = `
    {
      getTopNConversationSummaries{
        bytesIn
        bytesOut
        label
      }
    }
  `
  const { data: topConversationsData } = useQuery({
    query: GetTopNConversationSummaries,
    variables,
    cachePolicy: 'network-only'
  })
  const topConversations = computed(() => (topConversationsData.value?.getTopNConversationSummaries as TrafficSummary[]))

  return {
    flowSummary,
    topHosts,
    topApplications,
    topConversations
  }
})
