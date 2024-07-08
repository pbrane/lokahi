import { defineStore } from 'pinia'
import { InventoryMapper } from '@/mappers'
import { InventoryItem, InventoryItemFilters, NewInventoryNode, RawMetrics } from '@/types/inventory'
import { Tag } from '@/types/graphql'
import { useTagStore } from '../Components/tagStore'
import { useInventoryQueries } from '../Queries/inventoryQueries'
import { Pagination } from '@/types/alerts'
import { cloneDeep } from 'lodash'
import { InventoryFilter } from '@/types'
import { InventoryComparator } from '@/components/MonitoringPolicies/monitoringPolicies.constants'

const inventoryNodesFilterDefault: InventoryItemFilters = {
  sortAscending: true,
  sortBy: 'id',
  searchValue: '',
  searchType: 'labels'
}

const defaultPagination: Pagination = {
  page: 1, // FE pagination component has base 1 (first page)
  pageSize: 10,
  total: 0
}

export const useInventoryStore = defineStore('inventoryStore', {
  state: () => ({
    isTagManagerOpen: false,
    isTagManagerReset: false,
    isFilterOpen: false,
    loading: false,
    monitoredFilterActive: false,
    nodes: [] as InventoryItem[],
    unmonitoredFilterActive: false,
    detectedFilterActive: false,
    nodesSelected: [] as NewInventoryNode[],
    searchType: { id: 1, name: InventoryComparator[InventoryFilter.TAGS] },
    tagsSelected: [] as Tag[],
    loadingTimeout: -1,
    isEditMode: false,
    inventoryNodesDefaultFilter: cloneDeep(inventoryNodesFilterDefault),
    inventoryNodesPagination: cloneDeep(defaultPagination),
    totalInventoryNodes: 0
  }),
  actions: {
    async init() {
      this.loading = true
      const { buildNetworkInventory } = useInventoryQueries()
      const page = this.inventoryNodesPagination.page > 0 ? this.inventoryNodesPagination.page - 1 : 0
      const pagination = {
        ...this.inventoryNodesPagination,
        page
      }
      const data = await buildNetworkInventory(this.inventoryNodesDefaultFilter, pagination)
      if (data && data.searchNodes && data.allMetrics) {
        const nodelist = {
          findAllNodes: data.searchNodes.nodesList,
          allMetrics: data.allMetrics,
          lastPage: data.searchNodes.lastPage,
          nextPage: data.searchNodes.nextPage
        }
        this.totalInventoryNodes = data.searchNodes.totalNodes || 0
        if (this.inventoryNodesPagination.total !== this.totalInventoryNodes) {
          this.inventoryNodesPagination.total = this.totalInventoryNodes
        }
        this.receivedNetworkInventory(nodelist as any)
      }
      this.loadingTimeout = window.setTimeout(() => { this.loading = false }, 3000)
    },
    async filterNodesByTags() {
      const {getNodesByTags} = useInventoryQueries()
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      const tags = this.tagsSelected.map((tag) => tag.name!)
      const nodes = await getNodesByTags(tags)
      const b = InventoryMapper.fromServer(nodes.value?.findAllNodesByTags as Array<NewInventoryNode>, nodes.value?.allMetrics as RawMetrics)
      this.nodes = b.nodes
    },
    async filterNodesByLabel(label: string) {
      const {getNodesByLabel} = useInventoryQueries()
      const nodes = await getNodesByLabel(label)
      const b = InventoryMapper.fromServer(nodes.value?.findAllNodesByNodeLabelSearch as Array<NewInventoryNode>, nodes.value?.allMetrics as RawMetrics)
      this.nodes = b.nodes
    },
    receivedNetworkInventory(d: { findAllNodes: Array<NewInventoryNode>, allMetrics: RawMetrics }) {
      const b = InventoryMapper.fromServer(d.findAllNodes, d.allMetrics)
      this.nodes = b.nodes
      window.clearTimeout(this.loadingTimeout)
      this.loading = false
    },
    toggleTagManager() {
      this.isTagManagerOpen = !this.isTagManagerOpen

      if (!this.isTagManagerOpen) {
        this.tagsSelected = []
        this.isEditMode = false
        const tagStore = useTagStore()
        tagStore.tagsSelected = []
        tagStore.setTagEditMode(false)
      }
    },
    toggleFilter() {
      this.isFilterOpen = !this.isFilterOpen
    },
    toggleNodeEditMode() {
      this.isEditMode = !this.isEditMode
    },
    addSelectedTag(beep: Tag[]) {
      this.tagsSelected = beep
    },
    resetNodeEditMode() {
      this.isEditMode = false
    },
    addRemoveNodesSelected(node: NewInventoryNode) {
      if (this.nodesSelected.find((d) => d.id === node.id)) {
        this.nodesSelected = this.nodesSelected.filter(({ id }) => id !== node.id)
      } else {
        this.nodesSelected.push(node)
      }
    },
    selectAll(allNodes: NewInventoryNode[]) {
      this.nodesSelected = [...allNodes]
    },
    clearAll() {
      this.nodesSelected = []
    },
    setSearchType(searchType: any) {
      this.searchType = searchType
      this.inventoryNodesDefaultFilter = {
        ...this.inventoryNodesDefaultFilter,
        searchType: searchType?.value
      }
    },
    resetSelectedNode() {
      this.nodesSelected = []
    },
    setInventoriesByNodePage(page: number) {
      if (page !== Number(this.inventoryNodesPagination.page)) {
        this.inventoryNodesPagination = {
          ...this.inventoryNodesPagination,
          page
        }
      }
      this.init()
    },
    setInventoriesByNodePageSize(pageSize: number) {
      if (pageSize !== this.inventoryNodesPagination.pageSize) {
        this.inventoryNodesPagination = {
          ...this.inventoryNodesPagination,
          page: 0, // always request first page on change
          pageSize
        }
      }

      this.init()
    },
    inventeriesByNodeSortChanged(sortObj: any) {
      this.inventoryNodesDefaultFilter = {
        ...this.inventoryNodesDefaultFilter,
        sortBy: sortObj.sortBy,
        sortAscending: sortObj.sortAscending
      }
      if (this.inventoryNodesPagination.page !== 1 || this.inventoryNodesPagination.total !== this.totalInventoryNodes) {
        this.inventoryNodesPagination = {
          ...this.inventoryNodesPagination,
          page: 0, // always request first page on change
          total: this.totalInventoryNodes
        }
      }
      this.init()
    }
  }
})
