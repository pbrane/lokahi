<template>
  <ul class="filter-container">
    <li v-if="!onlyTags" class="autocomplete flex margin-bottom">
      <FeatherSelect
       class="filter-type-selector"
       label="Search Type" textProp="name"
       :options="InventoryFilterComparator"
       :modelValue="inventoryStore.searchType"
        @update:modelValue="inventoryStore.setSearchType">
      </FeatherSelect>
      <FeatherAutocomplete v-if="inventoryStore.searchType.name === InventoryComparator[InventoryFilter.TAGS]" type="multi"
        :modelValue="inventoryStore.tagsSelected" @update:modelValue="searchNodesByTags"
        :results="tagQueries.tagsSearched" @search="tagQueries.getTagsSearch" class="inventory-auto" label="Search"
        :allow-new="false" textProp="name" render-type="multi" data-test="search-by-tags" ref="searchNodesByTagsRef" />
      <FeatherInput v-if="inventoryStore.searchType.name === InventoryComparator[InventoryFilter.LABELS]" @update:model-value="searchNodesByLabel"
        label="Search" class="inventory-search" data-test="search" ref="searchNodesByLabelRef">
      </FeatherInput>
      <div class="pointer">
        <Icon :icon="filterIcon" />
      </div>
    </li>
    <li class="push-right">
      <InventoryTagManagerCtrl class="tag-manager" data-test="tag-manager-ctrl" />
    </li>
  </ul>
  <div :class="[inventoryStore.isTagManagerOpen ? 'padding' : 'no-padding']">
    <InventoryTagManager :visible="inventoryStore.isTagManagerOpen" />
  </div>
  <div class="margin-bottom">
    <FeatherButton text v-if="tagStore.isTagEditMode" data-test="select-all" @click="inventoryStore.selectAll(nodes)">
      Select All
    </FeatherButton>
    <FeatherButton data-test="clear-selection" text
      v-if="inventoryStore.nodesSelected.length > 0 && inventoryStore.isTagManagerOpen" @click="inventoryStore.clearAll">
      Clear Selection
    </FeatherButton>
  </div>
</template>

<script lang="ts" setup>
import { IIcon, InventoryFilter, NewInventoryNode, fncArgVoid } from '@/types'
import { useInventoryStore } from '@/store/Views/inventoryStore'
import { useTagQueries } from '@/store/Queries/tagQueries'
import { Tag } from '@/types/graphql'
import { PropType } from 'vue'
import { useTagStore } from '@/store/Components/tagStore'
import FilterAlt from '@featherds/icon/action/FilterAlt'
import useSpinner from '@/composables/useSpinner'
import { InventoryComparator } from '../MonitoringPolicies/monitoringPolicies.constants'

const { startSpinner, stopSpinner } = useSpinner()
const inventoryStore = useInventoryStore()
const tagStore = useTagStore()
const tagQueries = useTagQueries()

defineProps({
  onlyTags: {
    type: Boolean,
    default: false
  },
  state: {
    type: String,
    required: true
  },
  nodes: {
    type: Object as PropType<NewInventoryNode[]>,
    required: true
  }
})

const filterIcon: IIcon = {
  image: markRaw(FilterAlt),
  tooltip: 'Node Status',
  size: 1.5,
  cursorHover: true
}

const searchNodesByLabelRef = ref()
const InventoryFilterComparator = [
  { id: 1,  name: InventoryComparator[InventoryFilter.TAGS], value: 'tag'},
  { id: 2, name: InventoryComparator[InventoryFilter.LABELS], value: 'labels'}]
// Current BE setup only allows search by names OR tags.
// so we clear the other search to avoid confusion
const searchNodesByLabel: fncArgVoid = useDebounceFn((val: string | undefined) => {
  if (val) {
    startSpinner()
    inventoryStore.inventoryNodesDefaultFilter.searchValue = val
    inventoryStore.inventoryNodesPagination.page = 0
    inventoryStore.init()
    stopSpinner()
  }
})

const searchNodesByTags: fncArgVoid = (tags: Tag[]) => {
  const str = tags.map(tag => tag.id).join(', ')
  inventoryStore.tagsSelected = tags
  inventoryStore.inventoryNodesDefaultFilter.searchValue = str
  inventoryStore.inventoryNodesPagination.page = 0
  inventoryStore.init()
}
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@/styles/btns.scss';

.filter-container {
  margin: var(variables.$spacing-l) 0;
  display: flex;
  flex-flow: row wrap;
  margin-bottom: 0;
  align-items: center;
  min-height: 55px;

  >* {
    margin-right: var(variables.$spacing-l);
  }

  >.autocomplete {
    min-width: 13rem;
  }

  .or {
    line-height: 2.6;
  }

  :deep(.feather-input-sub-text) {
    display: none;
  }
}

.push-right {
  margin-left: auto;
  margin-right: 0;
}

.flex {
  display: flex;
  align-items: center;
}

.inventory-search {
  min-width: 240px;
  margin-right: 24px;
}

.inventory-auto {
  min-width: 400px;

  :deep(.feather-autocomplete-input) {
    min-width: 100px;
  }

  :deep(.feather-autocomplete-content) {
    display: block;
  }
}

.pointer {
  padding: 7px 9px;
  border-radius: 3px;
  font-size: 1.5rem;
  color: var(variables.$primary);
  cursor: pointer;
  border:1px solid var(--feather-secondary-text-on-surface);
  &:hover {
    cursor: pointer;
    color: var(variables.$disabled-text-on-surface);
  }
}

.margin-bottom {
  margin-bottom: var(variables.$spacing-s);
}

.filter-type-selector {
  margin-right: 12px;
  max-width: 120px;
}
</style>
