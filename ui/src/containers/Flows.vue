<template>
  <div class="header-container">
    <div class="header" data-test="page-header">
      Flows
    </div>

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
  </div>

  <div class="container">
    <h3>Total flows: {{ flowSummary?.numFlows }}</h3>

    <FeatherExpansionPanel title="Top Hosts">
      <div class="table-row">
        <TrafficGraph v-if="topHostSeries?.length > 0"
                      label="Top Hosts"
                      :topKSeries="topHostSeries"
                      class="flowchart"
        />

        <table class="data-table" aria-label="Top 10 hosts" data-test="Top 10 host">
          <thead>
          <tr>
            <th scope="col" data-test="col-host">Host</th>
            <th scope="col" data-test="col-bytes-in">Bytes In</th>
            <th scope="col" data-test="col-bytes-out">Bytes Out</th>
          </tr>
          </thead>
          <TransitionGroup name="data-table" tag="tbody">
            <tr v-for="(topHost, index) in topHostSummaries" :key="(topHost.label as string)" :data-index="index"
                data-test="top-host">
              <td>{{ topHost.label }}</td>
              <td>{{ toBytesDisplay(topHost.bytesIn) }}</td>
              <td>{{ toBytesDisplay(topHost.bytesOut) }}</td>
            </tr>
          </TransitionGroup>
        </table>
      </div>
    </FeatherExpansionPanel>


    <FeatherExpansionPanel title="Top Applications">
      <div class="table-row">
        <TrafficGraph v-if="topApplicationSeries?.length > 0"
                      label="Top Applications"
                      :topKSeries="topApplicationSeries"
                      class="flowchart"
        />

        <table class="data-table" aria-label="Top 10 applications" data-test="Top 10 applications">
          <thead>
          <tr>
            <th scope="col" data-test="col-app">Application</th>
            <th scope="col" data-test="col-bytes-in">Bytes In</th>
            <th scope="col" data-test="col-bytes-out">Bytes Out</th>
          </tr>
          </thead>
          <TransitionGroup name="data-table" tag="tbody">
            <tr v-for="(topApp, index) in topApplicationSummaries" :key="(topApp.label as string)" :data-index="index"
                data-test="top-host">
              <td>{{ topApp.label }}</td>
              <td>{{ toBytesDisplay(topApp.bytesIn) }}</td>
              <td>{{ toBytesDisplay(topApp.bytesOut) }}</td>
            </tr>
          </TransitionGroup>
        </table>
      </div>
    </FeatherExpansionPanel>

    <FeatherExpansionPanel title="Top Conversations">
      <div class="table-row">
        <TrafficGraph v-if="topConversationSeries?.length > 0"
                      label="Top Conversations"
                      :topKSeries="topConversationSeries"
                      class="flowchart"
        />

        <table class="data-table" aria-label="Top 10 conversations" data-test="Top 10 conversations">
          <thead>
          <tr>
            <th scope="col" data-test="col-convo">Conversation</th>
            <th scope="col" data-test="col-bytes-in">Bytes In</th>
            <th scope="col" data-test="col-bytes-out">Bytes Out</th>
          </tr>
          </thead>
          <TransitionGroup name="data-table" tag="tbody">
            <tr v-for="(topConvo, index) in topConversationSummaries" :key="(topConvo.label as string)"
                :data-index="index" data-test="top-host">
              <td>{{ topConvo.label }}</td>
              <td>{{ toBytesDisplay(topConvo.bytesIn) }}</td>
              <td>{{ toBytesDisplay(topConvo.bytesOut) }}</td>
            </tr>
          </TransitionGroup>
        </table>
      </div>
    </FeatherExpansionPanel>

  </div>
</template>

<script setup lang="ts">

const toBytesDisplay = function(bytes:number, si=false, dp=1) {
  // pulled from https://stackoverflow.com/a/14919494
  const thresh = si ? 1000 : 1024
  if (Math.abs(bytes) < thresh) {
    return bytes + ' B'
  }
  const units = si
    ? ['kB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB']
    : ['KiB', 'MiB', 'GiB', 'TiB', 'PiB', 'EiB', 'ZiB', 'YiB'];
  let u = -1
  const r = 10**dp
  do {
    bytes /= thresh
    ++u
  } while (Math.round(Math.abs(bytes) * r) / r >= thresh && u < units.length - 1)
  return bytes.toFixed(dp) + ' ' + units[u]
}


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

import {useFlowQueries} from '@/store/Queries/flowsQueries'

const store = useFlowQueries()

const setTimeWindow = function () {
  store.setTimeWindow(timeWindow.value.hours)
}

const flowSummary = computed(() => {
  return store.flowSummary
})

const topHostSummaries = computed(() => {
  return store.topHostSummaries
})

const topHostSeries = computed(() => {
  return store.topHostSeries
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

const topConversationSeries = computed(() => {
  return store.topConversationSeries
})
</script>


<style scoped lang="scss">
@use "@featherds/styles/themes/variables";
@use "@featherds/table/scss/table";
@use "@featherds/styles/mixins/typography";

.header-container {
  display: flex;
  justify-content: space-between;
  margin: 1.5rem 1rem;

  .header {
    @include typography.headline2;
    font-weight: bold;
  }

  .my-select-div {
    display: flex;
    justify-content: flex-end;
    gap: 20px;
  }
}

.container {
  margin: 5px;
}

.my-select-div {
  display: flex;
  justify-content: flex-end;
}

.table-row {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  gap: 10px;

  .flowchart {
    flex-grow: 1;
  }

  table {
    @include table.table;
    @include table.table-condensed;

    flex-grow: 0.3;

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
