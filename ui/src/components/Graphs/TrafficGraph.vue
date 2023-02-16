<template>
  <div class="container">
    <div class="canvas-wrapper">
      <canvas class="canvas" :id="`${label}`"></canvas>
    </div>
  </div>
</template>

<script setup lang="ts">
import _ from 'underscore'
import { useGraphs } from '@/composables/useGraphs'
import { ChartOptions, TitleOptions, ChartData } from 'chart.js'
import Chart from 'chart.js/auto'
import { PropType } from 'vue'
import { formatTimestamp, downloadCanvas } from './utils'
import { format } from 'd3'
import {FlowingPoint} from '@/types/graphql'

const graphs = useGraphs()

const props = defineProps({
  label: {
    required: true,
    type: String
  },
  topKSeries: {
    required: true,
    type: Object as PropType<FlowingPoint[]>
  }
})


const yAxisFormatter = format('.3s')

let chart: any = {}

const options = computed<ChartOptions>(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    title: {
      display: true,
      text: props.label
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
    legend: {
      display: true,
      position: 'bottom',
      align: 'start',
      maxHeight: 100,
      labels: {
        boxWidth: 10
      }
    }
  },
  scales: {
    y: {
      title: {
        display: false,
        text: props.label
      } as TitleOptions,
      ticks: {
        callback: (value) => yAxisFormatter(value as number),
        maxTicksLimit: 8
      },
      stacked: false
    },
    x: {
      ticks: {
        maxTicksLimit: 12
      }
    }
  }
}))

const xAxisLabels = computed(() => {
  let timestamps = props.topKSeries.map(x => Date.parse(x.timestamp))
  let uniqueTimestamps = [...new Set(timestamps)]
  uniqueTimestamps.sort()
  return uniqueTimestamps.map((val: any) => {
    return formatTimestamp(val, 'minutes')
  })
})

const dataSets = computed(() => {
  // every unique label and direction should be its own data set
  let seriesByKey = _.groupBy(props.topKSeries, function(point){
    return `${point.label} (${point.direction})`
  })

  return _.map(seriesByKey, function(points, label){
    let sortedPoints = _.sortBy(points, function(point){
      return Date.parse(point.timestamp)
    })
    let values = _.map(sortedPoints, function(point){ return point.value})
    return {
      label,
      data: values,
      hitRadius: 5,
      hoverRadius: 6
    }
  })
})

const chartData = computed<ChartData<any>>(() => {
  return {
    labels: xAxisLabels.value,
    datasets: dataSets.value
  }
})

const render = async (update?: boolean) => {
  try {
    if (update) {
      chart.data = chartData.value
      chart.update()
    } else {
      if(props.topKSeries && chartData.value.datasets.length) {
        const ctx: any = document.getElementById(`${props.label}`)
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
    console.log('Could not render graph for ', props.label)
  }
}

onMounted(async () => {
  render()
})
</script>

<style scoped lang="scss">
@use "@featherds/styles/themes/variables";

.container {
  position: relative;
  width: 30%;
  min-width: 400px;
  border: 1px solid var(variables.$secondary-text-on-surface);
  border-radius: 10px;
  padding: var(variables.$spacing-l) var(variables.$spacing-l);
}
.canvas-wrapper {
  height: 400px;
}
</style>
