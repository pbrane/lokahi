<template>
    <div :class="['welcome-slide-three-wrapper', visible ? 'visible' : 'hidden',
    ]">
        <div class="welcome-slide-three-inner">
            <div class="welcome-slide-three-title">
                <h2>Add Your First Device</h2>
                <p>We've populated the fields from your Minion deployement.</p>
            </div>
            <div class="welcome-slide-three-form">
                <FeatherInput label="Name" :modelValue="welcomeStore.firstDevice.name"
                    @update:modelValue="(e) => welcomeStore.updateFirstDevice('name', e)" />
                <FeatherInput label="Management IPV4/IPV6" :modelValue="welcomeStore.firstDevice.ip"
                    @update:modelValue="(e) => welcomeStore.updateFirstDevice('firstDevice', e)" />
                <FeatherInput label="Community String (optional)" :modelValue="welcomeStore.firstDevice.communityString"
                    @update:modelValue="(e) => welcomeStore.updateFirstDevice('communityString', e)" />
                <FeatherInput label="Port (optional)" :modelValue="welcomeStore.firstDevice.port"
                    @update:modelValue="(e) => welcomeStore.updateFirstDevice('port', e)" />
            </div>
            <ItemPreview :loading="welcomeStore.devicePreview.loading" :title="welcomeStore.devicePreview.title" :itemTitle="welcomeStore.devicePreview.itemTitle"
                :itemSubtitle="welcomeStore.devicePreview.itemSubtitle"
                :itemStatuses="welcomeStore.devicePreview.itemStatuses" />
            <div class="welcome-slide-footer">
                <FeatherButton text @click="welcomeStore.skipSlideThree">Skip</FeatherButton>
                <FeatherButton primary :disabled="welcomeStore.slideThreeDisabled" @click="welcomeStore.startMonitoring">
                    Continue
                </FeatherButton>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { useWelcomeStore } from '@/store/Views/welcomeStore'
import ItemPreview from '../Common/ItemPreview.vue'
defineProps({
  visible: { type: Boolean, default: false }
})
const welcomeStore = useWelcomeStore()
</script>
<style lang="scss" scoped>
@import '@featherds/styles/themes/variables';
@import '@featherds/styles/mixins/typography';

.welcome-slide-three-wrapper {
    border: 1px solid var($border-on-surface);
    border-radius: 3px;
    padding: 40px 24px;
    max-width: 660px;
    width: 100%;
    margin-bottom: 40px;
    background-color: var($surface);
    position: absolute;

    h2 {
        @include headline2();
        margin-bottom: 12px;
    }
}

.welcome-slide-three-title {
    margin-bottom: 32px;
}

.welcome-slide-footer {
    display: flex;
    justify-content: flex-end;
}

.visible {
    opacity: 1;
    transition: opacity 0.1s ease-in-out 0.2s, transform 0.2s ease-out 0.2s;
    transform: translateY(0px);
    pointer-events: all;
}

.hidden {
    opacity: 0;
    transition: opacity 0.2s ease-in-out 0s, transform 0.3s ease-in 0s;
    transform: translateY(-20px);
    pointer-events: none;
}
</style>