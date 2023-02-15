<template>
  <div class="container">
    <PageHeader heading="Flows" />
    <h3>Total flows: {{ flowSummary?.numFlows }}</h3>
    <div class="my-select-div">
      <FeatherSelect
        class="my-select"
        label="Time window"
        :options="timeWindows"
        text-prop="name"
        v-model="timeWindow"
        @update:modelValue="setTimeWindow"
      >
      </FeatherSelect>
    </div>


    <div class="mytablegrid">
      <div class="container">
        <h2>Top Hosts</h2>
        <table class="data-table" aria-label="Top 10 hosts" data-test="Top 10 host">
          <thead>
          <tr>
            <th scope="col" data-test="col-host">Host</th>
            <th scope="col" data-test="col-bytes-in">Bytes In</th>
            <th scope="col" data-test="col-bytes-out">Bytes Out</th>
          </tr>
          </thead>
          <TransitionGroup name="data-table" tag="tbody">
            <tr v-for="(topHost, index) in topHostSummaries" :key="(topHost.label as string)" :data-index="index" data-test="top-host">
              <td>{{ topHost.label }}</td>
              <td>{{ topHost.bytesIn }}</td>
              <td>{{ topHost.bytesOut }}</td>
            </tr>
          </TransitionGroup>
        </table>
      </div>

      <div class="container">
        <h2>Top Applications</h2>
        <table class="data-table" aria-label="Top 10 applications" data-test="Top 10 applications">
          <thead>
          <tr>
            <th scope="col" data-test="col-app">Application</th>
            <th scope="col" data-test="col-bytes-in">Bytes In</th>
            <th scope="col" data-test="col-bytes-out">Bytes Out</th>
          </tr>
          </thead>
          <TransitionGroup name="data-table" tag="tbody">
            <tr v-for="(topApp, index) in topApplicationSummaries" :key="(topApp.label as string)" :data-index="index" data-test="top-host">
              <td>{{ topApp.label }}</td>
              <td>{{ topApp.bytesIn }}</td>
              <td>{{ topApp.bytesOut }}</td>
            </tr>
          </TransitionGroup>
        </table>

        <table class="data-table" aria-label="Top 10 applications" data-test="Top 10 applications">
          <thead>
          <tr>
            <th scope="col" data-test="col-timestamp">Timestamp</th>
            <th scope="col" data-test="col-direction">Direction</th>
            <th scope="col" data-test="col-label">Label</th>
            <th scope="col" data-test="col-value">Value</th>
          </tr>
          </thead>
          <TransitionGroup name="data-table" tag="tbody">
            <tr v-for="(topApp, index) in topApplicationSeries" :key="[topApp.timestamp, topApp.direction, topApp.label]" :data-index="index" data-test="top-host">
              <td>{{ topApp.timestamp }}</td>
              <td>{{ topApp.direction }}</td>
              <td>{{ topApp.label }}</td>
              <td>{{ topApp.value }}</td>
            </tr>
          </TransitionGroup>
        </table>
      </div>

      <div class="container">
        <h2>Top Conversations</h2>
        <table class="data-table" aria-label="Top 10 conversations" data-test="Top 10 conversations">
          <thead>
          <tr>
            <th scope="col" data-test="col-convo">Conversation</th>
            <th scope="col" data-test="col-bytes-in">Bytes In</th>
            <th scope="col" data-test="col-bytes-out">Bytes Out</th>
          </tr>
          </thead>
          <TransitionGroup name="data-table" tag="tbody">
            <tr v-for="(topConvo, index) in topConversationSummaries" :key="(topConvo.label as string)" :data-index="index" data-test="top-host">
              <td>{{ topConvo.label }}</td>
              <td>{{ topConvo.bytesIn }}</td>
              <td>{{ topConvo.bytesOut }}</td>
            </tr>
          </TransitionGroup>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const timeWindows = [
  {
    hours: 1,
    name: 'Last hour'
  },
  {
    hours: 2,
    name: 'Last 2 hours'
  },
  {
    hours: 4,
    name: 'Last 4 hours'
  },
  {
    hours: 12,
    name: 'Last 12 hours'
  },
  {
    hours: 24,
    name: 'Last 24 hours'
  },
  {
    hours: 48,
    name: 'Last 2 days'
  }
]
const timeWindow = ref()

import { useFlowQueries } from '@/store/Queries/flowsQueries'
const store = useFlowQueries()

const setTimeWindow = function() {
  store.setTimeWindow(timeWindow.value.hours)
}

const flowSummary = computed(() => {
  return store.flowSummary
})

const topHostSummaries = computed(() => {
  return store.topHostSummaries
})

const topApplicationSummaries = computed(() => {
  return store.topApplicationSummaries
})

const topApplicationSeries = computed(() => {
  return store.topApplicationSeries
})

const topConversationSummaries = computed(() => {
  return store.topConversationSummaries
})
</script>


<style scoped lang="scss">
@use "@featherds/styles/themes/variables";
@use "@featherds/table/scss/table";
@use "@featherds/styles/mixins/typography";

.my-select-div {
  display: flex;
  justify-content: flex-end;
}

.my-select {
  width: 400px;
  align-self: flex-end;
}

.mytablegrid {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  gap: 10px;

  table {
    width: 100%;
    @include table.table;
    @include table.table-condensed;
    thead {
      background: var(typography.$background);
      text-transform: uppercase;
    }
    td {
      white-space: nowrap;
      display: table-cell;
      div {
        border-radius: 5px;
        padding: 0px 5px 0px 5px;
      }
    }
  }
}

</style>
