import { defineStore } from 'pinia'
import { useQuery } from 'villus'
import {FlowSummary, TrafficSummary, FlowingPoint} from '@/types/graphql'
import _ from 'underscore'

export const useFlowQueries = defineStore('flowQueries', () => {
  const timeWindow = ref({hours: 1})

  const hostFilter = ref(null)
  const applicationFilter = ref(null)

  const variables = computed(() => ({
    hours: timeWindow.value.hours,
    hostFilter: hostFilter.value,
    applicationFilter: applicationFilter.value,
  }))

  const { data: flowSummaryData } = useQuery({
    query: `query GetFlowSummary($hours: Long, $hostFilter: String, $applicationFilter: String) {
      getFlowSummary(hours: $hours, hostFilter: $hostFilter, applicationFilter: $applicationFilter) {
        numFlows
      }
    }`,
    variables,
    cachePolicy: 'network-only'
  })
  const flowSummary = computed(() => (flowSummaryData.value?.getFlowSummary as FlowSummary))


  const { data: topHostSummeriesData } = useQuery({
    query: `query GetTopNHostSummaries($hours: Long, $hostFilter: String, $applicationFilter: String) {
      getTopNHostSummaries(hours: $hours, hostFilter: $hostFilter, applicationFilter: $applicationFilter) {
        bytesIn
        bytesOut
        label
      }
    }`,
    variables,
    cachePolicy: 'network-only'
  })
  const topHostSummaries = computed(() => (
    _.sortBy(topHostSummeriesData.value?.getTopNHostSummaries as TrafficSummary[],
      function(summary){return -(summary.bytesIn+summary.bytesOut)})
  ))

  const { data: topHostSeriesData } = useQuery({
    query: `query GetTopNHostSeries($hours: Long, $hostFilter: String, $applicationFilter: String) {
      getTopNHostSeries(hours: $hours, hostFilter: $hostFilter, applicationFilter: $applicationFilter) {
        timestamp
        direction
        label
        value
      }
    }`,
    variables,
    cachePolicy: 'network-only'
  })
  const topHostSeries = computed(() => (topHostSeriesData.value?.getTopNHostSeries as FlowingPoint[]))


  const { data: topApplicationSummeriesData } = useQuery({
    query: `query GetTopNApplicationSummaries($hours: Long, $hostFilter: String, $applicationFilter: String) {
      getTopNApplicationSummaries(hours: $hours, hostFilter: $hostFilter, applicationFilter: $applicationFilter) {
        bytesIn
        bytesOut
        label
      }
    }`,
    variables,
    cachePolicy: 'network-only'
  })
  const topApplicationSummaries = computed(() => (
    _.sortBy(topApplicationSummeriesData.value?.getTopNApplicationSummaries as TrafficSummary[],
      function(summary){return -(summary.bytesIn+summary.bytesOut)})
  ))

  const { data: topApplicationSeriesData } = useQuery({
    query: `query GetTopNApplicationSeries($hours: Long, $hostFilter: String, $applicationFilter: String) {
      getTopNApplicationSeries(hours: $hours, hostFilter: $hostFilter, applicationFilter: $applicationFilter) {
        timestamp
        direction
        label
        value
      }
    }`,
    variables,
    cachePolicy: 'network-only'
  })
  const topApplicationSeries = computed(() => (topApplicationSeriesData.value?.getTopNApplicationSeries as FlowingPoint[]))

  
  const { data: topConversationSummeriesData } = useQuery({
    query: `query GetTopNConversationSummaries($hours: Long, $hostFilter: String, $applicationFilter: String) {
      getTopNConversationSummaries(hours: $hours, hostFilter: $hostFilter, applicationFilter: $applicationFilter) {
        bytesIn
        bytesOut
        label
      }
    }`,
    variables,
    cachePolicy: 'network-only'
  })
  const topConversationSummaries = computed(() => (
    _.sortBy(topConversationSummeriesData.value?.getTopNConversationSummaries as TrafficSummary[],
      function(summary){return -(summary.bytesIn+summary.bytesOut)})
  ))

  const { data: topConversationSeriesData } = useQuery({
    query: `query GetTopNConversationSeries($hours: Long, $hostFilter: String, $applicationFilter: String) {
      getTopNConversationSeries(hours: $hours, hostFilter: $hostFilter, applicationFilter: $applicationFilter) {
        timestamp
        direction
        label
        value
      }
    }`,
    variables,
    cachePolicy: 'network-only'
  })
  const topConversationSeries = computed(() => (topConversationSeriesData.value?.getTopNConversationSeries as FlowingPoint[]))

  const setTimeWindow = (hours: number) => {
    timeWindow.value.hours = hours;
  }

  const setFilters = (host: string, application: string) => {
    hostFilter.value = host;
    applicationFilter.value = application;
  }

  return {
    setTimeWindow,
    setFilters,
    flowSummary,
    topHostSummaries,
    topHostSeries,
    topApplicationSummaries,
    topApplicationSeries,
    topConversationSummaries,
    topConversationSeries,
  }
})
