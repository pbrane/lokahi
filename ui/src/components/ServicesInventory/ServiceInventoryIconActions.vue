<template>
  <ul :class="[`icon-action-list`, className]">
    <li @click="onEdit" data-test="node-status" class="pointer">
      <Icon :icon="editNodeIcon" />
    </li>
    <li @click="onLineChart" data-test="line-chart" class="pointer">
      <Icon :icon="lineChartIcon" />
    </li>
    <li @click="onDelete" data-test="delete">
      <Icon :icon="deleteIcon" />
    </li>
  </ul>
</template>

<script lang="ts" setup>
import { IIcon } from '@/types'
import { MonitoredEntityState } from '@/types/monitoredEntityState'
import Delete from '@featherds/icon/action/Delete'
import GraphIcon from '@/components/Common/GraphIcon.vue'
import Edit from '@featherds/icon/action/Edit'
import { defineProps, markRaw, PropType } from 'vue'

const props = defineProps({
  service: { type: Object as PropType<MonitoredEntityState>, default: () => ({}) },
  className: { type: String, default: '' },
  ['data-test']: { type: String, default: '' },
  nodeEdit: { type: String, required: false }
})
const onEdit = () => {
  console.log('Edit Action Clicked', props.service)
}

const onLineChart = () => {
  console.log('Line Chart Action Clicked')
}

const onDelete = () => {
  console.log('Delete Action Clicked')
}

const lineChartIcon: IIcon = {
  image: markRaw(GraphIcon),
  tooltip: 'Graphs',
  size: 1.5,
  cursorHover: true
}

const editNodeIcon: IIcon = {
  image: markRaw(Edit),
  tooltip: 'Node Edit',
  size: 1.5,
  cursorHover: true
}

const deleteIcon: IIcon = {
  image: markRaw(Delete),
  tooltip: 'Delete',
  cursorHover: true,
  size: 1.5
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
