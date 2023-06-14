import { ItemPreviewProps } from '@/components/Common/ItemPreview.vue'
import { defineStore } from 'pinia'
import { useWelcomeQueries } from '../Queries/welcomeQueries'
import { CertificateResponse } from '@/types/graphql'
import { createAndDownloadBlobFile } from '@/components/utils'
import { fncVoid } from '@/types'

interface WelcomeStoreState {
  showOnboarding: boolean,
  stopPollingMinions: fncVoid
  minionCert: CertificateResponse
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
    showOnboarding: false,
    stopPollingMinions: () => {},
    minionCert: { password: '', certificate: '' },
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
    async getShowOnboardingState() {
      const queries = useWelcomeQueries()
      await queries.checkSetupState()
      this.showOnboarding = queries.isShowOnboardingState
    },
    nextSlide() {
      this.slide = this.slide + 1
      if (this.slide === 2) {
        this.loadDevicePreview()
      }
      if (this.slide === 3) {
        this.stopPollingMinions()
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
      navigator.clipboard.writeText(this.dockerCmd).then(() => this.copied = true)
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
    async downloadClick() {
      const queries = useWelcomeQueries()
      await queries.downloadMinionCertificate()
      this.minionCert = queries.minionCert

      const { resume: startPollingMinions, pause: stopPollingMinions } = useTimeoutPoll(this.refreshMinions, 10000)
      startPollingMinions()
      this.stopPollingMinions = stopPollingMinions

      createAndDownloadBlobFile(this.minionCert.certificate, `${queries.defaultLocationName}-certificate.p12`)
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
    async refreshMinions() {
      const queries = useWelcomeQueries()
      await queries.getMinionsByLocId() 
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
  },
  getters: {
    dockerCmd: (state) => {
      const route = useRoute()
      const url = route.fullPath.includes('dev')
        ? 'minion.onms-fb-dev.dev.nonprod.dataservice.opennms.com'
        : 'minion.onms-fb-prod.production.prod.dataservice.opennms.com'
      return `docker run --rm -p 8181:8181 -p 8101:8101 -p 1162:1162/udp -p 8877:8877/udp -p 4729:4729/udp -p 9999:9999/udp -p 162:162/udp -e TZ='America/New_York' -e USE_KUBERNETES="false" -e MINION_GATEWAY_HOST="${url}" -e MINION_GATEWAY_PORT=443 -e MINION_GATEWAY_TLS="true" -e GRPC_CLIENT_KEYSTORE='/opt/karaf/minion.p12' -e GRPC_CLIENT_KEYSTORE_PASSWORD='${state.minionCert.password}'  -e MINION_ID='default'  --mount type=bind,source="pathToFile/default.p12",target="/opt/karaf/minion.p12",readonly opennms/lokahi-minion:latest`
    }
  }
})