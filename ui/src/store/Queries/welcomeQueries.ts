import { defineStore } from 'pinia'
import { useQuery } from 'villus'
import { fncArgVoid } from '@/types'
import {
  FindLocationsForWelcomeDocument,
  FindMinionsForWelcomeDocument,
  DownloadMinionCertificateForWelcomeDocument
} from '@/types/graphql'

export const useWelcomeQueries = defineStore('welcomeQueries', () => {
  const isShowOnboardingState = ref(false)
  const minionCert = ref()
  const variables = reactive({ locationId: undefined })
  const certVariables = reactive({ location: undefined })
  const defaultLocationName = 'default'

  let promiseResolve: fncArgVoid
  let promiseReject: fncArgVoid

  // promise used to await completion of both loc / minions calls
  const checkSetupState = async () => {
    const promise = new Promise((resolve, reject) => {
      promiseResolve = resolve
      promiseReject = reject
    })
    getLocations()
    return promise
  }

  // get all locations
  const {
    execute: getLocations,
    onData: onLocationsCallComplete,
    onError: onLocationsCallError
  } = useQuery({
    query: FindLocationsForWelcomeDocument,
    fetchOnMount: false,
    cachePolicy: 'network-only'
  })

  // get minions for location id
  const {
    onData: onMinionsCallComplete,
    onError: onMinionsCallError,
    execute: getMinionsByLocId
  } = useQuery({
    query: FindMinionsForWelcomeDocument,
    fetchOnMount: false,
    cachePolicy: 'network-only',
    variables: variables
  })

  // get certificate for location id
  const { onData: onDownloadCertCallComplete, execute: downloadMinionCertificate } = useQuery({
    query: DownloadMinionCertificateForWelcomeDocument,
    fetchOnMount: false,
    cachePolicy: 'network-only',
    variables: certVariables,
    paused: true
  })

  // check to see we have only 1 location (default)
  onLocationsCallComplete((data) => {
    if (
      data.findAllLocations?.length !== 1 ||
      data.findAllLocations[0].location?.toLowerCase() !== defaultLocationName
    ) {
      promiseResolve()
      return
    }
    variables.locationId = data.findAllLocations[0].id
    certVariables.location = data.findAllLocations[0].id
  })

  // check that there are no configured minions
  onMinionsCallComplete((data) => {
    if (data.findMinionsByLocationId?.length === 0) {
      isShowOnboardingState.value = true
    }
    promiseResolve()
  })

  onLocationsCallError((err) => promiseReject(err))
  onMinionsCallError((err) => promiseReject(err))

  onDownloadCertCallComplete((data) => (minionCert.value = data.getMinionCertificate))

  return {
    downloadMinionCertificate,
    getMinionsByLocId,
    checkSetupState,
    isShowOnboardingState,
    defaultLocationName,
    minionCert
  }
})
