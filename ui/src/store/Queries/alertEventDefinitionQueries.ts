import { defineStore } from 'pinia'
import { useQuery } from 'villus'
import {
  AlertEventDefinition,
  EventType,
  ListEventDefinitionsDocument,
  ListEventDefinitionsQuery
} from "@/types/graphql";

export const useAlertEventDefinitionQueries = defineStore('alertEventDefinitionQueries', () => {
  const listEventDefinitionsData = ref(<AlertEventDefinition[]>[])

  const listEventDefinitions = async (eventType: EventType) => {
    const { data, execute } = useQuery({
      query: ListEventDefinitionsDocument,
      variables: {
        eventType: eventType
      },
      fetchOnMount: false,
      cachePolicy: 'network-only'
    })

    await execute()

    listEventDefinitionsData.value = data.value?.listEventDefinitions ?? []
  }

  return {
    listEventDefinitions,
    listEventDefinitionsData
  }
})
