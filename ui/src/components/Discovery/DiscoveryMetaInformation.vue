<template>
  <div>
    <div :class="['flex-boxes', isICMP ? 'threefer' : 'twofer']">
      <FeatherTextarea v-if="isICMP"
        :modelValue="(discovery?.meta as DiscoverySNMPMeta)?.ipRanges"
        @update:modelValue="(e: string) => updateDiscoveryValue('ipRanges', e)"
        :error="discoveryErrors?.ipAddresses"
        label="Enter IP Ranges and/or Subnets" />
      <FeatherTextarea v-if="isSnmpTrapOrICMPV1"
        :modelValue="(discovery?.meta as DiscoveryTrapMeta)?.communityStrings"
        @update:modelValue="(e: string) => updateDiscoveryValue('communityStrings', e)"
        :error="discoveryErrors?.['meta.communityStrings']"
        label="Enter Community String (optional)" />
      <FeatherTextarea v-if="isSnmpTrapOrICMPV1"
        :modelValue="(discovery?.meta as DiscoveryTrapMeta)?.udpPorts"
        @update:modelValue="(e: string) => updateDiscoveryValue('udpPorts', e)"
        :error="discoveryErrors?.['meta.']"
        label="Enter UDP Port (optional)" />
    </div>
    <div v-if="isICMPV3">
      <h5>SNMP V3 Security Setting</h5>
      <div class="flex">
        <FeatherInput
          label="Username"
          :modelValue="(discovery?.meta as DiscoverySNMPV3Auth).username"
          :error="discoveryErrors?.username"
          @update:modelValue="(e?: string | number) => updateDiscoveryValue('username', String(e))" />
        <FeatherInput
          label="Context"
          :error="discoveryErrors?.context"
          :modelValue="(discovery?.meta as DiscoverySNMPV3Auth).context"
          @update:modelValue="(e?: string | number) => updateDiscoveryValue('context', String(e))" />
      </div>
    </div>

    <div v-if="isICMPV3">
      <FeatherTabContainer :modelValue="selectedTab" @update:modelValue="changeSecurityType">
        <template v-slot:tabs>
          <FeatherTab>No Auth</FeatherTab>
          <FeatherTab>Auth</FeatherTab>
          <FeatherTab>Auth + Privacy</FeatherTab>
        </template>
      </FeatherTabContainer>
    </div>
    <div v-if="isICMPV3WithPass">
      <div class="flex">
        <AtomicAutocomplete inputLabel="Auth" />
        <FeatherInput
          class="password"
          type="password"
          label="Password"
          :error="discoveryErrors?.password"
          :modelValue="(discovery?.meta as DiscoverySNMPV3Auth).password"
          @update:modelValue="(e?: string | number) => updateDiscoveryValue('password', String(e))" />
        <FeatherCheckbox
          :modelValue="(discovery?.meta as DiscoverySNMPV3Auth).usePasswordAsKey"
          @update:modelValue="(e?: boolean) => updateDiscoveryValue('usePasswordAsKey', String(e))"
          >Use password as key</FeatherCheckbox>
      </div>
    </div>
    <div v-if="[DiscoveryType.ICMPV3AuthPrivacy].includes(discovery.type as DiscoveryType)">
      <div class="flex">
        <AtomicAutocomplete inputLabel="Privacy" />
        <FeatherInput
          class="password"
          type="password"
          label="Password"
          :error="discoveryErrors?.privacy"
          :modelValue="(discovery?.meta as DiscoverySNMPV3AuthPrivacy).privacy"
          @update:modelValue="(e?: string | number) => updateDiscoveryValue('privacy', String(e))" />
        <FeatherCheckbox
          :modelValue="(discovery?.meta as DiscoverySNMPV3AuthPrivacy).usePrivacyAsKey"
          @update:modelValue="(e?: boolean) => updateDiscoveryValue('usePrivacyAsKey', String(e))"
          >Use password as key</FeatherCheckbox>
      </div>
    </div>
    <div v-if="[DiscoveryType.Azure].includes(discovery.type as DiscoveryType)">
      <div class="azure-row">
        <FeatherInput
          label="Client ID"
          :modelValue="(discovery?.meta as DiscoveryAzureMeta).clientId"
          :error="discoveryErrors?.clientId"
          :disabled="isOverallDisabled"
          @update:modelValue="(e?: string | number) => updateDiscoveryValue('clientId', String(e))" />
        <FeatherInput
          v-if="!discovery?.id"
          label="Client Secret"
          :error="discoveryErrors?.clientSecret"
          :modelValue="(discovery?.meta as DiscoveryAzureMeta).clientSecret"
          :disabled="isOverallDisabled"
          @update:modelValue="(e?: string | number) => updateDiscoveryValue('clientSecret', String(e))" />
      </div>
      <div class="azure-row">
        <FeatherInput
          label="Client Subscription ID"
          :error="discoveryErrors?.subscriptionId"
          :modelValue="(discovery?.meta as DiscoveryAzureMeta).subscriptionId"
          :disabled="isOverallDisabled"
          @update:modelValue="(e?: string | number) => updateDiscoveryValue('subscriptionId', String(e))" />
        <FeatherInput
          label="Directory ID" :error="discoveryErrors?.directoryId"
          :modelValue="(discovery?.meta as DiscoveryAzureMeta).directoryId"
          :disabled="isOverallDisabled"
          @update:modelValue="(e?: string | number) => updateDiscoveryValue('directoryId', String(e))" />
      </div>
    </div>
    <div v-if="[DiscoveryType.ServiceDiscovery].includes(discovery.type as DiscoveryType)">
      <div class="discovery-targets">
        <h4>Enter Discovery Targets&nbsp;&nbsp;
          <FeatherPopover :pointer-alignment="PointerAlignment.center" :placement="PopoverPlacement.top">
            <template #default>
              <div>
                <h4>Discovery Targets</h4>
                <p>Enter the FQDN or IP address/subnet/ranges of servers to Discover.</p>
              </div>
            </template>
            <template #trigger="{ attrs, on }">
              <FeatherIcon class="info-icon" v-bind="attrs" v-on="on" :icon="Info" />
            </template>
          </FeatherPopover>
        </h4>
        <FeatherTextarea
          class="input-discovery"
          label="Enter Discovery Targets"
          :error="discoveryErrors?.discoveryTargets"
          :modelValue="(discovery?.meta as DiscoveryServicesMeta)?.discoveryTargets"
          @update:modelValue="(e?: string) => updateDiscoveryValue('discoveryTargets', e)" rows="5">
        </FeatherTextarea>
      </div>
      <div class="discovery-targets">
        <h4>Enter Discovery Targets&nbsp;&nbsp;
          <FeatherPopover :pointer-alignment="PointerAlignment.center" :placement="PopoverPlacement.top">
            <template #default>
              <p>Services associated with discovery.</p>
            </template>
            <template #trigger="{ attrs, on }">
              <FeatherIcon class="info-icon" v-bind="attrs" v-on="on" :icon="Info" />
            </template>
          </FeatherPopover>
        </h4>
      </div>
      <div class="services-protocol">
        <FeatherCheckbox
          v-for="service in services" :key="service.label"
          :model-value="isServiceSelected(service.label)"
          @update:model-value="(isSelected: any) => onSelectService(service.label, Boolean(isSelected), service.type, service.port)">
          {{ service.label }}
        </FeatherCheckbox>
      </div>
    </div>
    <div v-if="[DiscoveryType.WindowsServer].includes(discovery.type as DiscoveryType)">
      <div class="window-protocol-row">
        <div class="col">
          <FeatherSelect
            label="Select Windows Protocol"
            text-prop="name"
            :options="windowProtocolOptions"
            @update:model-value="setWindowsProtocol" />
        </div>
        <div class="col"></div>
      </div>
      <div class="credential-row">
        <div class="col">
          <FeatherInput
            label="Username"
            :error="discoveryErrors?.username"
            :modelValue="(discovery?.meta as DiscoveryWindowServerMeta).username"
            @update:modelValue="(e?: string | number) => updateDiscoveryValue('username', String(e ?? ''))" />
        </div>
        <div class="col">
          <FeatherInput
            type="password"
            label="Password"
            :error="discoveryErrors?.password"
            :modelValue="(discovery?.meta as DiscoveryWindowServerMeta).password"
            @update:modelValue="(e?: string | number) => updateDiscoveryValue('password', String(e ?? ''))" />
        </div>
      </div>
      <div class="discovery-targets">
        <h4>Enter Discovery Targets&nbsp;&nbsp;
          <FeatherPopover :pointer-alignment="PointerAlignment.center" :placement="PopoverPlacement.top">
            <template #default>
              <div>
                <h4>Discovery Targets</h4>
                <p>Enter the FQDN or IP address/subnet/ranges of servers to Discover.</p>
              </div>
            </template>
            <template #trigger="{ attrs, on }">
              <FeatherIcon class="info-icon" v-bind="attrs" v-on="on" :icon="Info" />
            </template>
          </FeatherPopover>
        </h4>
        <FeatherTextarea
          :error="discoveryErrors?.discoveryTarget"
          :modelValue="(discovery?.meta as DiscoveryWindowServerMeta)?.discoveryTarget"
          @update:modelValue="(e: string) => updateDiscoveryValue('discoveryTarget', e ?? '')"
          label="Enter Discovery Targets" />
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
import {
  DiscoveryAzureMeta,
  DiscoverySNMPMeta,
  DiscoverySNMPV3Auth,
  DiscoverySNMPV3AuthPrivacy,
  DiscoveryTrapMeta,
  DiscoveryWindowServerMeta,
  NewOrUpdatedDiscovery,
  DiscoveryServicesMeta
} from '@/types/discovery'
import Info from '@featherds/icon/action/Info'
import { ISelectItemType } from '@featherds/select'
import { FeatherTabContainer } from '@featherds/tabs'
import { PointerAlignment, PopoverPlacement } from '@featherds/tooltip'
import { PropType } from 'vue'
import { DiscoveryType } from './discovery.constants'
import { useDiscoveryStore } from '@/store/Views/discoveryStore'
const discoveryStore = useDiscoveryStore()
const props = defineProps({
  discovery: { type: Object as PropType<NewOrUpdatedDiscovery>, default: () => ({}) },
  discoveryErrors: { type: Object as PropType<Record<string, string>>, default: () => ({}) },
  updateDiscoveryValue: { type: Function as PropType<(key: string, value: any) => void>, default: () => ({}) }
})

const isServiceSelected = (label: string): boolean => {
  return (props.discovery?.meta as DiscoveryServicesMeta)?.services?.label === label
}

const onSelectService = (label: string, isSelected: boolean, type: string | undefined, port: number | undefined) => {
  if (isSelected) {
    discoveryStore.selectedDiscovery = {
      ...discoveryStore.selectedDiscovery,
      meta: {
        ...(discoveryStore.selectedDiscovery.meta || {}),
        services: { label, type, port }
      }
    }
  } else {
    discoveryStore.selectedDiscovery = {
      ...discoveryStore.selectedDiscovery,
      meta: {
        ...(discoveryStore.selectedDiscovery.meta || {}),
        services: {}
      }
    }
  }
}

const isOverallDisabled = computed(() => !!(props.discovery.type === DiscoveryType.Azure && props.discovery.id))
const selectedTab = ref()
const services = reactive([
  { label: 'HTTP (Port 80)', type: 'HTTP', port: 80 },
  { label: 'HTTPS (Port 443)', type: 'HTTPS', port: 443 },
  { label: 'NTP' },
  { label: 'Telnet' },
  { label: 'DNS' },
  { label: 'SSH' }
])

const changeSecurityType = (type?: number) => {
  let setType = DiscoveryType.ICMPV3NoAuth
  if (type === 1) {
    setType = DiscoveryType.ICMPV3Auth
  } else if (type === 2) {
    setType = DiscoveryType.ICMPV3AuthPrivacy
  }
  selectedTab.value = type
  props.updateDiscoveryValue('type', setType)
}
const isSnmpTrapOrICMPV1 = computed(() =>
  [DiscoveryType.ICMP, DiscoveryType.SyslogSNMPTraps].includes(props.discovery.type as DiscoveryType)
)
const isICMP = computed(() =>
  [DiscoveryType.ICMP, DiscoveryType.ICMPV3Auth, DiscoveryType.ICMPV3AuthPrivacy, DiscoveryType.ICMPV3NoAuth].includes(
    props.discovery.type as DiscoveryType
  )
)
const isICMPV3 = computed(() =>
  [DiscoveryType.ICMPV3Auth, DiscoveryType.ICMPV3AuthPrivacy, DiscoveryType.ICMPV3NoAuth].includes(
    props.discovery.type as DiscoveryType
  )
)
const isICMPV3WithPass = computed(() =>
  [DiscoveryType.ICMPV3Auth, DiscoveryType.ICMPV3AuthPrivacy].includes(props.discovery.type as DiscoveryType)
)
const windowProtocolOptions = [
  { id: 1, name: 'WMI' }
]
const setWindowsProtocol = (selected: ISelectItemType | undefined) => {
  if (selected?.id) {
    props.updateDiscoveryValue('windowsProtocol', windowProtocolOptions.find((item) => item.id === selected.id)?.name ?? '')
  }
}
</script>
<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@/styles/mediaQueriesMixins';

.azure-row {
  display: flex;
  width: 100%;
  gap: 2%;

  > :deep(div) {
    flex-basis: 50%;
  }
}

.inline {
  display: inline-block;
  max-width: calc(33% - 8px);
  margin-right: 12px;
  width: 100%;
}

.zip {
  display: inline;
}

.flex {
  display: flex;
  gap: 2%;

  :deep(.feather-input-container) {
    flex-basis: 50%;
  }
}

.password {
  min-width: 300px;
  margin-left: 12px;
  margin-right: 12px;
}

.flex-boxes {
  width: 100%;
  display: flex;
  gap: 2%;
}

.flex-boxes.threefer {
  :deep(.feather-textarea-container) {
    flex-basis: 33.333%;
  }
}

.flex-boxes.twofer {
  :deep(.feather-textarea-container) {
    flex-basis: 50%;
  }
}

.credential-row,
.window-protocol-row {
  @include mediaQueriesMixins.screen-lg {
    display: flex;
    gap: var(variables.$spacing-xl);

    .col {
      flex: 1;
    }
  }
}

.discovery-targets {
  h4 {
    display: flex;
    align-items: center;
    margin-bottom: 10px;

    .info-icon {
      font-size: 1.5rem;
    }
  }
}

.services-protocol {
  display: flex;
  justify-content: space-between;
  align-items: center;
  grid-column-gap: 5px;
  margin: 25px 0;
}

.discovery-title {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  grid-column-gap: 5px;
  margin: 10px 0;
}
</style>
