<template>
  <div class="cards">
    <div v-for="node in tabContent" :key="node.id" class="card" :data-test="state">
      <section class="node-header">
        <h5 data-test="heading" class="node-label pointer" @click="() => onNodeClick(node.id)">{{ node?.nodeAlias || node?.nodeLabel }}</h5>
        <div class="card-chip-list">
          <div class="text-badge-row" v-if="state === MonitoredStates.MONITORED">
            <div v-for="badge, index in metricsAsTextBadges(node?.metrics,String(node?.id))" :key="index">
              <TextBadge v-if="badge.label" :type="badge.type">{{ badge.label }}</TextBadge>
            </div>
          </div>
        </div>
      </section>
      <section class="node-content">
        <div>
          <InventoryTextTagList :location="node?.location?.location" :ipAddress="node.ipInterfaces[0].ipAddress" data-test="text-anchor-list" />
        </div>
      </section>
      <div class="node-footer">
        <FeatherButton secondary @click="openModalForDeletingTags(node)">
          Tags: <span class="count">{{ node.tags.length }}</span>
        </FeatherButton>
        <InventoryIconActionList :node="node" className="icon-action" data-test="icon-action-list" />
      </div>
      <InventoryNodeTagEditOverlay v-if="tagStore.isTagEditMode" :node="node" />
    </div>
  </div>
  <div class="inventory-card-list" v-if="hasNodes">
    <FeatherPagination
      v-model="page"
      :pageSize="pageSize"
      :total="total"
      :pageSizes="[10, 20, 50]"
      @update:model-value="onPageChanged"
      @update:pageSize="onPageSizeChanged"
      data-test="pagination"
    />
  </div>
</template>

<script lang="ts" setup>
import { PropType } from 'vue'
import { NewInventoryNode, InventoryItem, MonitoredStates } from '@/types'
import { useTagStore } from '@/store/Components/tagStore'
import { useInventoryStore } from '@/store/Views/inventoryStore'
import TextBadge from '../Common/TextBadge.vue'
import useSpinner from '@/composables/useSpinner'
import {metricsAsTextBadges} from './inventory.utils'

defineProps({
  tabContent: {
    type: Object as PropType<InventoryItem[]>,
    required: true
  },
  state: {
    type: String,
    required: true
  }
})

const { startSpinner, stopSpinner } = useSpinner()
const tagStore = useTagStore()
const inventoryStore = useInventoryStore()
const isTagManagerReset = computed(() => inventoryStore.isTagManagerReset)
const page = computed(() => inventoryStore.inventoryNodesPagination.page || 1)
const pageSize = computed(() => inventoryStore.inventoryNodesPagination.pageSize)
const total = computed(() => inventoryStore.inventoryNodesPagination.total)
const data = computed(() => inventoryStore.nodes || [])

const hasNodes = computed(() => {
  return (data.value || []).length > 0
})

const router = useRouter()

watch(isTagManagerReset, (isReset) => {
  if (isReset) {
    resetState()
  }
})

const resetState = () => {
  tagStore.selectAllTags(false)
  tagStore.setTagEditMode(false)
  inventoryStore.resetSelectedNode()
  inventoryStore.isTagManagerReset = false
}

const onNodeClick = (id: number) => {
  router.push({
    name: 'Node Status',
    params: { id }
  })
}

const openModalForDeletingTags = (node: NewInventoryNode) => {
  tagStore.setActiveNode(node)
  tagStore.openModal()
}



const onPageChanged = (p: number) => {
  startSpinner()
  inventoryStore.setInventoriesByNodePage(p)
  stopSpinner()
}

const onPageSizeChanged = (p: number) => {
  startSpinner()
  inventoryStore.setInventoriesByNodePageSize(p)
  stopSpinner()
}
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@/styles/vars';
@use '@/styles/mediaQueriesMixins';
@use '@featherds/styles/mixins/typography';

.ctrls {
  display: flex;
  justify-content: end;
  padding: var(variables.$spacing-s) 0;
  min-width: vars.$min-width-smallest-screen;
}

.cards {
  display: flex;
  flex-flow: row wrap;
  gap: 1%;

  .card {
    position: relative;
    padding: var(variables.$spacing-m) var(variables.$spacing-m);
    border: 1px solid rgba(21, 24, 43, 0.12);
    border-radius: 4px;
    width: 100%;
    min-width: vars.$min-width-smallest-screen;
    margin-bottom: var(variables.$spacing-m);
    background: var(variables.$surface);

    @include mediaQueriesMixins.screen-sm {
      width: 100%;
    }

    @include mediaQueriesMixins.screen-md {
      width: auto;
      min-width: 438px;
    }

    .node-header {
      margin-bottom: var(variables.$spacing-s);
      display: flex;
      flex-direction: row;
      gap: 0.5rem;
      align-items: center;
      justify-content: space-between;
    }

    .node-label {
      margin: 0;
      line-height: 20px;
      letter-spacing: 0.28px;
    }
  }
}

.node-content {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  gap: 2rem;
}

.node-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 40px;
}

.text-badge-row {
  display:flex;
}

.inventory-card-list {
  margin-bottom: 10px;
 :deep(.feather-pagination) {
  border: none !important;
 }
}
</style>
