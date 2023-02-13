<template>
  <div class="container">
    <PageHeader heading="Flows" />
    <h1>{{ flowSummary?.numFlows }}</h1>
  </div>
</template>

<script setup lang="ts">
import { useFlowQueries } from '@/store/Queries/flowsQueries'
const store = useFlowQueries()

const flowSummary = computed(() => {
  return store.flowSummary
})
</script>
