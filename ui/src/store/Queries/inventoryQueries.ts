import { useQuery } from 'villus'
import { defineStore } from 'pinia'
import {
  BuildNetworkInventoryPageDocument,
  FindAllNodesByTagsDocument,
  FindAllNodesByNodeLabelSearchDocument,
  AllNodesStatusDocument
} from '@/types/graphql'
import { Pagination } from '@/types/alerts'
import { InventoryItemFilters } from '@/types'

export const useInventoryQueries = defineStore('inventoryQueries', () => {

  const getNodesByTags = async (tags: string | string[] | undefined) => {
    const { execute, data } = useQuery({
      query: FindAllNodesByTagsDocument,
      variables: {
        tags
      },
      cachePolicy: 'network-only'
    })
    await execute()
    return data
  }

  const getNodesByLabel = async (labelSearchTerm: string) => {
    const { execute, data } = useQuery({
      query: FindAllNodesByNodeLabelSearchDocument,
      variables: {
        labelSearchTerm
      },
      cachePolicy: 'network-only'
    })
    await execute()
    return data
  }

  const getAllNodeStatus = async () => {
    const { execute, data } = useQuery({
      query: AllNodesStatusDocument,
      cachePolicy: 'network-only'
    })
    await execute()
    return data
  }

  const buildNetworkInventory = async (filters?: InventoryItemFilters, paginations?: Pagination) => {
    const { execute, data } = useQuery({
      query: BuildNetworkInventoryPageDocument,
      variables: {
        pageSize: paginations?.pageSize,
        page: paginations?.page || 0,
        sortBy: filters?.sortBy,
        sortAscending: filters?.sortAscending || true,
        searchValue: filters?.searchValue || '',
        searchType: filters?.searchType
      },
      fetchOnMount: false,
      cachePolicy: 'network-only'
    })
    await execute()
    return toRaw(data.value)
  }

  return {
    buildNetworkInventory,
    getNodesByTags,
    getNodesByLabel,
    getAllNodeStatus
  }
})
