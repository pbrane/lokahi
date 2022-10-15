<template>
  <div class="topology-side-controls">
    <div class="controls">
      <CtrlSemanticZoomLevel />

      <FeatherButton
        class="refresh-btn"
        icon="Refresh"
        @click="refreshGraph"
      >
        <FeatherIcon :icon="RefreshIcon" />
      </FeatherButton>

      <CtrlLayers />
      <CtrlHighlightFocusedNode />
    </div>
    <TopologyRightDrawer />
  </div>
</template>

<script
  setup
  lang="ts"
>
import CtrlSemanticZoomLevel from './CtrlSemanticZoomLevel.vue'
import CtrlHighlightFocusedNode from './CtrlHighlightFocusedNodes.vue'
import CtrlLayers from './CtrlLayers.vue'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import RefreshIcon from '@featherds/icon/navigation/Refresh'
import TopologyRightDrawer from './TopologyRightDrawer.vue'
import { PropType } from 'vue'
import { useTopologyStore } from '@/store/Views/topologyStore'

const topologyStore = useTopologyStore()

defineProps({
  refreshGraph: {
    required: true,
    type: Function as PropType<(payload: MouseEvent) => void>
  }
})

const graphsSubLayers = computed(() => topologyStore.topologyGraphsSubLayers)
const width = computed<string>(() => topologyStore.isRightDrawerOpen ? '250px' : '65px')

watch(graphsSubLayers, (gsl) => {
  topologyStore.isRightDrawerOpen = Boolean(gsl.length > 1)
})
</script>

<style
  scoped
  lang="scss"
>
@use "@featherds/styles/themes/variables";
@use "@featherds/styles/mixins/elevation";

.topology-side-controls {
  @include elevation.elevation(2);
  display: flex;
  height: auto;
  padding-bottom: 20px;
  padding-top: 20px;
  width: v-bind(width);
  position: fixed;
  top: 62px;
  right: 0;

  .controls {
    display: block;
    width: 75px;
    border-right: 1px solid var(variables.$primary);
    padding-right: 15px;
  }
}
</style>

<style lang="scss">
// TODO: scope the following
.topology-side-controls {
  .btn {
    margin: 0px 0px 0px 15px !important;
  }

  .chip {
    margin: 0px 0px 0px 12px !important;
  }

  .refresh-btn {
    margin-top: 5px !important;
  }
}
</style>
