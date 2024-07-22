<template>
  <div class="toggle-card" :class="{ active: isActive }" @click="handleToggle">
    <div class="title">{{ title }}</div>
  </div>
</template>

<script setup lang="ts">
import { defineProps, defineEmits } from 'vue'

defineProps<{
  title: string;
  isActive?: boolean;
}>()

const emit = defineEmits(['toggle'])

const handleToggle = () => {
  emit('toggle')
}
</script>

<style scoped lang="scss">
@use '@featherds/styles/themes/variables';
@use '@/styles/vars.scss';

.toggle-card {
  cursor: pointer;
  padding: 10px;
  border: 1px solid var(variables.$border-on-surface);
  border-radius: vars.$border-radius-surface;
  background-color: var(variables.$surface);
  width: 18vw;
   &.active {
    background-color: var(variables.$primary);
    color: white !important;
   }
}

.title {
  font-weight: bold;
  margin-left: 0 !important;
}
</style>
