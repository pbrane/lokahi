<template>
  <p class="title">Horizon Stream</p>
  <p class="welcome" v-if="keycloak?.authenticated">Welcome, {{ keycloak.tokenParsed?.preferred_username }}</p>
  <AlarmsTable />
</template>

<script setup lang="ts">
import useKeycloak from '@/composables/useKeycloak'
const { keycloak } = useKeycloak()
</script>

<style scoped lang="scss">
@use "@featherds/styles/mixins/typography";

p {
  text-align: center;
}
.title {
  @include typography.headline2;
  margin-top: 10px;
}
.welcome {
  @include typography.headline4;
}
</style>
