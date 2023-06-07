import { ItemPreviewProps } from '@/components/Common/ItemPreview.vue'
import { defineStore } from 'pinia'

interface WelcomeStoreState {
  copied: boolean,
  devicePreview: ItemPreviewProps,
  downloaded: boolean,
  firstDevice: Record<string, string>,
  minionStatusCopy: string,
  minionStatusLoading: boolean,
  minionStatusStarted: boolean,
  minionStatusSuccess: boolean,
  slide: number,
  slideOneCollapseVisible: boolean,
  slideTwoDisabled: boolean,
  slideThreeDisabled: boolean,
}

export const useWelcomeStore = defineStore('welcomeStore', {
  state: () => ({
    copied: false,
    devicePreview: {
      title: 'Device Preview', loading: false, itemTitle: 'Minion Gateway', itemSubtitle: 'Added --/--/--', itemStatuses: [
        { title: 'ICMP Latency', status: 'Normal', statusColor: '#cee3ce', statusText: '#0b720c' },
        { title: 'SNMP Uptime', status: 'Normal', statusColor: '#cee3ce', statusText: '#0b720c' }
      ]
    },
    downloaded: false,
    minionStatusCopy: 'Waiting for the Docker Install Command to be complete.',
    minionStatusLoading: false,
    minionStatusSuccess: false,
    minionStatusStarted: false,
    slide: 1,
    slideOneCollapseVisible: false,
    slideTwoDisabled: false,
    firstDevice: { name: '', ip: '', communityString: '', port: '' },
    slideThreeDisabled: false
  } as WelcomeStoreState),
  actions: {
    nextSlide() {
      this.slide = this.slide + 1
      if (this.slide === 2) {
        this.loadDevicePreview()
      }
      this.scrollTop()
    },
    loadDevicePreview() {
      this.devicePreview.loading = true
      setTimeout(() => {
        this.devicePreview.loading = false
      }, 3000)
    },
    prevSlide() {
      this.slide = this.slide - 1
      this.scrollTop()
    },
    scrollTop() {
      const welcomeWrapper = document.querySelector('.welcome-wrapper')
      if (welcomeWrapper) {
        welcomeWrapper?.scrollTo({ top: 0, behavior: 'smooth' })
      }
    },
    skipSlideThree() {
      this.prevSlide()
      console.log('Close this modal')
    },
    startMonitoring() {
      console.log('Start Monitoring')
    },
    copyDockerClick() {
      this.copied = true
      setTimeout(() => {
        this.copied = false
      }, 3000)
    },
    updateMinionStatusCopy() {
      if (!this.minionStatusStarted) {
        this.minionStatusCopy = 'Waiting for the Docker Install Command to be complete.'
      }
      if (this.minionStatusStarted && this.minionStatusLoading) {

        this.minionStatusCopy = 'Please wait while we detect your minion'
      }
      if (this.minionStatusStarted && !this.minionStatusLoading && this.minionStatusSuccess) {
        this.minionStatusCopy = 'Minion Detected.'
      }
    },
    downloadClick() {
      this.downloaded = true
      this.minionStatusLoading = true
      this.minionStatusStarted = true
      this.updateMinionStatusCopy()
      setTimeout(() => {
        this.downloaded = false
        this.minionStatusLoading = false
        this.minionStatusSuccess = true
        this.updateMinionStatusCopy()
        setTimeout(() => {
          this.minionStatusSuccess = false
          this.minionStatusStarted = false
          this.updateMinionStatusCopy()
        }, 3000)
      }, 3000)
    },
    toggleSlideOneCollapse() {
      this.slideOneCollapseVisible = !this.slideOneCollapseVisible
    },
    updateFirstDevice(key: string, value: string | number | undefined) {
      if (typeof value === 'string') {
        this.firstDevice[key] = value
        if (value){

          this.devicePreview.loading = true
          this.devicePreview.itemStatuses[0].status = 'Critical'
          this.devicePreview.itemStatuses[0].statusColor = 'rgba(165,2,31,0.3)'
          this.devicePreview.itemStatuses[0].statusText = 'rgba(165,2,31,1)'
          setTimeout(() => {
            this.devicePreview.loading = false
          }, 1000)
        }
      }
    }
  }
})