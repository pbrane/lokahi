import { ListGetAllUsersDocument } from '@/types/graphql'
import { defineStore } from 'pinia'
import { useQuery } from 'villus'

export const useUserQueries = defineStore('userQueries', () => {
  const { data, execute: loadAllUsers } = useQuery({
    query: ListGetAllUsersDocument,
    cachePolicy: 'network-only'
  })

  const allUsersList = computed(() => {
    return data.value?.getAllUsers ?? []
  })

  return {
    allUsersList,
    loadAllUsers
  }
})
