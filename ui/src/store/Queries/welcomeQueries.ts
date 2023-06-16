import { defineStore } from 'pinia'
import { useQuery } from 'villus'
import { fncArgVoid } from '@/types'
import {
  FindLocationsForWelcomeDocument,
  FindMinionsForWelcomeDocument,
  DownloadMinionCertificateForWelcomeDocument,
  FindDevicesForWelcomeDocument,
  Node
} from '@/types/graphql'

export const useWelcomeQueries = defineStore('welcomeQueries', () => {
  const isShowOnboardingState = ref(false)
  const isMinionDetected = ref(false)
  const detectedDevice = ref<undefined | Partial<Node>>()
  const minionCert = ref()
  const variables = reactive({ locationId: undefined })
  const certVariables = reactive({ location: undefined })
  const defaultLocationName = 'TestLocation'

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

  // get all devices
  const { execute: getDevices, onData: onDevicesCallComplete } = useQuery({
    query: FindDevicesForWelcomeDocument,
    fetchOnMount: false,
    cachePolicy: 'network-only'
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
      data.findAllLocations[0].location?.toLowerCase() !== defaultLocationName.toLowerCase()
    ) {
      promiseResolve()
      return
    }
    variables.locationId = data.findAllLocations[0].id
    certVariables.location = data.findAllLocations[0].id
  })

  // check if there are configured minions
  onMinionsCallComplete((data) => {
    if (data.findMinionsByLocationId?.length === 0) {
      isShowOnboardingState.value = true
    } else {
      isMinionDetected.value = true
      getDevices()
    }
    promiseResolve()
  })

  // detected device will be used to auto populate the form
  onDevicesCallComplete((data) => {
    for (const device of data.findAllNodes || []) {
      for (const inter of device.ipInterfaces || []) {
        if (inter.snmpPrimary) {
          return detectedDevice.value = device
        }
      }
    }
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
    isMinionDetected,
    detectedDevice,
    minionCert
  }
})
