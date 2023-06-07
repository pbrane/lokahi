<template>
    <div class="welcome-wrapper">
        <div class="gradiant">
            <GradiantBG />
        </div>
        <div class="welcome-inner">
            <div class="welcome-logo">
                <LogoIcon />
            </div>
            <WelcomeSlideOne :visible="welcomeStore.slide === 1" />
            <WelcomeSlideTwo :visible="welcomeStore.slide === 2" />
            <WelcomeSlideThree :visible="welcomeStore.slide === 3" />
        </div>
    </div>
</template>
<script lang="ts" setup>
import LogoIcon from '@/assets/OpenNMS_Horizontal-Logo_Light-BG.svg'
import WelcomeSlideOne from '../components/Welcome/WelcomeSlideOne.vue'
import WelcomeSlideTwo from '../components/Welcome/WelcomeSlideTwo.vue'
import WelcomeSlideThree from '../components/Welcome/WelcomeSlideThree.vue'
import { useWelcomeStore } from '@/store/Views/welcomeStore'
import GradiantBG from '../components/Common/GradiantBG.vue'
const welcomeStore = useWelcomeStore()
</script>
<style lang="scss" scoped>
@import '@featherds/styles/themes/variables';
@import '@featherds/styles/mixins/typography';
@import "@featherds/table/scss/table";

.welcome-wrapper {
    position: fixed;
    z-index: 8001;
    top: 0;
    left: 0;
    width: 100%;
    height: 100vh;
    background-color: var($background);
    overflow-y: auto;
}

.welcome-inner {
    max-width: 660px;
    padding-top: 132px;
    margin: 0 auto;
    position: relative;
    z-index:1;
}

.welcome-logo svg {
    max-width: 160px;
    margin-bottom: 24px;
}
.gradiant {
    position:fixed;
    z-index:0;
    left:0;
    top:0;
    width:100%;
    height:100vh;
    filter:blur(200px);
    opacity:0.5;
    pointer-events:none;
    svg {
        width:100%;
        height:100vh;
    }
}
</style>