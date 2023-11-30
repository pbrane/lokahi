import { defineStore } from 'pinia'
import { Node, Tag } from '@/types/graphql'
import { useInventoryStore } from '../Views/inventoryStore'
import { useNodeMutations } from '../Mutations/nodeMutations'
import { NewInventoryNode } from '@/types'
import useSnackbar from '@/composables/useSnackbar'
import useModal from '@/composables/useModal'

export const useTagStore = defineStore('tagStore', () => {
  const tagSelected = ref<Tag>()
  const tagsSelected = ref([] as Tag[])
  const isTagEditMode = ref(false)
  const areAllTagsSelected = ref(false)
  const filteredTags = ref([] as Tag[])
  const originalTags = ref([] as Tag[])
  const activeNode = ref()
  const isLoading = ref(false)

  const { openModal, closeModal, isVisible } = useModal()
  const setFilteredTags = async (inFilteredTags: Tag[]) => {
    originalTags.value = [...inFilteredTags]
    filteredTags.value = [...inFilteredTags]
  }
  const setActiveNode = (node: NewInventoryNode | Node) => {
    activeNode.value = node
  }

  const setTags = (tagList: Tag[]) => {
    tagsSelected.value = [...tagList]
    updateTagEditMode()
  }

  const addFilteredTag = (tag: Tag) => {
    if (!filteredTags.value.find((t) => t.name === tag.name)){
      filteredTags.value = [...filteredTags.value].concat([tag])
    }
  }

  const saveFilteredTagsToNode = async () => {
    const nodeMutations = useNodeMutations()
    const snackbar = useSnackbar()
    isLoading.value = true
    const newTags = filteredTags.value.filter((d) => !originalTags.value.find((e) => e.id === d.id))
    const tagsToDelete = originalTags.value.filter((d) => !filteredTags.value.find((e) => e.id === d.id))
    const result = await nodeMutations.addTagsToNodes({ nodeIds:[activeNode.value.id], tags:newTags.map((b) => ({name:b.name})) })
    if (result.error){
      snackbar.showSnackbar({msg:result.error.message,error:true})
    }
    const resultRemove = await nodeMutations.removeTagsFromNodes({ nodeIds:[activeNode.value.id], tagIds:tagsToDelete.map((b) => (b.id)) })
    if (resultRemove.error){
      snackbar.showSnackbar({msg:resultRemove.error.message,error:true})
    }
    isLoading.value = false
    return !result.error && !resultRemove.error
  }

  const addNewTag = (newTag: Record<string, string>, skipTagEdit = false ) => {
    const tagSelectedExists = tagsSelected.value.some(({ name }) => name === newTag.name)
    if (!tagSelectedExists) tagsSelected.value.push(newTag as Tag)
    if (!skipTagEdit) updateTagEditMode()
  }

  const updateTagEditMode = () => {
    isTagEditMode.value = tagsSelected.value.length > 0
  }

  const setTagEditMode = (isEdit: boolean) => {
    isTagEditMode.value = isEdit
  }

  const deactivateTag = (tagIndex: number) => {
    const newTags = [...tagsSelected.value]
    newTags.splice(tagIndex, 1)
    setTags(newTags)
    if (newTags.length === 0) {
      setTagEditMode(false)
      const inventoryStore = useInventoryStore()
      inventoryStore.nodesSelected = []
    }
  }
  const toggleTagsSelected = (tag: Tag) => {
    const isTagAlreadySelected = tagsSelected.value.some(({ name }) => name === tag.name)

    if (isTagAlreadySelected) {
      tagsSelected.value = tagsSelected.value.filter(({ name }) => name !== tag.name)
    } else {
      tagsSelected.value.push(tag)
    }
  }

  const selectAllTags = (selectAll: boolean) => {
    tagsSelected.value = selectAll ? tagsSelected.value : []
  }

  const resetState = () => {
    areAllTagsSelected.value = false
    tagsSelected.value = []
  }

  const saveTagsToSelectedNodes = async () => {
    const tags = tagsSelected.value.map(({ name }) => ({ name }))
    const inventoryStore = useInventoryStore()
    const nodeMutations = useNodeMutations()
    const nodeIds = inventoryStore.nodesSelected.map((node) => node.id)
    await nodeMutations.addTagsToNodes({ nodeIds, tags })
    tagsSelected.value = []
    inventoryStore.isTagManagerOpen = false
    inventoryStore.nodesSelected = []
    isTagEditMode.value = false
    resetState()
  }

  const toggleSelectAll = () => {
    areAllTagsSelected.value = !areAllTagsSelected.value
    selectAllTags(areAllTagsSelected.value)
  }
  const filterTag = (tag: Tag) => {
    filteredTags.value = filteredTags.value.filter((d) => d.name !== tag.name)
  }
  return {
    activeNode,
    tagSelected,
    tagsSelected,
    areAllTagsSelected,
    setTags,
    addNewTag,
    isTagEditMode: computed(() => isTagEditMode.value),
    saveTagsToSelectedNodes,
    setTagEditMode,
    selectAllTags,
    filterTag,
    isLoading,
    openModal,
    closeModal,
    isVisible,
    toggleSelectAll,
    toggleTagsSelected,
    deactivateTag,
    filteredTags,
    updateTagEditMode,
    setFilteredTags,
    addFilteredTag,
    saveFilteredTagsToNode,
    setActiveNode
  }
})
