import { defineStore } from 'pinia'
import { useQuery } from 'villus'
import {
  AlertEventDefinition,
  EventType,
  ListAlertEventDefinitionsDocument,
  ListAlertEventDefinitionsQuery
} from "@/types/graphql";

export const useAlertEventDefinitionQueries = defineStore('alertEventDefinitionQueries', () => {
  const listAlertEventDefinitionsData = ref(<AlertEventDefinition[]>[])

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

    listAlertEventDefinitionsData.value = data.value?.listAlertEventDefinitions ?? []
  }

  return {
    listAlertEventDefinitions,
    listAlertEventDefinitionsData
  }
})
