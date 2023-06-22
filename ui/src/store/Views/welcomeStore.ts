import { defineStore } from 'pinia'
import * as yup from 'yup'
import {
  CertificateResponse,
  Node
} from '@/types/graphql'
import { createAndDownloadBlobFile } from '@/components/utils'
import router from '@/router'
import { ItemPreviewProps } from '@/components/Common/commonTypes'
import { useLocationMutations } from '../Mutations/locationMutations'
import { useDiscoveryMutations } from '../Mutations/discoveryMutations'
import { useDiscoveryQueries } from '../Queries/discoveryQueries'
import { REGEX_EXPRESSIONS } from '@/components/Discovery/discovery.constants'
import { validationErrorsToStringRecord } from '@/services/validationService'
import { useAppliancesQueries } from '../Queries/appliancesQueries'
import useMinionCmd from '@/composables/useMinionCmd'
import { ComputedRef } from 'vue'
import { useNodeMutations } from '../Mutations/nodeMutations'
import { useLocationQueries } from '../Queries/locationQueries'
import { useNodeQueries } from '../Queries/nodeQueries'
import { useMinionsQueries } from '../Queries/minionsQueries'
import { useCertificateQueries } from '../Queries/certificateQueries'

interface WelcomeStoreState {
  copied: boolean
  copyButtonCopy: string
  doneLoading: boolean
  doneGradient: boolean
  downloading: boolean;
  defaultLocationName: string
  detectedDevice: Partial<Node> | undefined
  devicePreview: ItemPreviewProps
  discoverySubmitted: boolean
  downloadCopy: string
  downloaded: boolean
  firstDiscovery: Record<string, string>
  firstDiscoveryErrors: Record<string, string>
  firstDiscoveryValidation: yup.ObjectSchema<{}>,
  firstLocation: { id: number, location: string }
  invalidForm: boolean
  minionCert: CertificateResponse
  minionCmd: {
    minionDockerCmd: ComputedRef<string>;
    setPassword: (pass: string) => string;
    setMinionId: (minionIdString: string) => string;
    clearMinionCmdVals: () => void;
  }
  minionStatusCopy: string
  minionStatusLoading: boolean
  minionStatusStarted: boolean
  minionStatusSuccess: boolean
  ready: boolean
  refreshing: boolean
  showOnboarding: boolean
  slide: number
  slideOneCollapseVisible: boolean
  slideThreeDisabled: boolean
  validateOnKeyup: boolean
}

export const useWelcomeStore = defineStore('welcomeStore', {
  state: () =>
  ({
    discoverySubmitted: false,
    doneGradient: false,
    doneLoading: false,
    showOnboarding: false,
    minionCert: { password: '', certificate: '' },
    copied: false,
    copyButtonCopy: 'Copy',
    defaultLocationName: 'default',
    detectedDevice: {},
    devicePreview: {
      title: 'Node Discovery',
      loading: false,
      itemTitle: '',
      itemSubtitle: 'Added --/--/--',
      itemStatuses: [
        { title: 'ICMP Latency', status: 'Normal', statusColor: '#cee3ce', statusText: '#0b720c' },
        { title: 'SNMP Uptime', status: 'Normal', statusColor: '#cee3ce', statusText: '#0b720c' }
      ]
    },
    downloadCopy: 'Download',
    downloaded: false,
    downloading: false,
    minionCmd: useMinionCmd(),
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
    firstDiscovery: { name: 'MyFirstDiscovery', ip: '192.168.1.1', communityString: 'public', port: '161' },
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
      const { getAllMinions } = useMinionsQueries()
      const minions = await getAllMinions();
      await this.createDefaultLocation();
      if (minions?.length > 0) {
        onboardingState = false
      } else {
        router.push('/welcome');
      }
      this.showOnboarding = onboardingState
      setTimeout(() => {
        this.ready = true;
      }, 200)
      setTimeout(() => {
        this.doneGradient = true;
      }, 210)
      setTimeout(() => {
        this.doneLoading = true;
      }, 450)
    },
    async createDefaultLocation() {
      const { fetchLocationsForWelcome } = useLocationQueries();
      const locations = await fetchLocationsForWelcome();
      const existingDefault = locations?.find((d: { location: string }) => d.location === 'default');
      if (!existingDefault) {
        const locationMutations = useLocationMutations();
        const { data } = await locationMutations.createLocation({ location: 'default' })
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
    async getFirstNode() {
      const { getDiscoveries } = useDiscoveryQueries();
      const { getNodeDetails } = useNodeQueries();
      await getDiscoveries();
      const details = await getNodeDetails(this.firstDiscovery.name);
      const metric = details?.metrics?.nodeLatency?.data?.result?.[0]?.values?.[0]?.[1]
      if (details.detail && metric) {
        this.devicePreview.itemTitle = details.detail.nodeLabel || ''
        this.devicePreview.itemSubtitle = 'Added ' + new Intl.DateTimeFormat('en-US').format(new Date(details.detail.createTime))
        this.devicePreview.itemStatuses[0].status = details.metrics?.nodeStatus?.status || ''
        this.devicePreview.itemStatuses[0].title = 'Status'
        if (details.metrics?.nodeStatus?.status === 'UP') {
          this.devicePreview.itemStatuses[0].statusColor = '#cee3ce'
          this.devicePreview.itemStatuses[0].statusText = '#0b720c'
        } else {
          this.devicePreview.itemStatuses[0].statusColor = 'rgba(165,2,31,0.3)'
          this.devicePreview.itemStatuses[0].statusText = 'rgba(165,2,31,1)'
        }
        this.devicePreview.itemStatuses[1].status = metric.toString();
        this.devicePreview.itemStatuses[1].title = 'ICMP'
        if (this.devicePreview.itemStatuses[1].status === 'Normal') {
          this.devicePreview.itemStatuses[1].statusColor = '#cee3ce'
          this.devicePreview.itemStatuses[1].statusText = '#0b720c'
        } else {
          this.devicePreview.itemStatuses[1].statusColor = 'rgba(165,2,31,0.3)'
          this.devicePreview.itemStatuses[1].statusText = 'rgba(165,2,31,1)'
        }

        this.devicePreview.loading = false;
      } else {
        setTimeout(this.getFirstNode, 10000)
      }
    },
    async startDiscovery() {
      await this.validateFirstDiscovery();
      if (!this.invalidForm) {
        const { createDiscoveryConfig } = useDiscoveryMutations();
        await createDiscoveryConfig({ request: { ipAddresses: [this.firstDiscovery.ip], locationId: this.firstLocation.id.toString(), name: this.firstDiscovery.name, snmpConfig: { ports: [Number(this.firstDiscovery.port)], readCommunities: [this.firstDiscovery.communityString] } } })

        this.devicePreview.loading = true;
        this.discoverySubmitted = true;
        this.getFirstNode();
      }
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
      let dcmd = this.minionCmd.minionDockerCmd
      if (location.origin.includes('local')) {
        dcmd = `docker run --rm -p 8181:8181 -p 8101:8101 -p 1162:1162/udp -p 8877:8877/udp -p 4729:4729/udp -p 9999:9999/udp -p 162:162/udp -e TZ='America/New_York' -e USE_KUBERNETES="false" -e MINION_GATEWAY_HOST="host.docker.internal" -e MINION_GATEWAY_PORT=1443 -e MINION_GATEWAY_TLS="true" -e GRPC_CLIENT_TRUSTSTORE=/opt/karaf/gateway.crt --mount type=bind,source="${import.meta.env.VITE_MINION_PATH}/target/tmp/server-ca.crt",target="/opt/karaf/gateway.crt",readonly -e GRPC_CLIENT_KEYSTORE='/opt/karaf/minion.p12' -e GRPC_CLIENT_KEYSTORE_PASSWORD='${this.minionCert.password}' -e MINION_ID='default' --mount type=bind,source="${import.meta.env.VITE_MINION_PATH}/target/tmp/${this.defaultLocationName}-certificate.p12",target="/opt/karaf/minion.p12",readonly  -e GRPC_CLIENT_OVERRIDE_AUTHORITY="minion.onmshs.local" -e IGNITE_SERVER_ADDRESSES="localhost" opennms/lokahi-minion:latest`
      }
      return dcmd;
    },
    async downloadClick() {
      const { getMinionCertificate } = useCertificateQueries();
      this.downloadCopy = 'Downloading'
      this.downloading = true;
      setTimeout(async () => {
        this.minionCert = await getMinionCertificate(this.firstLocation.id)
        this.downloading = false;
        this.downloaded = true
        this.downloadCopy = 'Downloaded'
        this.minionCmd.setPassword(this.minionCert.password || '');
        this.minionCmd.setMinionId(this.defaultLocationName);
        createAndDownloadBlobFile(this.minionCert.certificate, `${this.defaultLocationName}-certificate.p12`)

        this.refreshing = true
        this.refreshMinions()

        this.minionStatusLoading = true
        this.minionStatusStarted = true
        this.updateMinionStatusCopy()
      }, 1000)
    },
    async refreshMinions() {
      if (this.refreshing && this.firstLocation.location) {
        const { getAllMinions } = useMinionsQueries();
        const localMinions = await getAllMinions()
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
})
