<template>
  <div class="first-tab-container">
    <div class="left-column">
      <NodeStatusFirstTabGeneralInfo :node="(store.node as unknown as Node)" />
      <NodeStatusFirstTabTags :tags="store.node.tags ?? []" :openModal="openManageTagsModal" />
    </div>
    <div class="right-column"></div>
  </div>
  <TagsModal
    :closeModal="closeManageTagsModal"
    :visible="tagStore.isVisible"
    :node="(store.node as unknown as Node)"
  />
</template>

<script setup lang="ts">
import { useNodeStatusStore } from '@/store/Views/nodeStatusStore'
import { useNodeStatusQueries } from '@/store/Queries/nodeStatusQueries';
import { useTagStore } from '@/store/Components/tagStore'
import { Node } from '@/types/graphql'

const store = useNodeStatusStore()
const queries = useNodeStatusQueries()
const tagStore = useTagStore()

const openManageTagsModal = () => {
  tagStore.setActiveNode(store.node as unknown as Node)
  tagStore.openModal()
}

const closeManageTagsModal = () => {
  queries.fetchNodeStatus()
  tagStore.closeModal()
}
</script>

<style scoped lang="scss">
.first-tab-container {
  display: flex;
  margin: 20px 0px;
  gap: 30px;

  .left-column,
  .right-column {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .left-column {
    flex: 1;
  }

  .right-column {
    flex: 2;
  }
}
</style>
