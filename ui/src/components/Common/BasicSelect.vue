<template>
  <FeatherSelect
    label=""
    :options="list"
    text-prop="name"
    v-model="selectedItem"
    hideLabel
    :disabled="isDisabled"
    @update:modelValue="setSelectedItem"
  >
    <template #pre v-if="icon">
       <FeatherIcon :icon="Search"/>
    </template>
  </FeatherSelect>
</template>

<script lang="ts" setup>
import { ISelectItemType } from '@featherds/select/src/components/types'
import Search from '@featherds/icon/action/Search'

const emit = defineEmits(['item-selected'])

const props = defineProps<{
  list: ISelectItemType[] // accept the structure [{id, name}]
  isDisabled?: boolean
  selectedId?: string,
  icon?: boolean
}>()

const selectedItem = ref(props.list[0])

const setSelectedItem = (selected: ISelectItemType | undefined) => {
  emit('item-selected', selected?.id)
}

// set selected by passing in an id
watchEffect(() => {
  selectedItem.value = props.list[0]

  if (props.selectedId) {
    for (const item of props.list) {
      if (item.id === props.selectedId) {
        selectedItem.value = item
      }
    }
  }
})
</script>

<style scoped lang="scss">
:deep(.label-border) {
  width: 0 !important;
}

// allows for smaller select
:deep(.feather-select-input) {
  width: 100%;
  min-width: 60px;
}
:deep(.feather-input-wrapper) {
  flex-flow: nowrap;
}
</style>
