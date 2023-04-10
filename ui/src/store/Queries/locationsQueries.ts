import { useQuery } from 'villus'
import { defineStore } from 'pinia'
import { LocationsListDocument, Location } from '@/types/graphql'

export const useLocationsQueries = defineStore('locationsQueries', () => {
  const locations = ref<Location[]>([])

  const { data, execute } = useQuery({
    query: LocationsListDocument,
    fetchOnMount: false,
    cachePolicy: 'network-only'
  })

  watchEffect(() => {
    locations.value = data.value?.findAllLocations || []
  })

  return {
    fetchLocations: execute,
    locations: computed(() => locations.value)
  }
})
