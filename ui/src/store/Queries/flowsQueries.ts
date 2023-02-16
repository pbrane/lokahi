import { defineStore } from 'pinia'
import { useQuery } from 'villus'
import {FlowSummary, TrafficSummary, FlowingPoint} from '@/types/graphql'
import _ from 'underscore'

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
  const topHostSummaries = computed(() => (
    _.sortBy(topHostSummeriesData.value?.getTopNHostSummaries as TrafficSummary[],
      function(summary){return -(summary.bytesIn+summary.bytesOut)})
  ))

  const GetTopNHostSeries = computed(() => {
    return `{
      getTopNHostSeries(hours: ${variables.value.hours}){
        timestamp
        direction
        label
        value
      }
    }`
  })
  const { data: topHostSeriesData } = useQuery({
    query: GetTopNHostSeries,
    cachePolicy: 'network-only'
  })
  const topHostSeries = computed(() => (topHostSeriesData.value?.getTopNHostSeries as FlowingPoint[]))


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
  const topApplicationSummaries = computed(() => (
    _.sortBy(topApplicationSummeriesData.value?.getTopNApplicationSummaries as TrafficSummary[],
      function(summary){return -(summary.bytesIn+summary.bytesOut)})
  ))

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
  const topApplicationSeries = computed(() => (topApplicationSeriesData.value?.getTopNApplicationSeries as FlowingPoint[]))

  
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
  const topConversationSummaries = computed(() => (
    _.sortBy(topConversationSummeriesData.value?.getTopNConversationSummaries as TrafficSummary[],
      function(summary){return -(summary.bytesIn+summary.bytesOut)})
  ))

  const GetTopNConversationSeries = computed(() => {
    return `{
      getTopNConversationSeries(hours: ${variables.value.hours}){
        timestamp
        direction
        label
        value
      }
    }`
  })
  const { data: topConversationSeriesData } = useQuery({
    query: GetTopNConversationSeries,
    cachePolicy: 'network-only'
  })
  const topConversationSeries = computed(() => (topConversationSeriesData.value?.getTopNConversationSeries as FlowingPoint[]))

  const setTimeWindow = (hours: number) => {
    variables.value = { hours }
  }

  return {
    setTimeWindow,
    flowSummary,
    topHostSummaries,
    topHostSeries,
    topApplicationSummaries,
    topApplicationSeries,
    topConversationSummaries,
    topConversationSeries,
  }
})
