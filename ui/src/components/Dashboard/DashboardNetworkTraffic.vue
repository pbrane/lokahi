<template>
  <DashboardEmptyState :texts="dashboardText.NetworkTraffic">
    <template v-slot:icon>
      <FeatherIcon
        :icon="isDark ? AreaChartDark : AreaChart"
        class="empty-chart-icon"
      />
    </template>
  </DashboardEmptyState>
</template>

<script setup lang="ts">
import dashboardText from '@/components/Dashboard/dashboard.text'
import AreaChart from '@/assets/AreaChart.svg'
import AreaChartDark from '@/assets/AreaChart-dark.svg'
import useTheme from '@/composables/useTheme'

const { isDark } = useTheme()
</script>

<style scoped lang="scss"></style>
