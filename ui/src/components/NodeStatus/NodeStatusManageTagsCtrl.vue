<template>
  <FeatherButton
    :primary="primary"
    :text="text"
    @click="openManageTagsModal"
  >
    {{ label }}
  </FeatherButton>
</template>

<script setup lang="ts">
import { useNodeStatusStore } from '@/store/Views/nodeStatusStore'
import { useTagStore } from '@/store/Components/tagStore'
import { Node } from '@/types/graphql'

const store = useNodeStatusStore()
const tagStore = useTagStore()

defineProps<{
  primary?: boolean
  text?: boolean
  label: string
}>()

const openManageTagsModal = () => {
  tagStore.setActiveNode(store.node as unknown as Node)
  tagStore.openModal()
}
</script>
