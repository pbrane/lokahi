<template>
  <div class="full-page-container">
    <div class="">
      <HeadlinePage
        text="Service Inventory"
        class="header"
        data-test="page-header"
      />
    </div>
    <div class="border">
      <div class="table-heading">
        List of all Discovered Services
        <Icon :icon="warningIcon" />
      </div>
      <FeatherTabContainer class="tab-container" data-test="tab-container">
        <template v-slot:tabs>
          <FeatherTab>
            All Services
            <FeatherTextBadge type="info">1</FeatherTextBadge>
          </FeatherTab>
          <FeatherTab>
            Windows Services
            <FeatherTextBadge type="info">0</FeatherTextBadge>
          </FeatherTab>
          <FeatherTab>
            Hosted Services
            <FeatherTextBadge type="info">0</FeatherTextBadge>
          </FeatherTab>
          <FeatherTab>
            Standalone Services
            <FeatherTextBadge type="info">0</FeatherTextBadge>
          </FeatherTab>
        </template>
        <FeatherTabPanel>
          <p>All Services</p>
        </FeatherTabPanel>
        <FeatherTabPanel>
          <p>Windows Services</p>
        </FeatherTabPanel>
        <FeatherTabPanel>
          <p>Hosted Services</p>
        </FeatherTabPanel>
        <FeatherTabPanel>
          <p>Standalone Services</p>
        </FeatherTabPanel>
      </FeatherTabContainer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { IIcon } from '@/types'
import Warning from '@featherds/icon/notification/Warning'
import { FeatherTab, FeatherTabContainer, FeatherTabPanel } from '@featherds/tabs'
import { FeatherTextBadge } from '@featherds/badge'
const warningIcon: IIcon = {
  image: markRaw(Warning),
  title: 'All Network Services',
  tooltip: 'These are list of all Network Services found through Service Discovery',
  size: 1,
  cursorHover: true
}
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@/styles/vars';

.table-heading {
  font-weight: bold;
  font-size: 20px;
  padding-left: 20px;
  margin-top: 40px;
  margin-bottom: 50px;
}
.tab-container {
  :deep(> ul) {
    display: flex;
    border-bottom: 1px solid var(variables.$secondary-text-on-surface);
    min-width: vars.$min-width-smallest-screen;
    > li {
      display: flex !important;
      padding-right: 5px;
      > button {
        display: flex;
        flex-grow: 1;
        > span {
          flex-grow: 1;
        }
      }
    }
    li {
      :deep(.tab.selected) {
        background: none !important;
      }
    }
  }
}

.border {
  border: 1px solid var(variables.$shade-3);
}

</style>
