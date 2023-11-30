<template>
  <div class="first-tab-container">
    <div class="left-column">
      <NodeStatusFirstTabGeneralInfo :node="(store.node as unknown as Node)" />
      <NodeStatusFirstTabTags :tags="store.node.tags ?? []" />
    </div>
    <div class="right-column"></div>
  </div>
</template>

<script setup lang="ts">
import { useNodeStatusStore } from '@/store/Views/nodeStatusStore'
import { Node } from '@/types/graphql'

const store = useNodeStatusStore()
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
