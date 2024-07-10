import { AddUserDocument, UpdateUserDocument } from '@/types/graphql'
import { defineStore } from 'pinia'
import { useMutation } from 'villus'

export const useUsersMutations = defineStore('usersMutations', () => {
  const { execute: createNewUser, error: errorWhileCreatingUser, isFetching: isFetchingWhileCreatingUser } = useMutation(AddUserDocument)
  const { execute: updateUser, error: errorWhileUpdatingUser, isFetching: isFetchingWhileUpdatingUser } = useMutation(UpdateUserDocument)

  return {
    createNewUser,
    errorWhileCreatingUser: computed(() => errorWhileCreatingUser),
    isFetchingWhileCreatingUser: computed(() => isFetchingWhileCreatingUser),
    updateUser,
    errorWhileUpdatingUser: computed(() => errorWhileUpdatingUser),
    isFetchingWhileUpdatingUser: computed(() => isFetchingWhileUpdatingUser)
  }
})
