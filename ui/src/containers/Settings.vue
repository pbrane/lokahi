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
      <div class="custom-tabs-container">
        <div class="tab-name">
          <ToggleCard
            title="User Management"
            :isActive="activeToggle === 'userManagement'"
            @toggle="() => setActiveToggle('userManagement')"
          />
          <ToggleCard
            title="Third Party Integrations"
            :isActive="activeToggle === 'thirdParty'"
            @toggle="() => setActiveToggle('thirdParty')"
          />
        </div>
        <div class="management-content" v-if="activeToggle === 'userManagement'">
          <UserManagement />
        </div>
        <div class="management-content" v-if="activeToggle === 'thirdParty'">
          Third Party Integrations
        </div>
      </div>
    </section>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'

const activeToggle = ref('userManagement')

const setActiveToggle = (toggle: string) => {
  activeToggle.value = toggle
}
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@/styles/vars';

.custom-tabs-container {
  display: flex;
}

.management-content {
  flex: 1;
  margin-left: 10px;
}
</style>
