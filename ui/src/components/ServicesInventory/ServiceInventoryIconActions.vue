<template>
    <ul :class="[`icon-action-list`, className]">
      <li @click="onNodeStatus" data-test="node-status" class="pointer">
        <Icon :icon="editNodeIcon" />
      </li>
    </ul>
  </template>

<script lang="ts" setup>
import {markRaw, defineProps, PropType } from 'vue'
import { useRouter } from 'vue-router'
import { IIcon, ServiceInventoryItem } from '@/types'
import Edit from '@featherds/icon/action/Edit'

const router = useRouter()

const props = defineProps({
  node: { type: Object as PropType<ServiceInventoryItem>, default: () => ({}) },
  className: { type: String, default: '' },
  ['data-test']: { type: String, default: '' },
  nodeEdit: { type: String, required: false }
})
const onNodeStatus = () => {
  if (props.node?.id) {
    router.push({
      name: 'Node Status',
      params: { id: props.node.id }
    })
  }
}

const editNodeIcon: IIcon = {
  image: markRaw(Edit),
  tooltip: 'Node Edit',
  size: 1.5,
  cursorHover: true
}

</script>

  <style lang="scss" scoped>
  @use '@featherds/styles/themes/variables';

  .icon-action-list {
    display: flex;
    gap: 0.2rem;

    li {
      padding: var(--feather-spacing-xxs);
      font-size: 1.5rem;
      color: var(--feather-primary);
      cursor: pointer;

      &:hover {
        cursor: pointer;
        color: var(--feather-disabled-text-on-surface);
      }
    }
  }
  </style>
