<template>
  <div
    class="drawer-btn"
    v-if="!isOpen"
  >
    <FeatherButton
      icon="open"
      @click="openLeftDrawer"
    >
      <FeatherIcon :icon="ChevronRight" />
    </FeatherButton>
  </div>
</template>

<script
  setup
  lang="ts"
>
import { FeatherIcon } from '@featherds/icon'
import { FeatherButton } from '@featherds/button'
import ChevronRight from '@featherds/icon/navigation/ChevronRight'
import { useTopologyStore } from '@/store/Views/topologyStore'

const topologyStore = useTopologyStore()
const isOpen = computed<boolean>(() => topologyStore.isLeftDrawerOpen)
const openLeftDrawer = () => topologyStore.isLeftDrawerOpen = true
</script>

<style
  scoped
  lang="scss"
>
@use "@featherds/styles/mixins/elevation";

.drawer-btn {
  @include elevation.elevation(2);
  position: absolute;
  z-index: 1000;
  border-top-right-radius: 5px;
  border-bottom-right-radius: 5px;
  button {
    margin: 5px;
  }
}
</style>
