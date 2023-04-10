import { defineStore } from 'pinia'
import { useLocationsQueries } from '../Queries/locationsQueries'

export const useLocationsStore = defineStore('locationsStore', () => {
  const locationsList = ref()

  const locationsQueries = useLocationsQueries()

  const fetchLocations = async () => {
    const { data, isFetching } = await locationsQueries.fetchLocations()

    if (!isFetching.value) locationsList.value = data.value?.findAllLocations || []
  }

  const searchLocation = async (searchTerm = '') => {
    const { data, isFetching } = await locationsQueries.searchLocation(searchTerm)

    if (!isFetching.value) locationsList.value = data.value?.searchLocation || []
  }

  return {
    locationsList,
    fetchLocations,
    searchLocation
  }
})
