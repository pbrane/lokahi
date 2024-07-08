<template>
  <div class="full-page-container">
    <div>
      <HeadlinePage
        text="Settings"
        class="header"
        data-test="page-header"
      />
    </div>
    <section>
    <FeatherTabContainer>
      <template v-slot:tabs>
        <FeatherTab>User Management</FeatherTab>
        <FeatherTab>Integrations</FeatherTab>
      </template>
      <FeatherTabPanel>
        <UserManagement />
      </FeatherTabPanel>
      <FeatherTabPanel>Third Party Integrations</FeatherTabPanel>
    </FeatherTabContainer>
  </section>
  </div>
</template>

<script lang="ts" setup>
import { FeatherTab, FeatherTabContainer, FeatherTabPanel } from '@featherds/tabs'
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@/styles/vars';

</style>
