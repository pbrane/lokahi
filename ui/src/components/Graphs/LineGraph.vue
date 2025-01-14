<template>
  <div v-if="graphs.dataSets.value.length" :class="isCpuOrMemoryUtilizationGraph ? 'utilization-container' : 'container'">
    <div class="canvas-wrapper">
      <FeatherTooltip title="Download to PDF" v-slot="{ attrs, on }" v-if="!isCpuOrMemoryUtilizationGraph">
        <FeatherButton v-bind="attrs" v-on="on" icon="Download" class="download-icon" @click="onDownload">
          <FeatherIcon :icon="DownloadFile" />
        </FeatherButton>
      </FeatherTooltip>
      <canvas class="canvas" :id="`${graph.label}`"></canvas>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useGraphs } from '@/composables/useGraphs'
import { ChartData, ChartOptions, TitleOptions } from 'chart.js'
import Chart from 'chart.js/auto'
// import zoomPlugin from 'chartjs-plugin-zoom'
import useTheme from '@/composables/useTheme'
import { GraphProps } from '@/types/graphs'
import DownloadFile from '@featherds/icon/action/DownloadFile'
import 'chartjs-adapter-date-fns'
import { add, format } from 'date-fns'
import { PropType } from 'vue'
import { downloadCanvas } from './utils'
import { getChartGridColor, getColorFromFeatherVar, humanFileSizeFromBits } from '../utils'
import { GRAPH_TYPE_CPU_UTILIZATION, GRAPH_TYPE_MEMORY_UTILIZATION } from '../NodeStatus/NodeStatus.constants'

const emits = defineEmits(['has-data'])
const graphs = useGraphs()
const { onThemeChange, isDark } = useTheme()
const props = defineProps({
  graph: {
    required: true,
    type: Object as PropType<GraphProps>
  },
  type: { type: String, default: 'latency' },
  metricType: { type: String }
})
const isCpuOrMemoryUtilizationGraph = computed(() => props.metricType === GRAPH_TYPE_CPU_UTILIZATION || props.metricType === GRAPH_TYPE_MEMORY_UTILIZATION)

const colorFromFeatherVar = computed(() =>
  isDark.value ? getColorFromFeatherVar('primary-text-on-color') : getColorFromFeatherVar('primary-text-on-surface')
)
let chart: any = {}
const formatAxisBasedOnType = (context: number) => {
  let formattedAxis = context.toFixed(1)
  if (props.type === 'bytes') {
    formattedAxis = humanFileSizeFromBits(context)
  } else if (props.type === 'percentage') {
    formattedAxis = isCpuOrMemoryUtilizationGraph.value ? (context).toFixed(2) + '%' : (context * 100).toFixed(2) + '%'
  }
  return formattedAxis
}
const options = computed<ChartOptions<any>>(() => ({
  responsive: true,
  aspectRatio: 1.4,
  plugins: {
    legend: {
      labels: {
        color: colorFromFeatherVar.value
      }
    },
    title: {
      display: true,
      text: props.graph.label,
      color: colorFromFeatherVar.value
    } as TitleOptions,
    zoom: {
      zoom: {
        wheel: {
          enabled: true
        },
        mode: 'x'
      },
      pan: {
        enabled: true,
        mode: 'x'
      }
    },
    tooltip: {
      usePointStyle: true,
      callbacks: {
        label: (context: any) => {
          return ` ${context.label} - ${formatAxisBasedOnType(context.parsed.y)}`
        },
        title: () => {
          return ''
        }
      }
    }
  },
  scales: {
    y: {
      title: {
        display: false,
        text: props.graph.label
      } as TitleOptions,
      ticks: {
        callback: (value: any) => (formatAxisBasedOnType(value)),
        maxTicksLimit: 8,
        color: colorFromFeatherVar.value
      },
      grid: {
        display: true,
        color: getChartGridColor(isDark.value)
      },
      stacked: false
    },
    x: {
      type: 'time',
      time: {
        tooltipFormat: 'kk:mm',
        parser: (val: number) => val * 1000 // convert to miliseconds before applying format
      },
      ticks: {
        callback: (val: number) => format(new Date(val), 'kk:mm'),
        maxTicksLimit: 12,
        color: colorFromFeatherVar.value
      },
      grid: {
        display: true,
        color: getChartGridColor(isDark.value)
      }
    }
  }
}))
const xAxisLabels = computed(() => {
  const graphsDataSetsValues = (graphs.dataSets.value[0]?.values as any) || ([] as any)

  // if minion down, this tracks the gap between the previous point and now
  const date = add(new Date(), { minutes: 2 })
  graphsDataSetsValues.push([date.getTime() / 1000, 0])

  return graphsDataSetsValues.map((val: number[]) => val[0])
})
const dataSets = computed(() => {
  const bgColor = ['green', 'blue'] // TODO: find solution to set bg color, in regards to FeatherDS theme switching
  emits('has-data', graphs.dataSets.value.length)
  return graphs.dataSets.value.map((data: any, i) => ({
    label: props.graph.metrics[i],
    data: data.values.map((val: any) => val[1]),
    backgroundColor: bgColor[i],
    borderColor: bgColor[i],
    hitRadius: 5,
    hoverRadius: 6,
    // hides the last point, which tracks the gap between the previous point and now
    pointRadius: Array.from(Array(xAxisLabels.value.length).keys()).map((_, index) => xAxisLabels.value.length - 1 === index ? 0 : 3),
    spanGaps: 1000 * 60 * 2 // no line over 2+ minute gaps
  }))
})
const chartData = computed<ChartData<any>>(() => {
  return {
    labels: xAxisLabels.value,
    datasets: dataSets.value
  }
})
const render = async (update?: boolean) => {
  try {
    if (isCpuOrMemoryUtilizationGraph.value) {
      options.value.scales.y.max = 100
    }
    if (update || chart?.update) {
      chart.data = chartData.value
      chart.options = options.value
      chart.update()
    } else {
      if (chartData.value.datasets.length) {
        const ctx: any = document.getElementById(`${props.graph.label}`)
        chart = new Chart(ctx, {
          type: 'line',
          data: chartData.value,
          options: options.value,
          plugins: []
        })
      }
    }
  } catch (error) {
    console.log(error)
    console.log('Could not render graph for ', props.graph.label)
  }
}
const onDownload = () => {
  const canvas = document.getElementById(props.graph.label) as HTMLCanvasElement
  downloadCanvas(canvas, props.graph.label)
}
const loadMetricDataAndRenderGraph = async (update?: boolean) => {
  await graphs.getMetrics(props.graph)
  render(update)
}
watch([props, isDark], () => {
  if (props.graph.metrics) {
    loadMetricDataAndRenderGraph()
  }
})
onMounted(() => {
  loadMetricDataAndRenderGraph()
})
onThemeChange(() => {
  options.value.scales.x.ticks.color = colorFromFeatherVar.value
  options.value.scales.y.ticks.color = colorFromFeatherVar.value
  render(true)
})
defineExpose({ onDownload, loadMetricDataAndRenderGraph })
</script>

<!-- TODO: make theme switching works in graphs -->
<style scoped lang="scss">
@use '@featherds/styles/themes/variables';

.container {
  position: relative;
  width: 30%;
  min-width: 380px;
  border: 1px solid var(variables.$secondary-text-on-surface);
  border-radius: 10px;
  padding: var(variables.$spacing-m);

  .canvas-wrapper {
    width: 100%;

    .download-icon {
      position: absolute;
      right: 15px;
      top: 19px;

      svg {
        font-size: 15px;
      }
    }
  }
}

.utilization-container {
  padding: var(variables.$spacing-m);
  width: 100vw;
  .canvas-wrapper {
    width: 100%;

    .canvas {
      width: 100% !important;
      height: 100% !important;
    }
  }
}
</style>
