import { defineStore } from 'pinia'
import { useQuery } from 'villus'
import {Query, FlowSummary, TrafficSummary, FlowingPoint} from '@/types/graphql'

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

  const { data: topHostSummeriesData } = useQuery({
    query: GetTopNHostSummaries,
    cachePolicy: 'network-only'
  })
  const topHostSummaries = computed(() => (topHostSummeriesData.value?.getTopNHostSummaries as TrafficSummary[]))


  const GetTopNApplicationSummaries = computed(() => {
    return `{
      getTopNApplicationSummaries(hours: ${variables.value.hours}){
        bytesIn
        bytesOut
        label
      }
    }`
  })
  const { data: topApplicationSummeriesData } = useQuery({
    query: GetTopNApplicationSummaries,
    cachePolicy: 'network-only'
  })
  const topApplicationSummaries = computed(() => (topApplicationSummeriesData.value?.getTopNApplicationSummaries as TrafficSummary[]))


  const GetTopNApplicationSeries = computed(() => {
    return `{
      getTopNApplicationSeries(hours: ${variables.value.hours}){
        timestamp
        direction
        label
        value
      }
    }`
  })
  const { data: topApplicationSeriesData } = useQuery({
    query: GetTopNApplicationSeries,
    cachePolicy: 'network-only'
  })
  const topApplicationSeries = computed(() => (topApplicationSeriesData.value?.getTopNApplicationSummaries as FlowingPoint[]))
  
  
  const GetTopNConversationSummaries = computed(() => {
    return `{
      getTopNConversationSummaries(hours: ${variables.value.hours}){
        bytesIn
        bytesOut
        label
      }
    }`
  })
  const { data: topConversationSummeriesData } = useQuery({
    query: GetTopNConversationSummaries,
    cachePolicy: 'network-only'
  })
  const topConversationSummaries = computed(() => (topConversationSummeriesData.value?.getTopNConversationSummaries as TrafficSummary[]))

  const setTimeWindow = (hours: number) => {
    variables.value = { hours }
  }

  return {
    setTimeWindow,
    flowSummary,
    topHostSummaries,
    topApplicationSummaries,
    topConversationSummaries,
    topApplicationSeries
  }
})
