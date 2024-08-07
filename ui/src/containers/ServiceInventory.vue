<template>
  <div class="full-page-container">
    <div class="flex">
      <HeadlinePage
        text="Service Inventory"
        class="header"
        data-test="page-header"
      />
      <FeatherButton secondary>
        IMPORT NODES
      </FeatherButton>
    </div>
      <div class="table-heading">
        List of all Discovered Services
        <Icon :icon="warningIcon" />
      </div>
      <FeatherTabContainer class="tab-container" data-test="tab-container">
        <template v-slot:tabs>
          <FeatherTab>
            Network Services
            <FeatherTextBadge type="info">{{ store.pagination?.total ?? 0 }}</FeatherTextBadge>
          </FeatherTab>
          <FeatherTab v-if="!componentsUnderDevelopment">
            Windows Services
            <FeatherTextBadge type="info">(24)</FeatherTextBadge>
          </FeatherTab>
          <FeatherTab v-if="!componentsUnderDevelopment">
            Hosted Services
            <FeatherTextBadge type="info">(34)</FeatherTextBadge>
          </FeatherTab>
          <FeatherTab v-if="!componentsUnderDevelopment">
            Standalone Services
            <FeatherTextBadge type="info">(32)</FeatherTextBadge>
          </FeatherTab>
        </template>
        <FeatherTabPanel>
          <div class="tab-navigation" v-if="!componentsUnderDevelopment">
            <p class="search-node"> Search by name/tag or use additional filters to filter nodes </p>
            <div class="tab-menu">
              <FeatherButton secondary :class="{ 'active-tab': view === 'table' }" @click="toggleView('table')" :active="view === 'table'">
                Table
              </FeatherButton>
              <FeatherButton secondary :class="{ 'active-tab': view === 'card' }" @click="toggleView('card')" :active="view === 'card'">
                Card
              </FeatherButton>
            </div>
          </div>
          <ServiceInventoryFilter v-if="!componentsUnderDevelopment"/>
          <EmptyList
            data-test="unmonitored-empty"
            v-if="!store.monitoredEntityStatesList.length && !store.loading"
            bg
            :content="{
              msg: 'No services available. Add some on the Discovery page.',
              btn: {
                label: 'Visit Discovery Page',
                action: () => {
                  $router.push('/discovery')
                }
              }
            }"
          />
          <NetworkServices />
        </FeatherTabPanel>
        <FeatherTabPanel v-if="!componentsUnderDevelopment">
          <div class="tab-navigation">
            <p class="search-node"> Search by name/tag or use additional filters to filter nodes </p>
            <div class="tab-menu">
              <FeatherButton secondary :class="{ 'active-tab': view === 'table' }" @click="toggleView('table')" :active="view === 'table'">
                Table
              </FeatherButton>
              <FeatherButton secondary :class="{ 'active-tab': view === 'card' }" @click="toggleView('card')" :active="view === 'card'">
                Card
              </FeatherButton>
            </div>
          </div>
          <ServiceInventoryFilter />
          <NetworkServices :tabContent="tabContentOfAllServices" :columns="columns"/>
        </FeatherTabPanel>
        <FeatherTabPanel v-if="!componentsUnderDevelopment">
          <div class="tab-navigation">
            <p class="search-node"> Search by name/tag or use additional filters to filter nodes </p>
            <div class="tab-menu">
              <FeatherButton secondary :class="{ 'active-tab': view === 'table' }" @click="toggleView('table')" :active="view === 'table'">
                Table
              </FeatherButton>
              <FeatherButton secondary :class="{ 'active-tab': view === 'card' }" @click="toggleView('card')" :active="view === 'card'">
                Card
              </FeatherButton>
            </div>
          </div>
          <ServiceInventoryFilter />
          <NetworkServices :tab-content="tabContentOfHostedServices" :columns="columns"/>
        </FeatherTabPanel>
        <FeatherTabPanel v-if="!componentsUnderDevelopment">
          <div class="tab-navigation">
            <p class="search-node"> Search by name/tag or use additional filters to filter nodes </p>
            <div class="tab-menu">
              <FeatherButton secondary :class="{ 'active-tab': view === 'table' }" @click="toggleView('table')" :active="view === 'table'">
                Table
              </FeatherButton>
              <FeatherButton secondary :class="{ 'active-tab': view === 'card' }" @click="toggleView('card')" :active="view === 'card'">
                Card
              </FeatherButton>
            </div>
          </div>
          <ServiceInventoryFilter />
          <NetworkServices :tab-content="tabContentOfStandAloneServices" :columns="columns"/>
        </FeatherTabPanel>
      </FeatherTabContainer>
    </div>
</template>

<script setup lang="ts">
import { tabContentOfAllServices, tabContentOfHostedServices, tabContentOfStandAloneServices } from '@/components/ServicesInventory/MockData'
import { columns } from '@/components/ServicesInventory/serviceinventory.constants'
import { useServiceInventoryStore } from '@/store/Views/serviceInventoryStore'
import { IIcon } from '@/types'
import { FeatherTextBadge } from '@featherds/badge'
import Warning from '@featherds/icon/notification/Warning'
import { FeatherTab, FeatherTabContainer, FeatherTabPanel } from '@featherds/tabs'
import { markRaw, ref } from 'vue'

const store = useServiceInventoryStore()

const view = ref('card')

const componentsUnderDevelopment = ref(true)

const warningIcon: IIcon = {
  image: markRaw(Warning),
  title: 'All Network Services',
  tooltip: 'These are list of all Network Services found through Service Discovery',
  size: 1,
  cursorHover: true
}

const toggleView = (selectedView: string) => {
  view.value = selectedView
}
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@/styles/vars.scss';

.full-page-container {
  .table-heading {
    font-weight: bold;
    font-size: 20px;
    margin-bottom: 50px;
  }

  .flex {
    display: flex;
    width: 100%;
    justify-content: space-between;
    align-items: center;
  }

  .tab-container {
    :deep(> ul) {
      display: flex;
      border-bottom: 1px solid var(--feather-secondary-text-on-surface);
      min-width: var(--feather-min-width-smallest-screen);
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
    .tab-navigation {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .search-node {
        font-weight: bold;
        margin-bottom: var(variables.$spacing-s);
      }

      .tab-menu {
        display: flex;
        align-items: center;
        justify-content: flex-end;
        .active-tab {
          background-color: var(variables.$primary);
          color: white !important;
        }
      }
    }
  }
}
</style>
