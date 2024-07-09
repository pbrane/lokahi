import { AddUserDocument } from '@/types/graphql'
import { defineStore } from 'pinia'
import { useMutation } from 'villus'

export const useUsersMutations = defineStore('usersMutations', () => {
  const { execute: createNewUser, error: errorWhileCreatingUser, isFetching: isFetchingWhileCreatingUser } = useMutation(AddUserDocument)

  return {
    createNewUser,
    errorWhileCreatingUser: computed(() => errorWhileCreatingUser),
    isFetchingWhileCreatingUser: computed(() => isFetchingWhileCreatingUser)
  }
})
