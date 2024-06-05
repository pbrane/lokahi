<template>
    <div class="window-service">
      <TableCard>
          <div>
              <section class="feather-row">
                  <div class="heading-container">
                    <h3 data-test="heading" class="feather-col-6">Windows Service Summary</h3>
                    <h5>10 Services Discovered</h5>
                  </div>
                  <div class="btns action-container feather-col-6">
                      <div class="search-container">
                          <FeatherInput
                              label="Search Services"
                              type="search"
                          >
                              <template #pre>
                                  <FeatherIcon :icon="Search" />
                              </template>
                          </FeatherInput>
                      </div>
                      <FeatherButton
                          primary
                          icon="Download"
                      >
                          <FeatherIcon :icon="DownloadFile"/>
                      </FeatherButton>
                      <FeatherButton
                          primary
                          icon="Refresh"
                      >
                          <FeatherIcon :icon="Refresh"/>
                      </FeatherButton>
                  </div>
              </section>
          </div>
          <div>
            <FeatherTabContainer
              class="tab-container"
              data-test="tab-container"
            >
            <template v-slot:tabs>
              <FeatherTab>
                Services Running (7/10)
              </FeatherTab>
              <FeatherTab>
                Services Not Running (3/10)
              </FeatherTab>
              <FeatherTab>
                Services Encountering Errors (2)
              </FeatherTab>
            </template>
            <FeatherTabPanel>
              <ServiceTable :serviceType="ServiceType.Running"/>
            </FeatherTabPanel>
            <FeatherTabPanel>
              <ServiceTable :serviceType="ServiceType.NotRunning"/>
            </FeatherTabPanel>
            <FeatherTabPanel>
              <ServiceTable :serviceType="ServiceType.Error"/>
            </FeatherTabPanel>
            </FeatherTabContainer>
          </div>
      </TableCard>
    </div>
  </template>

<script lang="ts" setup>
import DownloadFile from '@featherds/icon/action/DownloadFile'
import Search from '@featherds/icon/action/Search'
import Refresh from '@featherds/icon/navigation/Refresh'
import { ServiceType } from '../utils'
</script>

<style lang="scss" scoped>
@use "@featherds/styles/themes/variables";
@use "@featherds/styles/mixins/typography";

.window-service {
    padding: 0 3rem;
}

.feather-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1rem 0 0.5rem;

  .heading-container{
    margin: 0 0 2.5rem 1rem;
  }
  .action-container {
    display: flex;
    justify-content: flex-end;
    gap: 5px;
    width: 30%;

    > .search-container {
      width: 80%;
      margin-right: 3px;

      :deep(.label-border) {
        width: 90px !important;
      }
    }
  }
}

.tab-container {
  padding: 0 2rem;
}
</style>
