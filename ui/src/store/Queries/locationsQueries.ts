import { useQuery } from 'villus'
import { defineStore } from 'pinia'
import { LocationsListDocument, SearchLocationDocument } from '@/types/graphql'

export const useLocationsQueries = defineStore('locationsQueries', () => {
  const fetchLocations = () =>
    useQuery({
      query: LocationsListDocument,
      fetchOnMount: false,
      cachePolicy: 'network-only'
    })

  const searchLocation = (searchTerm = '') =>
    useQuery({
      query: SearchLocationDocument,
      variables: {
        searchTerm
      },
      fetchOnMount: false,
      cachePolicy: 'network-only'
    })

  return {
    fetchLocations,
    searchLocation
  }
})
