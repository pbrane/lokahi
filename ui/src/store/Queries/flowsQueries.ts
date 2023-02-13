import { defineStore } from 'pinia'
import { useQuery } from 'villus'
import { Query, FlowSummary } from '@/types/graphql'

export const useFlowQueries = defineStore('flowQueries', () => {
  const variables = ref({})

  const GetFlowSummary = `
    {
      getFlowSummary{
        numFlows
      }
    }
  `

  const { data } = useQuery({
    query: GetFlowSummary,
    variables,
    cachePolicy: 'network-only'
  })

  const flowSummary = computed(() => (data.value?.getFlowSummary as FlowSummary))

  return {
    flowSummary
  }
})
