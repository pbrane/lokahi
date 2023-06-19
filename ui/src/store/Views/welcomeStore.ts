import { defineStore } from 'pinia'
import {
  CertificateResponse,
  FindDevicesForWelcomeDocument,
  FindLocationsForWelcomeDocument,
  Node
} from '@/types/graphql'
import { createAndDownloadBlobFile } from '@/components/utils'
import { fncVoid } from '@/types'
import { useNodeMutations } from '../Mutations/nodeMutations'
import router from '@/router'
import { getMinionsByLocationId } from '@/services/minionService'
import { CertificateService, MinionService, NodeService, QueryService } from '@/services'

interface WelcomeStoreState {
  showOnboarding: boolean
  minionCert: CertificateResponse
  copied: boolean
  defaultLocationName: string
  detectedDevice: Partial<Node> | undefined
  devicePreview: ItemPreviewProps
  downloaded: boolean
  firstDevice: Record<string, string>
  minionStatusCopy: string
  minionStatusLoading: boolean
  minionStatusStarted: boolean
  minionStatusSuccess: boolean
  refreshing: boolean
  slide: number
  slideOneCollapseVisible: boolean
  slideTwoDisabled: boolean
  slideThreeDisabled: boolean
}

export const useWelcomeStore = defineStore('welcomeStore', {
  state: () =>
    ({
      showOnboarding: false,
      minionCert: { password: '', certificate: '' },
      copied: false,
      defaultLocationName: 'TestLocation',
      detectedDevice: {},
      devicePreview: {
        title: 'Device Preview',
        loading: false,
        itemTitle: 'Minion Gateway',
        itemSubtitle: 'Added --/--/--',
        itemStatuses: [
          { title: 'ICMP Latency', status: 'Normal', statusColor: '#cee3ce', statusText: '#0b720c' },
          { title: 'SNMP Uptime', status: 'Normal', statusColor: '#cee3ce', statusText: '#0b720c' }
        ]
      },
      downloaded: false,
      minionStatusCopy: 'Waiting for the Docker Install Command to be complete.',
      minionStatusLoading: false,
      minionStatusSuccess: false,
      minionStatusStarted: false,
      refreshing: false,
      slide: 1,
      slideOneCollapseVisible: false,
      slideTwoDisabled: false,
      firstDevice: { name: '', ip: '', communityString: '', port: '' },
      slideThreeDisabled: false
    } as WelcomeStoreState),
  actions: {
    async init() {
      let onboardingState = true
      let detectedDevice = null
      const locations = await QueryService.executeQuery({ query: FindLocationsForWelcomeDocument })

      if (locations.length > 0 || locations[0]?.toLowerCase() !== this.defaultLocationName.toLowerCase()) {
        const minions: [] = await getMinionsByLocationId(locations[0].id)
        if (minions.length > 0) {
          const devices = await QueryService.executeQuery({ query: FindDevicesForWelcomeDocument })
          detectedDevice = devices?.find((d: any) => d.ipInterfaces?.find((e: any) => e.snmpPrimary))
          this.firstDevice = detectedDevice
          if (detectedDevice) {
            onboardingState = false
          }
        }
      }

      this.showOnboarding = onboardingState
    },
    nextSlide() {
      this.slide = this.slide + 1
      if (this.slide === 2) {
        this.loadDevicePreview()
      }
      if (this.slide === 3) {
        this.refreshing = false
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
      router.push('Dashboard')
    },
    async startMonitoring() {
      const tags = [{ name: 'default' }]

      if (this.detectedDevice) {
        const { ipInterfaces, id } = this.detectedDevice

        if (ipInterfaces?.[0]?.ipAddress === this.firstDevice.ip) {
          await NodeService.addTagsToNodes(id, tags)
        }
      }

      if (this.firstDevice.name && this.firstDevice.ip) {
        await NodeService.addDeviceToNode(this.firstDevice.name, this.firstDevice.ip, this.defaultLocationName, tags)
      }

      router.push('Dashboard')
    },
    copyDockerClick() {
      navigator.clipboard.writeText(this.dockerCmd()).then(() => (this.copied = true))
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
    dockerCmd() {
      const url = location.origin.includes('dev')
        ? 'minion.onms-fb-dev.dev.nonprod.dataservice.opennms.com'
        : 'minion.onms-fb-prod.production.prod.dataservice.opennms.com'
      return `docker run --rm -p 8181:8181 -p 8101:8101 -p 1162:1162/udp -p 8877:8877/udp -p 4729:4729/udp -p 9999:9999/udp -p 162:162/udp -e TZ='America/New_York' -e USE_KUBERNETES="false" -e MINION_GATEWAY_HOST="${url}" -e MINION_GATEWAY_PORT=443 -e MINION_GATEWAY_TLS="true" -e GRPC_CLIENT_KEYSTORE='/opt/karaf/minion.p12' -e GRPC_CLIENT_KEYSTORE_PASSWORD='${this.minionCert.password}'  -e MINION_ID='${this.defaultLocationName}'  --mount type=bind,source="pathToFile/${this.defaultLocationName}-certificate.p12",target="/opt/karaf/minion.p12",readonly opennms/lokahi-minion:latest`
    },
    async downloadClick() {
      this.minionCert = await CertificateService.downloadCertificateBundle(this.defaultLocationName)
      createAndDownloadBlobFile(this.minionCert.certificate, `${this.defaultLocationName}-certificate.p12`)

      this.refreshing = true
      this.refreshMinions()

      this.downloaded = true
      this.minionStatusLoading = true
      this.minionStatusStarted = true
      this.updateMinionStatusCopy()
    },
    async refreshMinions() {
      if (this.refreshing) {
        const localMinions = await MinionService.getMinionsByLocationId(this.defaultLocationName)
        if (localMinions) {
          this.refreshing = false
          this.minionStatusSuccess = true
        } else {
          setTimeout(this.refreshMinions, 10000)
        }
      }
    },
    toggleSlideOneCollapse() {
      this.slideOneCollapseVisible = !this.slideOneCollapseVisible
    },
    updateFirstDevice(key: string, value: string | number | undefined) {
      if (typeof value === 'string') {
        this.firstDevice[key] = value

        // it is not possible to get a status unless a device is already added and monitored.
        // is this preview supposed to be 'faked'?

        // if (value){

        //   this.devicePreview.loading = true
        //   this.devicePreview.itemStatuses[0].status = 'Critical'
        //   this.devicePreview.itemStatuses[0].statusColor = 'rgba(165,2,31,0.3)'
        //   this.devicePreview.itemStatuses[0].statusText = 'rgba(165,2,31,1)'
        //   setTimeout(() => {
        //     this.devicePreview.loading = false
        //   }, 1000)
        // }
      }
    }
  },
  getters: {}
})
