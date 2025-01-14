<template>
  <div class="full-page-container">
    <div class="flex">
      <HeadlinePage
        text="Network Inventory"
        class="header"
        data-test="page-header"
      />
      <FeatherButton secondary>
        IMPORT NODES
      </FeatherButton>
    </div>

    <InventoryTagModal
      :visible="tagStore.isVisible"
      :title="''"
      :node="tagStore.activeNode"
      :closeModal="tagStore.closeModal"
    />
    <FeatherTabContainer
      class="tab-container"
      data-test="tab-container"
    >
      <template v-slot:tabs>
        <FeatherTab
          >Monitored Nodes
          <FeatherTextBadge
            :type="BadgeTypes.info"
            v-if="totalCountNodes > 0"
            >{{ totalCountNodes}}</FeatherTextBadge
          >
        </FeatherTab>
        <FeatherTab
          >Unmonitored Nodes
          <FeatherTextBadge
            :type="BadgeTypes.info"
            v-if="tabUnmonitoredContent.length > 0"
            >{{ tabUnmonitoredContent.length }}</FeatherTextBadge
          >
        </FeatherTab>
        <FeatherTab
          >Detected Nodes
          <FeatherTextBadge
            :type="BadgeTypes.info"
            v-if="tabDetectedContent.length > 0"
            >{{ tabDetectedContent.length }}</FeatherTextBadge
          >
        </FeatherTab>
      </template>

      <!-- Monitored Nodes -->
      <FeatherTabPanel>
         <div class="tab-navigation"  v-if="tabMonitoredContent.length && !inventoryStore.loading">
          <p class="search-node"> Search by name/tag or use additional filters to filters nodes </p>
          <div class="tab-menu">
            <FeatherButton secondary :class="{ 'active-tab': view === 'table' }" @click="toggleView('table')" :active="view === 'table'">
               Table
            </FeatherButton>
            <FeatherButton secondary :class="{ 'active-tab': view === 'card' }" @click="toggleView('card')" :active="view === 'card'">
              Card
            </FeatherButton>
          </div>
         </div>
        <InventoryFilter
          v-if="inventoryStore.monitoredFilterActive"
          :state="MonitoredStates.MONITORED"
          :nodes="tabMonitoredContent"
        />
        <InventoryTabTable
          v-if="tabMonitoredContent.length && !inventoryStore.loading && view === 'table'"
          :tabContent="tabMonitoredContent"
          :state="MonitoredStates.MONITORED"
        />
        <InventoryTabContent
          v-if="tabMonitoredContent.length && !inventoryStore.loading && view === 'card'"
          :tabContent="tabMonitoredContent"
          :state="MonitoredStates.MONITORED"
        />
        <EmptyList
          data-test="monitored-empty"
          v-if="!tabMonitoredContent.length && !inventoryStore.loading"
          bg
          :content="{
            msg: 'No monitored nodes. Add some on the Discovery page.',
            btn: {
              label: 'Visit Discovery Page',
              action: () => {
                $router.push('/discovery')
              }
            }
          }"
        />
        <FeatherSpinner v-if="inventoryStore.loading" />
      </FeatherTabPanel>

      <!-- Unmonitored Nodes -->
      <FeatherTabPanel>
        <InventoryFilter
          v-if="inventoryStore.unmonitoredFilterActive"
          :state="MonitoredStates.UNMONITORED"
          onlyTags
          :nodes="tabUnmonitoredContent"
        />
        <InventoryTabContent
          v-if="tabUnmonitoredContent.length && !inventoryStore.loading"
          :tabContent="tabUnmonitoredContent"
          :state="MonitoredStates.UNMONITORED"
        />
        <EmptyList
          data-test="unmonitored-empty"
          v-if="!tabUnmonitoredContent.length && !inventoryStore.loading"
          bg
          :content="{
            msg: 'No unmonitored nodes. Add some on the Discovery page.',
            btn: {
              label: 'Visit Discovery Page',
              action: () => {
                $router.push('/discovery')
              }
            }
          }"
        />
        <FeatherSpinner v-if="inventoryStore.loading" />
      </FeatherTabPanel>

      <!-- Detected Nodes -->
      <FeatherTabPanel>
        <InventoryFilter
          v-if="inventoryStore.detectedFilterActive"
          :state="MonitoredStates.DETECTED"
          onlyTags
          :nodes="tabDetectedContent"
        />
        <InventoryTabContent
          v-if="tabDetectedContent.length && !inventoryStore.loading"
          :tabContent="tabDetectedContent"
          :state="MonitoredStates.DETECTED"
        />
        <EmptyList
          data-test="discovery-empty"
          v-if="!tabDetectedContent.length && !inventoryStore.loading"
          bg
          :content="{
            msg: 'No detected nodes. Add some on the Discovery page.',
            btn: {
              label: 'Visit Discovery Page',
              action: () => {
                $router.push('/discovery')
              }
            }
          }"
        />
        <FeatherSpinner v-if="inventoryStore.loading" />
      </FeatherTabPanel>
    </FeatherTabContainer>
  </div>
</template>

<script lang="ts" setup>
import { FeatherTab, FeatherTabContainer, FeatherTabPanel } from '@featherds/tabs'
import InventoryFilter from '@/components/Inventory/InventoryFilter.vue'
import InventoryTabContent from '@/components/Inventory/InventoryTabContent.vue'
import { MonitoredStates, InventoryItem } from '@/types'
import { useInventoryStore } from '@/store/Views/inventoryStore'
import { FeatherTextBadge, BadgeTypes } from '@featherds/badge'
import { useTagStore } from '@/store/Components/tagStore'

const view = ref('card')
const inventoryStore = useInventoryStore()
const tabMonitoredContent = computed((): InventoryItem[] =>
  inventoryStore.nodes.filter((d) => d.monitoredState === MonitoredStates.MONITORED)
)
const totalCountNodes = computed(() => inventoryStore.totalInventoryNodes)
const tabUnmonitoredContent = computed((): InventoryItem[] =>
  inventoryStore.nodes.filter((d) => d.monitoredState === MonitoredStates.UNMONITORED)
)
const tabDetectedContent = computed((): InventoryItem[] =>
  inventoryStore.nodes.filter((d) => d.monitoredState === MonitoredStates.DETECTED)
)
const tagStore = useTagStore()

onMounted(async () => {
  inventoryStore.init()
})

const toggleView = (selectedView: string) => {
  view.value = selectedView
}

/**
 * If at any point, a tab of content is more than zero,
 * show the corresponding filter. Prior to this change, it
 * was hidden on zero results, which meant hidden when no search results.
 */
watchEffect(() => {
  if (tabMonitoredContent.value.length > 0) {
    inventoryStore.monitoredFilterActive = true
  }
  if (tabUnmonitoredContent.value.length > 0) {
    inventoryStore.unmonitoredFilterActive = true
  }
  if (tabDetectedContent.value.length > 0) {
    inventoryStore.detectedFilterActive = true
  }
})
</script>
<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@/styles/vars';

.tab-container {
  :deep(> ul) {
    display: flex;
    border-bottom: 1px solid var(variables.$secondary-text-on-surface);
    min-width: vars.$min-width-smallest-screen;

    > li {
      display: flex !important;
      flex-grow: 1;

      > button {
        display: flex;
        flex-grow: 1;

        > span {
          flex-grow: 1;
        }
      }
    }
  }

  .tab-navigation {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .search-node {
      font-weight: bold;
      margin-bottom: var(variables.$spacing-s);
    }

    .tab-menu {
      display: flex;
      align-items: center;
      justify-content: flex-end;
      .active-tab {
        background-color: var(variables.$primary);
        color: white !important;
      }
    }
 }
}

.flex {
  display: flex;
  width: 100%;
  justify-content: space-between;
  align-items: center;
}

.tag-manager {
  margin-right: 24px;
}

.padding {
  padding: 24px;
  transition: padding ease-in-out 0.4s;
}

.no-padding {
  padding: 24px;
  padding-top: 0;
  padding-bottom: 0;
  transition: padding ease-in-out 0.4s;
}
</style>
