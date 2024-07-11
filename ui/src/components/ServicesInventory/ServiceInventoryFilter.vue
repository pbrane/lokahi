<template>
  <ul class="filter-container">
    <li class="autocomplete flex margin-bottom">
      <FeatherSelect
        class="filter-type-selector"
        label="Search Type"
        textProp="name"
        :options="[{ id: 1, name: 'Labels' }, { id: 2, name: 'Tags' }]"
        :modelValue="selectedSearchType"
        @update:modelValue="updateSearchType"
      />
      <FeatherAutocomplete
        v-if="selectedSearchType.name === 'Tags'"
        type="multi"
        class="inventory-auto"
        label="Search"
        :allow-new="false"
        textProp="name"
        render-type="multi"
        data-test="search-by-tags"
      />
      <FeatherInput
        v-if="selectedSearchType.name === 'Labels'"
        label="Search"
        class="inventory-search"
        data-test="search"
      />
      <div class="pointer">
        <Icon :icon="filterIcon" />
      </div>
    </li>
    <li class="push-right">
      <InventoryTagManagerCtrl class="tag-manager" data-test="tag-manager-ctrl" />
    </li>
  </ul>
</template>

<script lang="ts" setup>
import { ref, markRaw } from 'vue'
import { IIcon } from '@/types'
import FilterAlt from '@featherds/icon/action/FilterAlt'
import { SelectItem } from '@/types'

const isSelectItemType = (value: SelectItem): boolean => {
  return typeof value?.id === 'number' && typeof value?.name === 'string'
}

const filterIcon: IIcon = {
  image: markRaw(FilterAlt),
  tooltip: 'Node Status',
  size: 1.5,
  cursorHover: true
}

const selectedSearchType = ref<SelectItem>({ id: 1, name: 'Labels' })

const updateSearchType = (newType: any) => {
  if (isSelectItemType(newType)) {
    selectedSearchType.value = newType
  }
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

  > * {
    margin-right: var(variables.$spacing-l);
  }

  > .autocomplete {
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
  border: 1px solid var(--feather-secondary-text-on-surface);

  &:hover {
    cursor: pointer;
    color: var(variables.$disabled-text-on-surface);
  }
}

.push-right {
  margin-left: auto;
  margin-right: 0;
}

.margin-bottom {
  margin-bottom: var(variables.$spacing-s);
}

.filter-type-selector {
  margin-right: 12px;
  max-width: 120px;
}
:deep(.label-border) {
  width: 69px !important;
}
</style>
