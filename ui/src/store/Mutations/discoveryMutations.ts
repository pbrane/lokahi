import { defineStore } from 'pinia'
import { useMutation } from 'villus'

import { AddAzureCredentialDocument } from '@/types/graphql'

export const useDiscoveryMutations = defineStore('discoveryMutations', () => {
  const {
    execute: addAzureCreds,
    error
  } = useMutation(AddAzureCredentialDocument)

  return {
    addAzureCreds,
    azureError: computed(() => error)
  }
})
