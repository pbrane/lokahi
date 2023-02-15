import { defineStore } from 'pinia'
import { useQuery } from 'villus'
import {Query, FlowSummary, TrafficSummary} from '@/types/graphql'

export const useFlowQueries = defineStore('flowQueries', () => {
  const variables = ref({hours:1})
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


  const GetTopNHostSummaries = computed(() => {
    return `{
      getTopNHostSummaries(hours: ${variables.value.hours}){
        bytesIn
        bytesOut
        label
      }
    }`
  })

  const { data: topHostsData } = useQuery({
    query: GetTopNHostSummaries,
    cachePolicy: 'network-only'
  })
  const topHosts = computed(() => (topHostsData.value?.getTopNHostSummaries as TrafficSummary[]))


  const GetTopNApplicationSummaries = computed(() => {
    return `{
      getTopNApplicationSummaries(hours: ${variables.value.hours}){
        bytesIn
        bytesOut
        label
      }
    }`
  })
  const { data: topApplicationsData } = useQuery({
    query: GetTopNApplicationSummaries,
    cachePolicy: 'network-only'
  })
  const topApplications = computed(() => (topApplicationsData.value?.getTopNApplicationSummaries as TrafficSummary[]))

  const GetTopNConversationSummaries = computed(() => {
    return `{
      getTopNConversationSummaries(hours: ${variables.value.hours}){
        bytesIn
        bytesOut
        label
      }
    }`
  })
  const { data: topConversationsData } = useQuery({
    query: GetTopNConversationSummaries,
    cachePolicy: 'network-only'
  })
  const topConversations = computed(() => (topConversationsData.value?.getTopNConversationSummaries as TrafficSummary[]))

  const setTimeWindow = (hours: number) => {
    variables.value = { hours }
  }

  return {
    setTimeWindow,
    flowSummary,
    topHosts,
    topApplications,
    topConversations
  }
})
