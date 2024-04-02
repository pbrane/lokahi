<template>
  <PrimaryModal
    :hideTitle="true"
    :visible="isVisible"
    class="monitoring-policy-modal"
  >
    <template #content>
      <div class="close-modal">
        <p v-if="name" class="sub-title"> Are you sure you wish to delete {{ name }}? </p>
        <FeatherButton
          icon="Cancel"
          @click="closeModal"
        >
          <FeatherIcon :icon="icons.Cancel" />
        </FeatherButton>
      </div>
      <p v-if="customMsg" class="customMessage">{{ customMsg }}</p>
      <p v-if="noteMsg" v-html="noteMsg"  class="noteMessage"></p>
    </template>
    <template #footer>
      <FeatherButton
        data-test="cancel-btn"
        secondary
        @click="closeModal"
      >
        Cancel
      </FeatherButton>
      <ButtonWithSpinner
        data-test="delete-btn"
        primary
        :click="deleteAndCloseModal"
        :isFetching="isDeleting"
      >
        {{ actionBtnText || 'Delete' }}
      </ButtonWithSpinner>
    </template>
  </PrimaryModal>
</template>

<script setup lang="ts">
import Cancel from '@featherds/icon/navigation/Cancel'
import { useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'

const store = useMonitoringPoliciesStore()
const icons = markRaw({
  Cancel
})
const props = defineProps<{
  name?: string
  isVisible: boolean
  deleteHandler: (...p: any) => any
  closeModal: () => void
  isDeleting?: boolean
  customMsg?: string
  actionBtnText?: string
  noteMsg?: string
}>()

const deleteAndCloseModal = async () => {
  await props.deleteHandler()
  props.closeModal()
  store.clearSelectedPolicy()
}

const closeModal = () => {
  props.closeModal()
  store.clearSelectedPolicy()
}
</script>
<style lang="scss">

.monitoring-policy-modal .content {
  max-width: 530px;
}
</style>
<style lang="scss" scoped>

  .customMessage, .noteMessage {
    margin-top: 5%;
  }

  .note-message, .sub-title {
    font-weight: bold;
  }

  .close-modal {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
</style>
