import { defineStore } from 'pinia'
import * as yup from 'yup'
import {
  CertificateResponse,
  FindDevicesForWelcomeDocument,
  Node
} from '@/types/graphql'
import { createAndDownloadBlobFile } from '@/components/utils'
import router from '@/router'
import { CertificateService, LocationService, MinionService, NodeService, QueryService } from '@/services'
import { WelcomeLocations } from '@/services/locationService'
import { ItemPreviewProps } from '@/components/Common/commonTypes'
import { useLocationMutations } from '../Mutations/locationMutations'
import { useDiscoveryMutations } from '../Mutations/discoveryMutations'
import { useDiscoveryQueries } from '../Queries/discoveryQueries'
import { REGEX_EXPRESSIONS } from '@/components/Discovery/discovery.constants'
import { validationErrorsToStringRecord } from '@/services/validationService'
import { useAppliancesQueries } from '../Queries/appliancesQueries'
import { getNodeDetails } from '@/services/nodeService'

interface WelcomeStoreState {
  showOnboarding: boolean
  minionCert: CertificateResponse
  copied: boolean
  defaultLocationName: string
  detectedDevice: Partial<Node> | undefined
  devicePreview: ItemPreviewProps
  discoverySubmitted: boolean
  downloaded: boolean
  firstDiscovery: Record<string, string>
  firstDiscoveryErrors: Record<string, string>
  firstDiscoveryValidation: yup.ObjectSchema<{}>,
  firstLocation: WelcomeLocations
  invalidForm: boolean
  minionStatusCopy: string
  minionStatusLoading: boolean
  minionStatusStarted: boolean
  minionStatusSuccess: boolean
  ready: boolean
  refreshing: boolean
  slide: number
  slideOneCollapseVisible: boolean
  slideThreeDisabled: boolean
  validateOnKeyup: boolean
}

export const useWelcomeStore = defineStore('welcomeStore', {
  state: () =>
  ({
    discoverySubmitted: false,
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
    firstLocation: { id: -1, location: '' },
    invalidForm: true,
    minionStatusCopy: 'Waiting for the Docker Install Command to be complete.',
    minionStatusLoading: false,
    minionStatusSuccess: false,
    minionStatusStarted: false,
    ready: false,
    refreshing: false,
    slide: 1,
    slideOneCollapseVisible: false,
    firstDiscovery: { name: '', ip: '', communityString: 'public', port: '161' },
    firstDiscoveryErrors: { name: '', ip: '', communityString: '', port: '' },
    firstDiscoveryValidation: yup.object().shape({
      name: yup.string().required("Please enter a name"),
      ip: yup.string().required("Please enter an IP").matches(new RegExp(REGEX_EXPRESSIONS.IP[0]), 'Must be a valid IP'),
      communityString: yup.string(),
      port: yup.number()
    }).required(),
    slideThreeDisabled: true,
    validateOnKeyup: false
  } as WelcomeStoreState),
  actions: {
    async init() {
      let onboardingState = true
      const minions = await MinionService.getAllMinions();
      if (minions?.length > 0) {
        onboardingState = false
      } else {
        router.push('/welcome');
        this.createDefaultLocation();
      }
      this.showOnboarding = onboardingState
      setTimeout(() => {
        this.ready = true;
      }, 150)
    },
    async createDefaultLocation() {
      const locations = await LocationService.getLocationsForWelcome();
      const existingDefault = locations?.find((d) => d.location === 'Default' || d.location === 'TestLocation');
      if (!existingDefault) {
        const locationMutations = useLocationMutations();
        const { data } = await locationMutations.createLocation({ location: 'Default' })
        if (data?.location && data?.id) {
          this.firstLocation = { location: data.location, id: data.id }
        }
      } else {
        this.firstLocation = existingDefault;
      }
    },
    nextSlide() {
      this.slide = this.slide + 1
      if (this.slide === 3) {
        this.loadDevicePreview()
      }
      this.scrollTop()
    },
    loadDevicePreview() {
      this.devicePreview.loading = true
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

    async validateFirstDiscovery() {
      try {
        await this.firstDiscoveryValidation.validate(this.firstDiscovery, { abortEarly: false });
        this.firstDiscoveryErrors = { ip: '', name: '', communityString: '', port: '' }
        this.invalidForm = false;
      } catch (e) {
        this.invalidForm = true;
        this.firstDiscoveryErrors = validationErrorsToStringRecord<{ ip: string, name: string, communityString?: string, port?: string }>(e as yup.ValidationError);
        this.validateOnKeyup = true;
      }
    },
    async startDiscovery() {
      await this.validateFirstDiscovery();
      if (!this.invalidForm) {
        const { createDiscoveryConfig } = useDiscoveryMutations();
        const { getDiscoveries } = useDiscoveryQueries();
        await createDiscoveryConfig({ request: { ipAddresses: [this.firstDiscovery.ip], location: this.firstLocation.location, name: this.firstDiscovery.name, snmpConfig: { ports: [Number(this.firstDiscovery.port)], readCommunities: [this.firstDiscovery.communityString] } } })
        this.devicePreview.loading = true;
        this.discoverySubmitted = true;
        const responses = await getDiscoveries();

        this.devicePreview.loading = true;
        const myDiscovery = responses.data?.listActiveDiscovery?.slice(-1);
        this.devicePreview.itemStatuses = [{ status: '', title: '', statusColor: '', statusText: '' }]
        getNodeDetails();
        this.devicePreview.itemStatuses[0].status = 'Normal'
        this.devicePreview.itemStatuses[0].title = 'ICMP'
        this.devicePreview.itemStatuses[0].statusColor = 'rgba(165,2,31,0.3)'
        this.devicePreview.itemStatuses[0].statusText = 'rgba(165,2,31,1)'
        this.devicePreview.loading = false;
        //   setTimeout(() => {
        //     this.devicePreview.loading = false
        //   }, 1000)
        console.log('MY DISCOVERY!', myDiscovery?.[0])
      }
    },
    async startMonitoring() {
      const tags = [{ name: 'default' }]

      if (this.detectedDevice) {
        const { ipInterfaces, id } = this.detectedDevice

        if (ipInterfaces?.[0]?.ipAddress === this.firstDiscovery.ip) {
          await NodeService.addTagsToNodes(id, tags)
        }
      }

      if (this.firstDiscovery.name && this.firstDiscovery.ip) {
        await NodeService.addDeviceToNode(this.firstDiscovery.name, this.firstDiscovery.ip, this.defaultLocationName, tags)
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
      let dcmd = `docker run --rm -p 8181:8181 -p 8101:8101 -p 1162:1162/udp -p 8877:8877/udp -p 4729:4729/udp -p 9999:9999/udp -p 162:162/udp -e TZ='America/New_York' -e USE_KUBERNETES="false" -e MINION_GATEWAY_HOST="${url}" -e MINION_GATEWAY_PORT=443 -e MINION_GATEWAY_TLS="true" -e GRPC_CLIENT_KEYSTORE='/opt/karaf/minion.p12' -e GRPC_CLIENT_KEYSTORE_PASSWORD='${this.minionCert.password}'  -e MINION_ID='${this.defaultLocationName}'  --mount type=bind,source="pathToFile/${this.defaultLocationName}-certificate.p12",target="/opt/karaf/minion.p12",readonly opennms/lokahi-minion:latest`
      if (location.origin.includes('local')) {
        dcmd = `docker run --rm -p 8181:8181 -p 8101:8101 -p 1162:1162/udp -p 8877:8877/udp -p 4729:4729/udp -p 9999:9999/udp -p 162:162/udp -e TZ='America/New_York' -e USE_KUBERNETES="false" -e MINION_GATEWAY_HOST="host.docker.internal" -e MINION_GATEWAY_PORT=1443 -e MINION_GATEWAY_TLS="true" -e GRPC_CLIENT_TRUSTSTORE=/opt/karaf/gateway.crt --mount type=bind,source="${import.meta.env.VITE_MINION_PATH}/target/tmp/server-ca.crt",target="/opt/karaf/gateway.crt",readonly -e GRPC_CLIENT_KEYSTORE='/opt/karaf/minion.p12' -e GRPC_CLIENT_KEYSTORE_PASSWORD='${this.minionCert.password}' -e MINION_ID='Default' --mount type=bind,source="${import.meta.env.VITE_MINION_PATH}/target/tmp/TestLocation-certificate.p12",target="/opt/karaf/minion.p12",readonly  -e GRPC_CLIENT_OVERRIDE_AUTHORITY="minion.onmshs.local" -e IGNITE_SERVER_ADDRESSES="localhost" opennms/lokahi-minion:latest`
      }
      return dcmd;
    },
    async downloadClick() {
      this.minionCert = await CertificateService.downloadCertificateBundle(this.firstLocation.id)
      createAndDownloadBlobFile(this.minionCert.certificate, `${this.defaultLocationName}-certificate.p12`)

      this.refreshing = true
      this.refreshMinions()

      this.downloaded = true
      this.minionStatusLoading = true
      this.minionStatusStarted = true
      this.updateMinionStatusCopy()
    },
    async refreshMinions() {
      console.log('refreshing!', this.refreshing, this.firstLocation.location)
      if (this.refreshing && this.firstLocation.location) {
        const localMinions = await MinionService.getAllMinions()
        if (localMinions?.length > 0) {
          this.refreshing = false
          this.minionStatusSuccess = true
          this.minionStatusLoading = false
          this.updateMinionStatusCopy();
        } else {
          setTimeout(this.refreshMinions, 10000)
        }
      }
    },
    toggleSlideOneCollapse() {
      this.slideOneCollapseVisible = !this.slideOneCollapseVisible
    },
    updateFirstDiscovery(key: string, value: string | number | undefined) {
      if (typeof value === 'string') {
        this.firstDiscovery[key] = value

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
      if (this.validateOnKeyup) {
        this.validateFirstDiscovery();
      }
    }
  },
  getters: {}
})
