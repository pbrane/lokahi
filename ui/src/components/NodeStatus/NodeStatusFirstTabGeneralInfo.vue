<template>
  <div class="general-info-container border">
    <div class="title">General Information</div>

    <div class="row">
      <div>Monitoring Location</div>
      <div class="subtitle">{{ node.location?.location }}</div>
    </div>

    <div class="row">
      <div>System Name</div>
      <div class="subtitle">{{ node.systemName }}</div>
    </div>

    <div class="row">
      <div>System Location</div>
      <div class="subtitle">{{ node.systemLocation }}</div>
    </div>

    <div class="row">
      <div>Contact</div>
      <div class="subtitle">{{ node.systemContact }}</div>
    </div>

    <div class="row">
      <div>System Description</div>
      <div class="subtitle">{{ node.systemDescr }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Node } from '@/types/graphql'
defineProps<{
  node: Node
}>()
</script>

<style scoped lang="scss">
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';

.general-info-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  background: var(variables.$surface);
  padding: 30px 25px;
  border-radius: 4px;

  .title {
    @include typography.headline3;
  }

  .row {
    display: flex;
    justify-content: space-between;

    .subtitle {
      @include typography.subtitle1;
    }
  }
}
</style>
