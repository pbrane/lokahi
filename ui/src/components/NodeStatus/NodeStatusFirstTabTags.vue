<template>
  <div class="tab-tags-container border">
    <div class="title-btn-wrapper">
      <div class="title">Tags</div>
      <FeatherButton
        text
        @click="openModal"
      >
        Manage
      </FeatherButton>
    </div>
    <FeatherChipList label="Tags">
      <FeatherChip
        v-for="tag in tags"
        :key="tag.id"
      >
        {{ tag.name }}
      </FeatherChip>
    </FeatherChipList>
  </div>
</template>

<script setup lang="ts">
import { Tag } from '@/types/graphql'
defineProps<{
  tags: Tag[]
  openModal: () => void
}>()
</script>

<style scoped lang="scss">
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';

.tab-tags-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  background: var(variables.$surface);
  padding: 30px 25px;
  border-radius: 4px;

  .title-btn-wrapper {
    display: flex;
    justify-content: space-between;
    .title {
      @include typography.headline3;
    }
  }
}
</style>
