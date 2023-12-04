<template>
  <div class="third-tab-events-wrapper border">
    <TableCard>
      <div class="header">
        <div class="title-container">
          <span class="title">Latest Events </span>
        </div>
      </div>
      <div class="container">
        <table
          class="data-table"
          aria-label="Events Table"
          data-test="data-table"
        >
          <thead>
            <tr>
              <th scope="col">Time</th>
              <th scope="col">UEI</th>
              <th scope="col">IP Address</th>
            </tr>
          </thead>
          <TransitionGroup
            name="data-table"
            tag="tbody"
          >
            <tr
              v-for="event in events"
              :key="event.id as number"
              data-test="data-item"
            >
              <td>{{ fnsFormat(event.producedTime, 'M/dd/yyyy HH:mm:ssxxx') }}</td>
              <td>{{ event.uei }}</td>
              <td>{{ event.ipAddress }}</td>
            </tr>
          </TransitionGroup>
        </table>
      </div>
    </TableCard>
  </div>
</template>

<script lang="ts" setup>
import { format as fnsFormat } from 'date-fns'
import { Event } from '@/types/graphql'
defineProps<{
  events: Event[]
}>()
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/_transitionDataTable';

.third-tab-events-wrapper {
  padding: 10px 20px;
  background: var(variables.$surface);
  border-radius: 4px;
  .header {
    display: flex;
    justify-content: space-between;
    .title-container {
      display: flex;
      .title {
        @include typography.headline3;
        margin-bottom: 25px;
      }
    }
  }

  .container {
    display: block;
    overflow-x: auto;
    table {
      width: 100%;
      @include table.table;
      thead {
        background: var(variables.$background);
        text-transform: uppercase;
      }
      td {
        white-space: nowrap;
        div {
          border-radius: 5px;
          padding: 0px 5px 0px 5px;
        }
      }
    }
  }
}
</style>
