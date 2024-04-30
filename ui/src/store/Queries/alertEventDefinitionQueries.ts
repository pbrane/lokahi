import { defineStore } from 'pinia'
import { useQuery } from 'villus'
import {
  EventDefsByVendorRequest,
  EventType,
  ListAlertEventDefinitionsByVendorDocument,
  ListAlertEventDefinitionsDocument
} from '@/types/graphql'

export const useAlertEventDefinitionQueries = defineStore('alertEventDefinitionQueries', () => {
  const listAlertEventDefinitions = async (eventType: EventType) => {
    const { data, execute } = useQuery({
      query: ListAlertEventDefinitionsDocument,
      variables: {
        eventType: eventType
      },
      fetchOnMount: false,
      cachePolicy: 'network-only'
    })

    await execute()

    return data
  }

  const listAlertEventDefinitionsByVendor = async (request: EventDefsByVendorRequest) => {
    const { execute, data } = useQuery({
      query: ListAlertEventDefinitionsByVendorDocument,
      variables: request,
      cachePolicy: 'network-only'
    })
    await execute()
    return data.value?.alertEventDefsByVendor?.alertEventDefinitionList
  }

  return {
    listAlertEventDefinitions,
    listAlertEventDefinitionsByVendor
  }
})
