import { defineStore } from 'pinia'
import { useLocationsQueries } from '../Queries/locationsQueries'

export const useLocationsStore = defineStore('locationsStore', () => {
  const locationsList = ref()

  const locationsQueries = useLocationsQueries()

  const fetchLocations = async () => {
    const { data } = await locationsQueries.fetchLocations()

    locationsList.value = data.value?.findAllLocations || []
  }

  const searchLocation = async (searchTerm = '') => {
    const { data } = await locationsQueries.searchLocation(searchTerm)

    locationsList.value = data.value?.searchLocation || []
  }

  return {
    locationsList,
    fetchLocations,
    searchLocation
  }
})
