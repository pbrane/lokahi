<template>
  <div class="empty">
    <div class="icon">
      <slot name="icon"></slot>
    </div>
    <div class="title">{{ texts.emptyTitle }}</div>
    <div class="subtitle">{{ texts.emptySubtitle }}</div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  texts: Record<string, string>
}>()
</script>

<style scoped lang="scss">
@use '@featherds/styles/mixins/typography';
@use '@featherds/styles/themes/variables';
@use '@/styles/mediaQueriesMixins.scss';

.empty {
  text-align: center;
  padding-top: 0;
  @include mediaQueriesMixins.screen-md {
    padding: var(variables.$spacing-xxl) 0;
  }
  .icon {
    width: 144px;
    height: 144px;
    display: inline-flex;
    margin-bottom: var(variables.$spacing-l);
  }
  > .title {
    @include typography.headline4();
  }
}
</style>
