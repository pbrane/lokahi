<template>
  <div>
    <section class="node-component-header">
      <h3 data-test="heading" class="node-label">{{ title }}</h3>
      <div class="actions">
        <FeatherTooltip title="Download to PDF" v-slot="{ attrs, on }">
          <FeatherButton
            v-bind="attrs"
            v-on="on" icon="Download"
            class="download-icon"
            @click="graphRef.onDownload()"
          >
            <FeatherIcon :icon="DownloadFile" />
          </FeatherButton>
        </FeatherTooltip>
        <FeatherTooltip title="Refresh" v-slot="{ attrs, on }">
          <FeatherButton
            v-bind="attrs"
            v-on="on"
            icon="Refresh"
            class="refresh-icon"
            @click="graphRef.loadMetricDataAndRenderGraph(true)"
          >
            <FeatherIcon :icon="Refresh" />
          </FeatherButton>
        </FeatherTooltip>
      </div>
    </section>
    <section class="node-component-content">
      <LineGraph
        ref="graphRef"
        :graph="graphProps"
        :metricType="$props.type"
        type="percentage"
        @has-data="displayEmptyMsgIfNoData"
      />
    </section>
  </div>
</template>

<script lang="ts" setup>
import { TimeRangeUnit } from '@/types/graphql'
import DownloadFile from '@featherds/icon/action/DownloadFile'
import Refresh from '@featherds/icon/navigation/Refresh'
import { GraphProps } from '@/types/graphs'

const hasMetricData = ref(false)
const graphRef = ref()
const route = useRoute()
const props = defineProps<{
  title: string
  type: string
}>()

const graphProps = computed<GraphProps>(() => {
  // TODO: once we start getting actual metric data graph props needs to change
  return {
    label: props.title,
    // TODO: This here is a temporary metric for cpu and memory we still need to configure the actual one
    metrics: props.type === 'cpu' ? ['CpuRawIdle'] : ['Memory'],
    monitor: 'SNMP',
    nodeId: route.params.id as string,
    instance: '',
    timeRange: 10,
    timeRangeUnit: TimeRangeUnit.Minute,
    ifName: ''
  }
})

const displayEmptyMsgIfNoData = (hasData: boolean) => {
  if (hasMetricData.value) {
    return
  }
  hasMetricData.value = hasData
}
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@/styles/vars';
@use '@/styles/mediaQueriesMixins';
@use '@featherds/styles/mixins/typography';

.node-component-header {
  padding: var(variables.$spacing-m);
  display: flex;
  flex-direction: row;
  gap: 0.5rem;
  align-items: center;
  justify-content: space-between;
}

.node-component-label {
  margin: 0;
  line-height: 20px;
  letter-spacing: 0.28px;
}

.node-component-content {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  gap: 2rem;
}
</style>
