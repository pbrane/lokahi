<template>
  <FeatherAppLayout
    contentLayout="full"
    class="feather-styles layout"
    v-if="ready"
  >
    <template v-slot:header>
      <Menubar />
    </template>

    <template v-slot:rail>
      <NavigationRail />
    </template>

    <div id="mainContent">
      <Spinner />
      <Snackbar />
      <router-view />
    </div>
  </FeatherAppLayout>
</template>

<script setup lang="ts">
import { useWelcomeStore } from '@/store/Views/welcomeStore'
const router = useRouter()
const welcomeStore = useWelcomeStore()
const ready = ref(false)

onBeforeMount(async () => {
  await welcomeStore.getShowOnboardingState()
  if (welcomeStore.showOnboarding) await router.push('Welcome')
  ready.value = true
})
</script>

<style lang="scss" scoped>
:deep(.feather-app-rail) {
  border-right: 0 !important;
}
</style>

<style lang="scss">
@use '@/styles/_app';
</style>
