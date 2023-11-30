<template>
  <div class="full-node-status-wrapper">
    <div class="header-btn-wrapper">
      <NodeStatusPageHeader />
      <NodeStatusManageTagsCtrl
        label="Manage Tags"
        primary
      />
    </div>
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
  <TagsModal
    :closeModal="closeManageTagsModal"
    :visible="tagStore.isVisible"
    :node="(store.node as unknown as Node)"
  />
</template>

<script setup lang="ts">
import { useNodeStatusStore } from '@/store/Views/nodeStatusStore'
import { useNodeStatusQueries } from '@/store/Queries/nodeStatusQueries'
import { useTagStore } from '@/store/Components/tagStore'
import { Node } from '@/types/graphql'

const tagStore = useTagStore()
const store = useNodeStatusStore()
const queries = useNodeStatusQueries()
const route = useRoute()

const closeManageTagsModal = () => {
  queries.fetchNodeStatus()
  tagStore.closeModal()
}

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

  .header-btn-wrapper {
    display: flex;
    justify-content: space-between;
    margin-top: 40px;
    margin-bottom: 40px;
  }
}
</style>
