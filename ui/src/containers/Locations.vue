<template>
  <div class="wrapper">
    <div class="header">
      <PageHeadline
        text="Locations"
        class="page-headline"
      />
      <div><AppliancesNotificationsCtrl /></div>
    </div>
    <div class="content">
      <div class="content-left">
        <FeatherButton
          primary
          @click="addLocation"
          data-test="add-location-btn"
        >
          <FeatherIcon :icon="icons.Add" />
          Location
        </FeatherButton>
        <hr />
        <!-- search input -->
        <!-- existing locations list -->
      </div>
      <div class="content-right">
        <!-- minions list of a location -->
        <!--  header -->
        <!--  card list -->
        <!-- OR -->
        <!-- location add form -->
        <!--  header -->
        <!--  inputs, actions -->
        <!--  footer: cancel, save -->
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import Add from '@featherds/icon/action/Add'

const addLocation = () => ({})

const icons = markRaw({
  Add
})
</script>

<style lang="scss" scoped>
@use '@/styles/layout/headlineTwoColumns';
</style>
