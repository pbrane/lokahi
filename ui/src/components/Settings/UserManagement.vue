<template>
<section class="container">
  <UserModal v-if="store.isModalVisible" />
  <TableCard class="card border user-management-wrapper">
      <div class="header">
        <div class="title-container">
          <div class="title-main">
            <div class="heading">
               <h3>User Management</h3>
             <FeatherButton primary @click="handleAddUser">Add User</FeatherButton>
            </div>
          </div>
          <p>Manage User Access to this instance, including adding, editing, and deleting access.</p>
        </div>
      </div>
      <h2 class="heading-inside">User Management</h2>
      <div class="container card table-card border">
        <table
          class="data-table"
          aria-label="User management table"
        >
          <thead>
            <tr>
              <FeatherSortHeader
                v-for="col of columns"
                :key="col.label"
                scope="col"
                :property="col.id"
                :sort="(sort as any)[col.id]"
                v-on:sort-changed="sortChanged"
              >
                {{ col.label }}
              </FeatherSortHeader>
              <th> Email </th>
              <th> Edit </th>
              <th> Resend E-mail </th>
              <th> Revoke Access </th>
            </tr>
          </thead>
          <TransitionGroup
            name="data-table"
            tag="tbody"
          >
            <tr v-for="data of store.usersList" v-bind:key="data.id">
              <td>{{ data.firstName }} {{ data.lastName }}</td>
              <td>{{ data.email }}</td>
              <td> <FeatherIcon class="my-primary-icon" :icon="Edit" @click="handleEditUser(data.id)" /> </td>
              <td> <FeatherIcon class="my-primary-icon" :icon="Email" @click="handleEmailUser(data.id)" /> </td>
              <td> <FeatherIcon class="my-primary-icon" :icon="Remove" @click="handleRemoveUser(data.id)" /> </td>
            </tr>
          </TransitionGroup>
        </table>
        <FeatherPagination
          v-model="pagination.page"
          :pageSize="pagination.pageSize"
          :total="pagination.total"
          @update:model-value="setUserTablePage"
        >
        </FeatherPagination>
      </div>
    </TableCard>
</section>
</template>
<script setup lang="ts">
import { useUserStore } from '@/store/Views/userStore'
import { SORT } from '@featherds/table'
import Edit from '@featherds/icon/action/Edit'
import Email from '@featherds/icon/action/Email'
import Remove from '@featherds/icon/action/Remove'
const store = useUserStore()

let pagination = reactive({
  page: 1, // FE pagination component has base 1 (first page)
  pageSize: 10,
  total: 0
})

const columns = [
  { id: 'name', label: 'Name' }
]

const sort = reactive({
  name: SORT.DESCENDING
}) as any

const setUserTablePage = (page: number) => {
  if (page !== pagination.page) {
    pagination.page = page
    store.getUsersList()
  }
}
const sortChanged = (sortObj: Record<string, string>) => {
  setUserTableSort(sortObj)
  for (const prop in sort) {
    sort[prop] = SORT.NONE
  }
  sort[sortObj.property] = sortObj.value
}
const setUserTableSort = (sortObj: Record<string, string>) => {
  const isAscending = sortObj.value === 'asc'
  store.getUsersList()
}
const handleAddUser = () => {
  store.createUser()
  store.openModalHandler()
}
const handleEditUser = (id: string) => {
  console.log('handleEditUser is clicked', id)
}
const handleEmailUser = (id: string) => {
  console.log('handleEmailUser is clicked', id)
}
const handleRemoveUser = (id: string) => {
  console.log('handleRemoveUser is clicked', id)
}
onMounted(async () => await store.getUsersList())
</script>
<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/_transitionDataTable';
@use '@/styles/vars.scss';

.user-management-wrapper {
  margin-bottom: 20px;
  padding: 20px;
  width: 100%;
  .header{
      display: flex;
      justify-content: space-between;
      padding: 0 26px;
      .title-container{
        width: 100%;
        .heading{
            display: flex;
            justify-content: space-between;
           }
      }
  }
  .card {
      .title-container {
        display: flex;
        justify-content: space-between;
        width: 100%;
        margin-bottom: 20px;
        .title-main {
          display: flex;
          flex-direction: column;
          .title {
            @include typography.headline3;
            margin-left: 20px;
            margin-top: 2px;
          }
          .time-frame {
            @include typography.caption;
            margin-left: 20px;
          }
        }
        .btns {
          margin-right: 15px;
        }
      }
    }
  .container {
    display: block;
    overflow-x: auto;
    margin: 0px 20px;
    .data-table{
      width: 100%;
    }
    table {
      width: 100% I !important;
      @include table.table;
      thead {
        background: var(variables.$surface);
        text-transform: uppercase;
      }
      td {
        white-space: nowrap;
        .my-primary-icon {
          font-size: 1.5rem;
          color: var(variables.$primary);
          cursor: pointer;
        }
      }
    }
  }
  .heading-inside{
  @include typography.headline3;
  margin: 20px;
  background-color: var(variables.$background);
  padding: 20px 30px;
  font-weight: bolder;
  }
}
</style>
