<template>
  <div class="flows">
    <div class="header">
      <div class="section-title">{{ dashboardText.TopApplications.title }}</div>
      <div
        class="link"
        @click="router.push('Flows')"
      >
        {{ dashboardText.TopApplications.linkFlows }}
      </div>
    </div>
    <div class="section-subtitle">{{ dashboardText.TopApplications.timePeriod }}</div>
    <div
      v-if="hasData"
      class="chart-box"
    >
      <PolarArea
        :data="dataGraph"
        :options="constGraph"
        :id="'pieChartApplications'"
      />
    </div>
    <div
      v-else
      class="empty"
    >
      <!--will be replaced with the component-->
      No data
    </div>
  </div>
</template>

<script setup lang="ts">
import { map, sum, sortBy } from 'lodash'
import { useFlowsStore } from '@/store/Views/flowsStore'
import useTheme from '@/composables/useTheme'
import { useMediaQuery } from '@vueuse/core'
import { PolarArea } from 'vue-chartjs'
import dashboardText from '@/components/Dashboard/dashboard.text'

const isLargeScreen = useMediaQuery('(min-width: 1024px)')
const { onThemeChange, isDark } = useTheme()
const flowsStore = useFlowsStore()
const router = useRouter()
const constGraph = ref()
const dataGraph = ref()
const hasData = ref(false)

const dataApplications = computed(() => flowsStore.topApplications)

const buildData = () => {
  if (dataApplications.value.length > 0) {
    const values = map(sortBy(dataApplications.value, ['bytesIn']).reverse(), 'bytesIn')
    const total = sum(values)
    const percentages = map(values, (i) => (i * 100) / total)
    const labels = map(dataApplications.value, (app, i) => ` ${i + 1}. ${app.label} (${percentages[i].toFixed(2)}%)`)
    const data = {
      labels: labels,
      datasets: [
        {
          data: percentages
        }
      ]
    }
    hasData.value = true
    dataGraph.value = data
  } else {
    hasData.value = false
  }
}

watchEffect(() => {
  buildData()
})

const config = {
  responsive: true,
  aspectRatio: 1.3,
  plugins: {
    legend: {
      display: isLargeScreen.value,
      align: 'center',
      position: 'right',
      labels: {
        boxWidth: 22,
        boxHeight: 22,
        borderRadius: 20,
        useBorderRadius: true,
        padding: 20,
        color: isDark.value ? '#d1d0d0' : '#00000',
        font: {
          size: 15
        },
        usePointStyle: true
      }
    }
  }
}
constGraph.value = config

onThemeChange(() => {
  config.plugins.legend.labels.color = isDark.value ? '#d1d0d0' : '#00000'
  constGraph.value = { ...config }
})
</script>

<style scoped lang="scss">
@use '@featherds/styles/mixins/typography';
@use '@featherds/styles/themes/variables';
@use '@/styles/mediaQueriesMixins.scss';

.flows {
  width: 100%;
  background-color: var(variables.$surface);
  padding: var(variables.$spacing-l);
  border: 1px solid var(variables.$border-on-surface);
  @include mediaQueriesMixins.screen-md {
    width: 48%;
  }
  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    .link {
      @include typography.subtitle2();
      text-decoration: underline;
      cursor: pointer;
      color: var(variables.$primary);
    }
    .section-title {
      @include typography.headline3();
    }
  }
  .chart-box {
    border: 1px solid var(variables.$border-on-surface);
    padding: 0 var(variables.$spacing-l);
    margin-top: var(variables.$spacing-l);
  }

  .empty {
    height: 630px;
    text-align: center;
    padding-top: 50px;
  }
}

.section-subtitle {
  @include typography.caption();
}
</style>
