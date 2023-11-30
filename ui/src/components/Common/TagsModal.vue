<template>
  <PrimaryModal
    :visible="visible"
    :class="[modal.cssClass, 'common-tag-modal']"
  >
    <template #content>
      <h3>Manage Tags</h3>
      <h4 class="subhead-1">{{ node.nodeAlias || node.nodeLabel }}</h4>
      <p>A tag is an optional label that you can associate with a node to filter and group devices.</p>
      <h4 class="subhead-1 existing-tags">Existing Tags</h4>
      <FeatherChipList label="Tags">
        <FeatherChip
          v-for="tag in tagStore.filteredTags"
          :key="tag.id"
          :class="{ selected: tagsForDeletion.some((t) => t.id === tag.id) }"
        >
          {{ tag.name }}
          <template v-slot:icon>
            <FeatherIcon
              :icon="deleteIcon"
              @click="tagStore.filterTag(tag)"
              class="pointer"
            />
          </template>
        </FeatherChip>
      </FeatherChipList>
      <h4 class="subhead-1 add-tags">Add Tags</h4>
      <AtomicAutocomplete
        class="tag-modal-complete"
        data-test="search-by-tags-inner"
        inputLabel="Search or create new tag"
        :loading="tagQueries.tagsSearchIsFetching"
        :outsideClicked="closeAutocomplete"
        :itemClicked="itemClicked"
        :resultsVisible="isAutoCompleteOpen"
        :focusLost="onFocusLost"
        :wrapperClicked="wrapperClicked"
        :results="
          tagQueries.tagsSearched
            .filter((t) => {
              return !tagStore.filteredTags.find((d) => d.name === t.name)
            })
            .map((d) => d.name)
        "
        :inputValue="inputValue"
        :textChanged="textChanged"
      />
    </template>
    <template #footer>
      <FeatherButton
        secondary
        @click="closeModal"
      >
        {{ modal.cancelLabel }}
      </FeatherButton>
      <FeatherButton
        primary
        @click="saveFilteredTagsToNode"
      >
        {{ modal.saveLabel }}
      </FeatherButton>
    </template>
  </PrimaryModal>
</template>

<script setup lang="ts">
import { Tag, Node } from '@/types/graphql'
import { ModalPrimary } from '@/types/modal'
import { PropType } from 'vue'
import deleteIcon from '@featherds/icon/navigation/Cancel'
import useAtomicAutocomplete from '@/composables/useAtomicAutocomplete'
import { useTagQueries } from '@/store/Queries/tagQueries'
import { useTagStore } from '@/store/Components/tagStore'

const props = defineProps({
  closeModal: { type: Function as PropType<() => void>, default: () => null, required: true },
  node: { type: Object as PropType<Node>, default: () => ({}), required: true },
  visible: { type: Boolean, default: false, required: true }
})

const tagQueries = useTagQueries()
const tagsForDeletion = ref([] as Tag[])
const nodeIdForDeletingTags = ref()
const tagStore = useTagStore()

const { isAutoCompleteOpen, closeAutocomplete, wrapperClicked, onFocusLost, itemClicked, textChanged, inputValue } =
  useAtomicAutocomplete(tagQueries.getTagsSearch, () => tagQueries.tagsSearched.length, tagStore.addFilteredTag)

const modal = ref<ModalPrimary>({
  title: '',
  cssClass: '',
  content: '',
  id: '',
  cancelLabel: 'cancel',
  saveLabel: 'Apply',
  hideTitle: true
})

const saveFilteredTagsToNode = async () => {
  const success = await tagStore.saveFilteredTagsToNode()
  if (success) props.closeModal()
}

watchEffect(() => {
  if (props.visible && props.node && props.node.tags) {
    modal.value.saveLabel = 'Apply'
    nodeIdForDeletingTags.value = props.node.id
    tagStore.setFilteredTags(props.node.tags)
  } else if (!props.visible) {
    inputValue.value = ''
  }
})
</script>
<style lang="scss">
.common-tag-modal .dialog-body header {
  margin-bottom: 0;
}

.common-tag-modal .content {
  max-width: 490px;
}
</style>

<style lang="scss" scoped>
.common-tag-modal h3 {
  margin-bottom: var(--feather-spacing-m);
}

.existing-tags {
  margin-top: var(--feather-spacing-m);
}
.add-tags {
  margin-top: var(--feather-spacing-l);
  margin-bottom: var(--feather-spacing-s);
}
</style>
