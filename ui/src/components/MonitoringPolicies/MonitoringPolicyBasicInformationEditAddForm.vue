<template>
  <div class="page-headline3">Name & Description</div>
  <p>
    Create a clear and descriptive name for the monitoring policy
  </p>
  <div class="policy-form">

    <FeatherInput v-model.trim="store.selectedPolicy!.name" label="New Policy Name" v-focus
      data-test="policy-name-input" :error="store.validationErrors.policyName" />
    <FeatherTextarea v-model.trim="store.selectedPolicy!.memo" label="Description" :maxlength="100" />
    <FeatherCheckboxGroup label="Notifications (Optional)" vertical>
      <FeatherCheckbox v-model="store.selectedPolicy!.notifyByEmail">Email</FeatherCheckbox>
      <FeatherCheckbox v-model="store.selectedPolicy!.notifyByPagerDuty">PagerDuty</FeatherCheckbox>
    </FeatherCheckboxGroup>
    <div class="subtitle">Tags</div>
    <BasicAutocomplete @itemsSelected="selectTags" :getItems="tagQueries.getTagsSearch"
      :label="'Tag name'" :items="tagQueries.tagsSearched" />
  </div>
</template>
<script setup lang="ts">
import { useTagQueries } from '@/store/Queries/tagQueries'
import { useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'
import { TagSelectItem } from '@/types';

const store = useMonitoringPoliciesStore()
const tagQueries = useTagQueries()

const selectTags = (tags: TagSelectItem[]) => (store.selectedPolicy!.tags = tags.map((tag) => tag.name))

// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
const formattedTags = computed(() => store.selectedPolicy!.tags!.map((tag: string) => ({ name: tag, id: tag })))
</script>
<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@/styles/vars.scss';

.page-headline1 {
  @include typography.headline1;
}

.page-headline2 {
  @include typography.headline2;
}

.page-headline3 {
  @include typography.headline3;
}

.mt-2 {
  margin-top: var(variables.$spacing-m);
}

.p-2 {
  padding: var(variables.$spacing-m);
}

.d-flex {
  display: flex;
}

.justify-content-center {
  justify-content: center;
}

.justify-content-end {
  justify-content: end;
}
</style>
