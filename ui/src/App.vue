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
@use "@/styles/_transitionFade";

@import "@featherds/styles/lib/grid";
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/open-mixins";
@import "@featherds/table/scss/table";
@import "@featherds/styles/themes/variables";

html {
  overflow-x: hidden;
}

body {
  background: var($background);
  margin: 0
}

.open-light {
  @include open-light;
}
.open-dark {
  @include open-dark;
}

.content-and-widget {
  display: flex;
}

.main-content {
  width: 100%;

  table {
    width: 100%;
    @include table;
  }

  .data-table tr,
  .data-table div {
    @for $i from 1 through 50 {
    &:nth-child(#{$i}) {
      transition: all 0.3s ease $i * 0.05s;
    }
    }
  }
  .data-table-enter-active,
  .data-table-leave-active {
    transform: translateX(0px);
    opacity:1;
  }
  .data-table-enter-from,
  .data-table-leave-to {
    transform: translateX(30px);
    opacity:0;
  }
}

a {
  text-decoration: none;
  color: var($primary);
}

.pointer {
  cursor: pointer;
}
 </style>

<style scoped lang="scss">
:deep(.feather-app-rail) {
  border-right: 0 !important;
}
</style>
