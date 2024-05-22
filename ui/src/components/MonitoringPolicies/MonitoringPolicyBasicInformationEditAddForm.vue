<template>
  <div class="page-headline3">Name & Description</div>
  <p class="page-sub-headline">
    Create a clear and descriptive name for the monitoring policy
  </p>
  <div v-if="store.selectedPolicy" class="policy-form">
    <FeatherInput
      v-model.trim="store.selectedPolicy.name"
      label="New Policy Name"
      v-focus
      data-test="policy-name-input"
      :error="store.validationErrors.policyName"
      :readonly="store.selectedPolicy.isDefault"
      class="policy-name"
    />
    <FeatherTextarea
      v-model.trim="store.selectedPolicy!.memo"
      label="Description" :maxlength="100"
      :disabled="store.selectedPolicy.isDefault"
    />
    <FeatherCheckboxGroup
      label="Notifications (Optional)"
      vertical
    >
      <FeatherCheckbox
        v-model="store.selectedPolicy!.notifyByEmail"
        :disabled="store.selectedPolicy.isDefault"
      >
        Email
      </FeatherCheckbox>
      <FeatherCheckbox
        v-model="store.selectedPolicy!.notifyByPagerDuty"
        :disabled="store.selectedPolicy.isDefault"
      >
        PagerDuty
      </FeatherCheckbox>
    </FeatherCheckboxGroup>
    <div class="subtitle">Tags</div>
    <BasicAutocomplete
      @itemsSelected="selectTags"
      :getItems="tagQueries.getTagsSearch"
      :label="'Tag name'"
      :items="tagQueries.tagsSearched"
      :preselectedItems="formattedTags"
    />
  </div>
</template>
<script setup lang="ts">
import { useTagQueries } from '@/store/Queries/tagQueries'
import { useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'
import { TagSelectItem } from '@/types'

const store = useMonitoringPoliciesStore()
const tagQueries = useTagQueries()

const selectTags = (tags: TagSelectItem[]) => (store.selectedPolicy!.tags = tags.map((tag) => tag.name))
const formattedTags = computed(() => store.selectedPolicy?.tags?.map((tag: string) => ({ name: tag, id: tag })) || [])
</script>
<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@/styles/vars.scss';

.policy-name {
  margin-top: 5px
}

.page-headline1 {
  @include typography.headline1;
}

.page-headline2 {
  @include typography.headline2;
}

.page-headline3 {
  @include typography.headline3;
}

.page-sub-headline {
    padding: 5px 0 15px 0;
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

.subtitle {
  padding-bottom: 1rem;
}
</style>
