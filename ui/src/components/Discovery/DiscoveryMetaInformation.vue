<template>
  <div>
    <div :class="['flex-boxes', isICMP ? 'threefer' : 'twofer']">
      <FeatherTextarea
        v-if="isICMP"
        :modelValue="(discovery?.meta as DiscoverySNMPMeta)?.ipRanges"
        @update:modelValue="(e: string) => updateDiscoveryValue('ipRanges',e)"
        :error="discoveryErrors?.ipAddresses"
        label="Enter IP Ranges and/or Subnets"
      />
      <FeatherTextarea
        v-if="isSnmpTrapOrICMPV1"
        :modelValue="(discovery?.meta as DiscoveryTrapMeta)?.communityStrings"
        @update:modelValue="(e: string) => updateDiscoveryValue('communityStrings',e)"
        :error="discoveryErrors?.['meta.communityStrings']"
        label="Enter Community String (optional)"
      />
      <FeatherTextarea
        v-if="isSnmpTrapOrICMPV1"
        :modelValue="(discovery?.meta as DiscoveryTrapMeta)?.udpPorts"
        @update:modelValue="(e: string) => updateDiscoveryValue('udpPorts',e)"
        :error="discoveryErrors?.['meta.']"
        label="Enter UDP Port (optional)  "
      />
    </div>
    <div v-if="isICMPV3">
      <h5>SNMP V3 Security Setting</h5>
      <div class="flex">
        <FeatherInput
          label="Username"
          :modelValue="(discovery?.meta as DiscoverySNMPV3Auth).username"
          :error="discoveryErrors?.username"
          @update:modelValue="(e?: string | number) => updateDiscoveryValue('username',String(e))"
        />
        <FeatherInput
          label="Context"
          :error="discoveryErrors?.context"
          :modelValue="(discovery?.meta as DiscoverySNMPV3Auth).context"
          @update:modelValue="(e?: string | number) => updateDiscoveryValue('context',String(e))"
        />
      </div>
    </div>

    <div v-if="isICMPV3">
      <FeatherTabContainer
        :modelValue="selectedTab"
        @update:modelValue="changeSecurityType"
      >
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
          @update:modelValue="(e?: string | number) => updateDiscoveryValue('password',String(e))"
        />
        <FeatherCheckbox
          :modelValue="(discovery?.meta as DiscoverySNMPV3Auth).usePasswordAsKey"
          @update:modelValue="(e?: boolean) => updateDiscoveryValue('usePasswordAsKey',String(e))"
          >Use password as key</FeatherCheckbox
        >
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
          @update:modelValue="(e?: string | number) => updateDiscoveryValue('privacy',String(e))"
        />
        <FeatherCheckbox
          :modelValue="(discovery?.meta as DiscoverySNMPV3AuthPrivacy).usePrivacyAsKey"
          @update:modelValue="(e?: boolean) => updateDiscoveryValue('usePrivacyAsKey',String(e))"
          >Use password as key</FeatherCheckbox
        >
      </div>
    </div>
    <div v-if="[DiscoveryType.Azure].includes(discovery.type as DiscoveryType)">
      <div class="azure-row">
        <FeatherInput
          label="Client ID"
          :modelValue="(discovery?.meta as DiscoveryAzureMeta).clientId"
          :error="discoveryErrors?.clientId"
          :disabled="isOverallDisabled"
          @update:modelValue="(e?: string | number) => updateDiscoveryValue('clientId',String(e))"
        />
        <FeatherInput
          v-if="!discovery?.id"
          label="Client Secret"
          :error="discoveryErrors?.clientSecret"
          :modelValue="(discovery?.meta as DiscoveryAzureMeta).clientSecret"
          :disabled="isOverallDisabled"
          @update:modelValue="(e?: string | number) => updateDiscoveryValue('clientSecret',String(e))"
        />
      </div>
      <div class="azure-row">
        <FeatherInput
          label="Client Subscription ID"
          :error="discoveryErrors?.subscriptionId"
          :modelValue="(discovery?.meta as DiscoveryAzureMeta).subscriptionId"
          :disabled="isOverallDisabled"
          @update:modelValue="(e?: string | number) => updateDiscoveryValue('subscriptionId',String(e))"
        />
        <FeatherInput
          label="Directory ID"
          :error="discoveryErrors?.directoryId"
          :modelValue="(discovery?.meta as DiscoveryAzureMeta).directoryId"
          :disabled="isOverallDisabled"
          @update:modelValue="(e?: string | number) => updateDiscoveryValue('directoryId',String(e))"
        />
      </div>
    </div>
    <div v-if="[DiscoveryType.WindowsServer].includes(discovery.type as DiscoveryType)"></div>
    <div v-if="[DiscoveryType.ServiceDiscovery].includes(discovery.type as DiscoveryType)">
      <div class="discovery-targets">
        <div class="discovery-title">
          <h5>Enter Discovery Targets</h5>
          <Icon :icon="targetsDiscoveryIcon" />
        </div>
        <FeatherTextarea
          class="input-dicovery"
          v-model.trim="discoveryTargets"
          label="Enter Discovery Targets"
          :maxlength="150"
        >
        </FeatherTextarea>
       </div>
       <div class="discovery-title">
          <h5>Select services to associate with the Discovery</h5>
          <Icon :icon="servicesProtocolIcon" />
       </div>
      <div class="services-protocol">
        <FeatherCheckbox
          v-for="service in services"
          :key="service.label"
          :model-value="selectService.includes(service.label)"
          @update:model-value="(isSelected) => onSelectService(service.label, Boolean(isSelected))"
        >
          {{ service.label }}
        </FeatherCheckbox>
   </div>
  </div>
 </div>
</template>
<script lang="ts" setup>
import { PropType } from 'vue'
import { DiscoveryType } from './discovery.constants'
import {
  NewOrUpdatedDiscovery,
  DiscoverySNMPMeta,
  DiscoveryTrapMeta,
  DiscoverySNMPV3Auth,
  DiscoverySNMPV3AuthPrivacy,
  DiscoveryAzureMeta
} from '@/types/discovery'
import { FeatherTabContainer } from '@featherds/tabs'
import { IIcon } from '@/types'
import Warning from '@featherds/icon/notification/Warning'
const props = defineProps({
  discovery: { type: Object as PropType<NewOrUpdatedDiscovery>, default: () => ({}) },
  discoveryErrors: { type: Object as PropType<Record<string, string>>, default: () => ({}) },
  updateDiscoveryValue: { type: Function as PropType<(key: string, value: string) => void>, default: () => ({}) }
})
const isOverallDisabled = computed(() => !!(props.discovery.type === DiscoveryType.Azure && props.discovery.id))
const selectedTab = ref()
const discoveryTargets = ref()
const selectService = ref<string[]>([])
const services = reactive([
  { label: 'HTTP (Port 80)' },
  { label: 'HTTPS (Port 443)' },
  { label: 'NTP' },
  { label: 'Telnet' },
  { label: 'DNS' },
  { label: 'SSH' }
])

const targetsDiscoveryIcon: IIcon = {
  image: markRaw(Warning),
  tooltip: 'Discovery Targets',
  size: 1.5,
  cursorHover: true
}
const servicesProtocolIcon: IIcon = {
  image: markRaw(Warning),
  tooltip: 'Services associate with discovery',
  size: 1.5,
  cursorHover: true
}

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

const onSelectService = (label: string, isSelected: boolean) => {
  if (isSelected) {
    selectService.value.push(label)
  } else {
    const index = selectService.value.indexOf(label)
    if (index > -1) {
      selectService.value.splice(index, 1)
    }
  }
}
</script>
<style lang="scss" scoped>
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
.services-protocol {
  display: flex;
  justify-content: space-between;
  align-items: center;
  grid-column-gap: 5px;
  margin: 25px 0;
}
.discovery-targets {
  margin-top: 20px;
}
.discovery-title {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  grid-column-gap:5px;
  margin:10px 0;
}
</style>
