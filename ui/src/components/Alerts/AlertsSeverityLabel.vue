<template>
  <span
    v-if="severity"
    class="severity-status"
    :class="[`${severity.toLowerCase()}-color`]"
    data-test="severity-status"
    >{{ severity }}</span
  >
</template>

<script lang="ts" setup>
defineProps<{
  severity?: string
}>()
</script>

<style lang="scss" scoped>
@use '@featherds/styles/mixins/typography';
@use '@/styles/vars.scss';
@use '@/styles/_severities';

.severity-status {
  @include typography.title();
  display: inline-block;
  font-size: 1rem;
  line-height: normal;
  padding: 2px 8px;
  border-radius: vars.$border-radius-sm;
}
</style>
