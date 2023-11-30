<template>
  <div class="full-node-status-wrapper">
    <NodeStatusPageHeader />
    <FeatherTabContainer>
      <template v-slot:tabs>
        <FeatherTab>Status</FeatherTab>
        <FeatherTab>Interfaces</FeatherTab>
        <FeatherTab>Events</FeatherTab>
      </template>
      <FeatherTabPanel>
        <NodeStatusFirstTab />
      </FeatherTabPanel>
      <FeatherTabPanel>
        <SNMPInterfacesTable v-if="!store.isAzure" />
        <IPInterfacesTable />
      </FeatherTabPanel>
      <FeatherTabPanel>
        <EventsTable />
      </FeatherTabPanel>
    </FeatherTabContainer>
  </div>
</template>

<script setup lang="ts">
import { useNodeStatusStore } from '@/store/Views/nodeStatusStore'
import { useNodeStatusQueries } from '@/store/Queries/nodeStatusQueries'
const store = useNodeStatusStore()
const queries = useNodeStatusQueries()
const route = useRoute()


onBeforeMount(() => {
  const nodeId = Number(route.params.id)
  store.setNodeId(nodeId)
  store.fetchExporters(nodeId)
  queries.fetchNodeStatus()
})
</script>

<style lang="scss" scoped>
.full-node-status-wrapper {
  max-width: 1262px;
  margin: 0 auto;
  padding-left: 40px;
  padding-right: 40px;
}
</style>
