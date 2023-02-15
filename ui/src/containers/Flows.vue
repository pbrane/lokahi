<template>
  <div class="container">
    <PageHeader heading="Flows" />
    <h3>Total flows: {{ flowSummary?.numFlows }}</h3>

    <h2>Top Hosts</h2>
    <div class="container">
      <table class="data-table" aria-label="Top 10 hosts" data-test="Top 10 host">
        <thead>
        <tr>
          <th scope="col" data-test="col-host">Host</th>
          <th scope="col" data-test="col-bytes-in">Bytes In</th>
          <th scope="col" data-test="col-bytes-out">Bytes Out</th>
        </tr>
        </thead>
        <TransitionGroup name="data-table" tag="tbody">
          <tr v-for="(topHost, index) in topHosts" :key="(topHost.label as string)" :data-index="index" data-test="top-host">
            <td>{{ topHost.label }}</td>
            <td>{{ topHost.bytesIn }}</td>
            <td>{{ topHost.bytesOut }}</td>
          </tr>
        </TransitionGroup>
      </table>
    </div>

    <h2>Top Applications</h2>
    <div class="container">
      <table class="data-table" aria-label="Top 10 applications" data-test="Top 10 applications">
        <thead>
        <tr>
          <th scope="col" data-test="col-app">Application</th>
          <th scope="col" data-test="col-bytes-in">Bytes In</th>
          <th scope="col" data-test="col-bytes-out">Bytes Out</th>
        </tr>
        </thead>
        <TransitionGroup name="data-table" tag="tbody">
          <tr v-for="(topApp, index) in topApplications" :key="(topApp.label as string)" :data-index="index" data-test="top-host">
            <td>{{ topApp.label }}</td>
            <td>{{ topApp.bytesIn }}</td>
            <td>{{ topApp.bytesOut }}</td>
          </tr>
        </TransitionGroup>
      </table>
    </div>

    <h2>Top Conversations</h2>
    <div class="container">
      <table class="data-table" aria-label="Top 10 conversations" data-test="Top 10 conversations">
        <thead>
        <tr>
          <th scope="col" data-test="col-convo">Conversation</th>
          <th scope="col" data-test="col-bytes-in">Bytes In</th>
          <th scope="col" data-test="col-bytes-out">Bytes Out</th>
        </tr>
        </thead>
        <TransitionGroup name="data-table" tag="tbody">
          <tr v-for="(topConvo, index) in topConversations" :key="(topConvo.label as string)" :data-index="index" data-test="top-host">
            <td>{{ topConvo.label }}</td>
            <td>{{ topConvo.bytesIn }}</td>
            <td>{{ topConvo.bytesOut }}</td>
          </tr>
        </TransitionGroup>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useFlowQueries } from '@/store/Queries/flowsQueries'
const store = useFlowQueries()

const flowSummary = computed(() => {
  return store.flowSummary
})

const topHosts = computed(() => {
  return store.topHosts
})

const topApplications = computed(() => {
  return store.topApplications
})

const topConversations = computed(() => {
  return store.topConversations
})
</script>
