<template>
  <div class="policy-form" v-if="store.selectedPolicy">
    <div class="policy-form-title-container">
      <div class="form-title">{{ store.selectedPolicy.name }}</div>
      <div>
        <FeatherButton
          icon="Edit"
          @click="onEdit(store.selectedPolicy ?? {})"
        >
          <FeatherIcon :icon="icons.Edit" />
        </FeatherButton>
        <FeatherButton icon="ContentCopy" @click="onCopy">
          <FeatherIcon :icon="icons.ContentCopy" />
        </FeatherButton>
        <FeatherButton icon="Cancel" @click="onClose">
          <FeatherIcon :icon="icons.Cancel" />
        </FeatherButton>
      </div>
    </div>
    <hr style="width: 100%" />
    <div class="policy-details-card-wrapper">
      <div class="card">
        <div class="sub-title">STATUS</div>
        <div class="toggle-wrapper">
          <FeatherListSwitch class="basic-switch" v-model="store.selectedPolicy.enabled" />
          <span>Active</span>
        </div>
      </div>
      <div class="card">
        <div class="sub-title">TAGS</div>
        <FeatherChipList condensed label="Tags" class="tag-chip-list" data-test="tag-chip-list">
          <FeatherChip v-for="(tag, index) in store.selectedPolicy?.tags" :key="index" class="pointer" active>
            {{ tag }}
          </FeatherChip>
        </FeatherChipList>
      </div>
      <div class="card">
        <div class="sub-title">ALERT RULES</div>
        <div class="alert-rules-wrapper">
          <div class="alert-rule" v-for="(rule, index) in store.selectedPolicy?.rules?.slice(0, 6)" :key="rule.id">
            <div v-if="index < 5">
              <div class="headline">
                <span>{{ rule.name }}</span>
              </div>
              <div>
                <span>Rule Description</span>
              </div>
            </div>
            <span v-else v-html="monitoringPolicyRules" class="rules" />
          </div>
        </div>
      </div>
      <div class="card">
        <FeatherButton class="delete" secondary @click="openModal">
          <FeatherIcon :icon="icons.deleteIcon" />
          DELETE
        </FeatherButton>
      </div>
    </div>
  </div>
  <DeleteConfirmationModal
   :name="store.selectedPolicy?.name"
   :isVisible="isVisible"
   :customMsg="deleteMsg"
   :noteMsg="noteMsg"
   :closeModal="onCloseModal"
   :deleteHandler="removePolicy" />
</template>

<script setup lang="ts">
import useModal from '@/composables/useModal'
import router from '@/router'
import { useMonitoringPoliciesStore } from '@/store/Views/monitoringPoliciesStore'
import { MonitorPolicy } from '@/types/graphql'
import ContentCopy from '@featherds/icon/action/ContentCopy'
import Delete from '@featherds/icon/action/Delete'
import Edit from '@featherds/icon/action/Edit'
import Cancel from '@featherds/icon/navigation/Cancel'



const store = useMonitoringPoliciesStore()

const { openModal, closeModal, isVisible } = useModal()

const isEnabled = ref(false)

const noteMsg = ref('<b>Deleting this policy may cause these nodes to not be monitored, which means we will stop sending alerts</b>')

const emit = defineEmits<{
  (e: 'onClose'): void
}>()

const icons = markRaw({
  Cancel,
  ContentCopy,
  Edit,
  deleteIcon: Delete
})

const monitoringPolicyRules = computed(() => {
  const length = store.selectedPolicy?.rules?.length || 0
  if (length > 5) {
    return `Plus ${length - 5} other rules`
  } else {
    return ''
  }
})

const deleteMsg = computed(() =>
  `Deleting monitoring policy ${store.selectedPolicy?.name} removes ${store.numOfAlertsForRule} associated alerts. Do you wish to proceed?`
)

const onEdit = (policy: MonitorPolicy) => {
  if (policy.id) {
    router.push(`/monitoring-policies-new/${policy.id}`)
  }
}

const onCopy = () => {
  store.copyPolicy(store.selectedPolicy!)
}

const onClose = () => {
  emit('onClose')
  store.clearSelectedPolicy()
}

const onCloseModal = () => {
  closeModal()
  emit('onClose')
}

const removePolicy = () => {
  store.removePolicy()
  emit('onClose')
}
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@/styles/vars.scss';

.policy-form {
  display: flex;
  flex: 1;
  flex-direction: column;
  padding: var(variables.$spacing-l);

  .policy-form-title-container {
    display: flex;
    justify-content: space-between;

    .form-title {
      @include typography.headline3;
      margin-bottom: var(variables.$spacing-m);
    }
  }

  .subtitle {
    @include typography.subtitle1;
  }
}

.policy-details-card-wrapper {
  display: flex;
  flex-direction: column;

  .card {
    padding: 15px 0px;

    .rules {
      margin-top: 5px;
    }

    .delete {
      &:focus {
        border: none
      }
    }
  }
}

.sub-title {
  @include typography.button;
  color: var(variables.$primary-text-on-surface);
}

.toggle-wrapper {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  gap: 5px;

  :deep(.feather-list-item) {
    padding: 0px !important;

    &:deep(hover) {
      background: none !important;
    }
  }
}

.headline {
  font-size: 1rem;
  font-weight: 600;
  margin: 10px 0px;
}
</style>
