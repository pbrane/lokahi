<template>
  <div class="third-tab-container">
    <NodeStatusThirdTabEventsTable :events="store.events" />
  </div>
</template>

<script setup lang="ts">
import { useNodeStatusStore } from '@/store/Views/nodeStatusStore'
const store = useNodeStatusStore()
</script>

<style scoped lang="scss">
.third-tab-container {
  margin: 20px 0px;
}
</style>
