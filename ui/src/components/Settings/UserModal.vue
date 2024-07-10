<template>
  <PrimaryModal v-if="store.selectedUser" :visible="store.isModalVisible" :class="[modal.cssClass, 'user-modal']">
    <template #content>
      <section class="feather-row">
        <h3 class="feather-col-6">{{ store.userEditMode === CreateEditMode.Create ? "Add User" : "Edit User" }}</h3>
        <div class="feather-col-6">
          <FeatherIcon :icon="deleteIcon" @click="store.closeModalHandler()" class="pointer" />
        </div>
      </section>
     <section class="user-labels">
       <div class="name-div">
         <FeatherInput
           v-model.trim="store.selectedUser.firstName"
           label="First Name*"
           v-focus
           data-test="user-firstName-input"
           :error="store.validationErrors.firstName"
           class="user-firstName"
         />
          <FeatherInput
           v-model.trim="store.selectedUser.lastName"
           label="Last Name*"
           v-focus
           data-test="user-lastName-input"
           :error="store.validationErrors.lastName"
           class="user-lastName"
          />
       </div>
       <FeatherInput
        v-model.trim="store.selectedUser.email"
        label="Email*"
        data-test="user-email-input"
        :error="store.validationErrors.email"
        class="user-email"
        />
       <FeatherInput
        v-model.trim="store.selectedUser.username"
        label="Username*"
        data-test="user-username-input"
        :disabled="store.userEditMode === CreateEditMode.Edit"
        :error="store.validationErrors.username"
        class="user-username"
        />
      <FeatherProtectedInput
        v-if="store.userEditMode === CreateEditMode.Create"
        v-model.trim="store.selectedUser.password"
        label="Password*"
        data-test="user-password-input"
        :error="store.validationErrors.password"
        class="user-password"
        />
     </section>
    </template>
    <template #footer>
      <FeatherButton secondary @click="store.closeModalHandler()">
        {{ modal.cancelLabel }}
      </FeatherButton>
      <FeatherButton primary @click="handleButtonAction()">
        {{ store.userEditMode === CreateEditMode.Create ? "Add" : "Update" }}
      </FeatherButton>
    </template>
  </PrimaryModal>
</template>

<script setup lang="ts">
import { ModalPrimary } from '@/types/modal'
import deleteIcon from '@featherds/icon/navigation/Cancel'
import { useUserStore } from '@/store/Views/userStore'
import { CreateEditMode } from '@/types'
import { FeatherProtectedInput } from '@featherds/protected-input'

const store = useUserStore()

const modal = ref<ModalPrimary>({
  title: '',
  cssClass: '',
  content: '',
  id: '',
  cancelLabel: 'cancel',
  saveLabel: 'Apply',
  hideTitle: true
})
const handleButtonAction = async () => {
  if (store.userEditMode === CreateEditMode.Create) {
    await store.saveUser()
  } else {
    await store.updateUserData()
  }
}
</script>
<style lang="scss">
.user-modal {
  margin-bottom: var(--feather-spacing-s);
  .content {
    max-width: 490px;
  }
  .feather-row {
    display: flex;
    gap: 0.5rem;
    align-items: center;
    justify-content: space-between;
      .feather-col-6 {
        h3 {
          margin-bottom: var(--feather-spacing-m);
        }
        :deep(.feather-icon) {
          width: 1.5em;
          height: 2em;
          margin-top: 5px;
        }
      }
  }
  .user-labels{
    margin-top: var(--feather-spacing-s);
    .name-div{
      display: flex;
      gap: 15px;
      &>div{
        flex: 1;
      }
    }
  }
  .dialog-footer{
    padding: 0rem 1rem 1.5rem .5rem !important
  }
  .dialog-body header {
    margin-bottom: 0;
  }
}
</style>

