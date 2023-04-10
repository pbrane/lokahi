<template>
  <div class="wrapper">
    <div class="header">
      <PageHeadline
        text="Locations"
        class="page-headline"
        data-test="locations-headline"
      />
      <div><AppliancesNotificationsCtrl data-test="locations-notification-ctrl" /></div>
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
        <FeatherInput
          @update:model-value="searchLocationListener"
          label="Search Location"
          type="search"
          class="search-location-input"
          data-test="search-input"
        >
          <template #pre>
            <FeatherIcon :icon="icons.Search" />
          </template>
        </FeatherInput>
        <!-- existing locations list -->
        <!-- to be replaced by location card -->
        <ul>
          <li
            v-for="{ id, location } in locationsList"
            :key="id"
          >
            {{ location }}
          </li>
        </ul>
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
import Search from '@featherds/icon/action/Search'
import { useLocationsStore } from '@/store/Views/locationsStore'

const locationsStore = useLocationsStore()

const locationsList = computed(() => locationsStore.locationsList)

onMounted(async () => {
  await locationsStore.fetchLocations()
})

const addLocation = () => ({})

const searchLocationListener = (val: string | number | undefined) => {
  locationsStore.searchLocation(val as string)
}

const icons = markRaw({
  Add,
  Search
})
</script>

<style lang="scss" scoped>
@use '@/styles/layout/headlineTwoColumns';
</style>
