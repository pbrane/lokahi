<template>
  <div class="header">
    <div class="pre-title">Network Inventory</div>
    <div class="page-headline">
      {{ store.node.nodeAlias || store.node.nodeLabel }}
      <FeatherButton
        icon="Edit"
        @click="openModal"
      >
        <FeatherIcon :icon="EditIcon" />
      </FeatherButton>
    </div>
    <div
      class="post-title"
      v-if="store.node.nodeAlias"
    >
      {{ store.node.nodeLabel }}
    </div>
  </div>
  <EditModal
    :isVisible="isVisible"
    :closeModal="() => closeModal()"
    :handler="(newAlias: string) => store.updateNodeAlias(newAlias)"
    :callback="() => queries.fetchNodeStatus()"
    title="Edit Node Name"
    inputLabel="Type a new name"
    :currentValue="store.node.nodeAlias || ''"
  />
</template>

<script setup lang="ts">
import useModal from '@/composables/useModal'
import EditIcon from '@featherds/icon/action/EditMode'
import { useNodeStatusQueries } from '@/store/Queries/nodeStatusQueries'
import { useNodeStatusStore } from '@/store/Views/nodeStatusStore'

const { openModal, closeModal, isVisible } = useModal()
const store = useNodeStatusStore()
const queries = useNodeStatusQueries()
</script>

<style lang="scss" scoped>
@use '@featherds/styles/mixins/typography';
@use '@featherds/styles/themes/variables';

.header {
  margin-top: 40px;
  margin-bottom: 40px;
  display: flex;
  flex-direction: column;

  .pre-title {
    @include typography.button;
    color: var(variables.$secondary-variant);
  }

  .page-headline {
    @include typography.headline1;
  }

  .post-title {
    @include typography.caption;
  }
}
</style>
