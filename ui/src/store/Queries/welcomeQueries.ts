import { defineStore } from 'pinia'
import { useQuery } from 'villus'
import {
  FindLocationsForWelcomeDocument,
  FindMinionsForWelcomeDocument,
  DownloadMinionCertificateForWelcomeDocument
} from '@/types/graphql'

export const useWelcomeQueries = defineStore('welcomeQueries', () => {
  const isShowOnboardingState = ref(false)
  const minionCert = ref()
  const variables = reactive({ locationId: undefined })
  const defaultLocationName = 'default'

  const { execute: checkSetupState, onData: onLocationsCallComplete } = useQuery({
    query: FindLocationsForWelcomeDocument,
    fetchOnMount: false,
    cachePolicy: 'network-only'
  })

  const { onData: onMinionsCallComplete } = useQuery({
    query: FindMinionsForWelcomeDocument,
    fetchOnMount: false,
    cachePolicy: 'network-only',
    variables: variables
  })

  const { onData: onDownloadCertCallComplete, execute: downloadMinionCertificate } = useQuery({
    query: DownloadMinionCertificateForWelcomeDocument,
    fetchOnMount: false,
    cachePolicy: 'network-only',
    variables: {
      location: defaultLocationName
    }
  })

  // check to see we have only 1 location (default)
  onLocationsCallComplete((data) => {
    if (data.findAllLocations?.length !== 1) return
    if (data.findAllLocations[0].location?.toLowerCase() !== 'default') return
    variables.locationId = data.findAllLocations[0].id
  })

  // check that there are no configured minions
  onMinionsCallComplete((data) => {
    if (data.findMinionsByLocationId?.length === 0) {
      isShowOnboardingState.value = true
    }
  })

  onDownloadCertCallComplete((data) => minionCert.value = data.getMinionCertificate)

  return {
    downloadMinionCertificate,
    checkSetupState,
    isShowOnboardingState,
    minionCert
  }
})
