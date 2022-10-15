<template>
  <FeatherAppLayout contentLayout="full" class="feather-styles layout">
    <template v-slot:header>
      <Menubar />
    </template>

    <template v-slot:rail>
      <NavigationRail />
    </template>
    
    <div class="content-and-widget">
      <div id="mainContent" class="main-content">
        <Spinner />
        <Snackbar />
        <router-view />
      </div>
      <transition name="fade">
        <Widgetbar v-if="store.widgetBarOpen" />
      </transition>
    </div>
  </FeatherAppLayout>
</template>
  
<script setup lang="ts">
import { useLayoutStore } from './store/Views/layoutStore'

// Remove KC redirectUri theme param
const route = useRoute()
const router = useRouter()
if (route.query.theme) router.replace(route.path)

const store = useLayoutStore()
</script>
  
<style lang="scss">
@use "@featherds/styles/themes/variables";
@use "@featherds/styles/themes/open-mixins";

html {
  overflow-x: hidden;
}
body {
  background: var(variables.$background);
  margin: 0;
}
.open-light {
  @include open-mixins.open-light;
}
.open-dark {
  @include open-mixins.open-dark;
}
</style>

<style lang="scss" scoped>
.content-and-widget {
  display: flex;
}
.main-content {
  width: 100%;
}
:deep(.feather-app-rail) {
  border-right: 0 !important;
}
</style>
