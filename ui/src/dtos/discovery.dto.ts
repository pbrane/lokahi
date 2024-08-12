import { DiscoveryType, REGEX_EXPRESSIONS } from '@/components/Discovery/discovery.constants'
import { validationErrorsToStringRecord } from '@/services/validationService'
import { DiscoveryAzureMeta, DiscoveryServicesMeta, DiscoverySNMPMeta, DiscoveryTrapMeta, DiscoveryWindowServerMeta, NewOrUpdatedDiscovery, ServerDiscoveries } from '@/types/discovery'
import * as yup from 'yup'

export const splitStringOnSemiCommaOrDot = (inString?: string) => {
  return inString?.split(/[;,]+/) ?? []
}

export const discoveryFromClientToServer = (discovery: NewOrUpdatedDiscovery) => {
  if (discovery.type === DiscoveryType.Azure) {
    return discoveryFromAzureClientToServer(discovery)
  }

  if (discovery.type === DiscoveryType.ICMP) {
    return discoveryFromActiveClientToServer(discovery)
  }

  if (discovery.type === DiscoveryType.SyslogSNMPTraps) {
    return discoveryFromTrapClientToServer(discovery)
  }

  if (discovery.type === DiscoveryType.WindowsServer) {
    return discoveryFromWindowsServerClientToServer(discovery)
  }
  if (discovery.type === DiscoveryType.ServiceDiscovery) {
    return discoveryFromServicesServerClientToServer(discovery)
  }
  return {}
}

export const discoveryFromActiveClientToServer = (discovery: NewOrUpdatedDiscovery) => {
  const meta = discovery.meta as DiscoverySNMPMeta
  return {
    id: discovery.id,
    ipAddresses: splitStringOnSemiCommaOrDot(meta.ipRanges),
    locationId: discovery.locations?.[0]?.id,
    name: discovery.name,
    snmpConfig: {ports: splitStringOnSemiCommaOrDot(meta.udpPorts), readCommunities: splitStringOnSemiCommaOrDot(meta.communityStrings)},
    tags: discovery.tags?.map((t) => ({ name: t.name }))
  }
}

export const discoveryFromAzureClientToServer = (discovery: NewOrUpdatedDiscovery) => {
  const meta = discovery.meta as DiscoveryAzureMeta
  return {
    id: discovery.id,
    locationId: discovery.locations?.[0]?.id,
    name: discovery.name,
    tags: discovery.tags?.map((t) => ({ name: t.name })),
    clientId: meta.clientId,
    subscriptionId: meta.subscriptionId,
    clientSecret: meta.clientSecret,
    directoryId: meta.directoryId
  }
}

export const discoveryFromTrapClientToServer = (discovery: NewOrUpdatedDiscovery) => {
  const meta = discovery.meta as DiscoveryTrapMeta
  return {
    id: discovery.id,
    locationId: discovery.locations?.[0]?.id,
    name: discovery.name,
    snmpPorts: splitStringOnSemiCommaOrDot(meta.udpPorts),
    snmpCommunities: splitStringOnSemiCommaOrDot(meta.communityStrings),
    tags: discovery.tags?.map((t) => ({name: t.name}))
  }
}

export const discoveryFromServicesServerClientToServer = (discovery: NewOrUpdatedDiscovery) => {
  const meta = discovery.meta as DiscoveryServicesMeta
  return {
    name: discovery.name,
    type: meta.services.type,
    config: `{"inetAddress":"${meta.discoveryTargets}","responseCode":"200-201","url":"${meta.services.type?.toLocaleLowerCase()}://${meta.discoveryTargets}/","ports":[${meta.services?.port}],"hostName":"${meta.discoveryTargets}"}`,
    locationId: discovery.locations?.[0]?.id
  }
}

export const discoveryFromWindowsServerClientToServer = (discovery: NewOrUpdatedDiscovery) => {
  const meta = discovery.meta as DiscoveryWindowServerMeta
  return {
    id: discovery.id,
    locationId: discovery.locations?.[0]?.id,
    name: discovery.name,
    username: meta.username,
    password: meta.password,
    discoveryTarget: meta.discoveryTarget,
    windowsProtocol: meta.windowsProtocol
  }
}

export const sortDiscoveriesByName = (a: { name?: string }, b: { name?: string }) => {
  let ret = 0

  if (!a.name) {
    a.name = ''
  }
  if (!b.name) {
    b.name = ''
  }

  if (a.name < b.name) {
    ret = -1
  }
  if (a.name > b.name) {
    ret = 1
  }

  return ret
}

export const discoveryFromServerToClient = (dataIn: ServerDiscoveries, locations: Array<{ id: number }>) => {
  let combined: Array<NewOrUpdatedDiscovery> = []

  dataIn.listActiveDiscovery?.forEach((d) => {
    combined.push({
      id: d.details?.id,
      name: d.details?.name,
      tags: d.details?.tags,
      type: d.discoveryType,
      locations: locations.filter((b) => { return b.id === Number(d.details?.locationId) }),
      meta: {
        communityStrings: d?.details?.snmpConfig?.readCommunities.join(';') ?? '',
        ipRanges: d?.details?.ipAddresses?.join(';') ?? '',
        udpPorts: d?.details?.snmpConfig?.ports.join(';') ?? '',
        clientId: d?.details?.clientId,
        clientSecret: d?.details?.clientSecret ?? '',
        subscriptionId: d?.details?.subscriptionId,
        directoryId: d?.details?.directoryId
      }
    })
  })

  dataIn.passiveDiscoveries?.forEach((d) => {
    combined.push({
      id: d.id,
      name: d.name,
      tags: d.tags,
      locations: locations.filter((b) => { return b.id === Number(d.locationId) }),
      type: DiscoveryType.SyslogSNMPTraps,
      meta: {
        udpPorts: d?.snmpPorts?.join(';'),
        communityStrings: d?.snmpCommunities?.join(';'),
        toggle: { toggle: d.toggle, id: d.id }
      }
    })
  })

  combined = combined.sort(sortDiscoveriesByName)
  return combined
}

const activeDiscoveryValidation = yup.object().shape({
  name: yup.string().trim().required('Please enter a name.'),
  locationId: yup.string().trim().required('Location required.'),
  ipAddresses: yup.array().min(1, 'Please enter an ip address.').of(yup.string().required('Please enter an IP address.')
    .test('validate-ip',
      (ip, ctx) => {
        const matches = []
        const testIp = ip.trim()

        matches.push(REGEX_EXPRESSIONS.IP[0].test(testIp))
        matches.push(REGEX_EXPRESSIONS.IP[1].test(testIp))
        matches.push(REGEX_EXPRESSIONS.IP[2].test(testIp))

        // ip must pass one of the reg exprs
        if (!matches.some((val) => val === true)) {
          return ctx.createError({ message: 'Contains input that is not supported.', path: 'ipAddresses' })
        }

        return true
      })
  ),
  snmpConfig: yup.object({
    communityStrings: yup.array().of(yup.string().required('Please enter a community string.')),
    udpPorts: yup.array().of(yup.number())
  }).required('required')
}).required()

const passiveDiscoveryValidation = yup.object().shape({
  name: yup.string().trim().required('Please enter a name.'),
  locationId: yup.string().trim().required('Location required.'),
  snmpConfig: yup.object({
    communityStrings: yup.array().of(yup.string().required('Please enter a community string.')),
    udpPorts: yup.array().of(yup.number())
  }).required('required')
}).required()

const azureDiscoveryValidation = yup.object().shape({
  name: yup.string().trim().required('Please enter a name.'),
  locationId: yup.string().trim().required('Location required.'),
  clientId: yup.string().trim().required('Client ID is required.'),
  subscriptionId: yup.string().trim().required('Client subscription ID is required.'),
  directoryId: yup.string().trim().required('Directory ID is required.'),
  clientSecret: yup.string().trim().required('Client secret is required.')
}).required()

const WindowsServerDiscoveryValidation = yup.object().shape({
  name: yup.string().trim().required('Please enter a name.'),
  locationId: yup.string().trim().required('Location required.'),
  username: yup.string().trim().required('Username is required.'),
  password: yup.string().trim().required('Password is required.'),
  windowsProtocol: yup.string().trim().required('Windows Protocol is required.'),
  discoveryTarget: yup.string().trim().required('Client secret is required.')
}).required()

const ServiceDiscoveryValidation = yup.object().shape({
  name: yup.string().trim().required('Please enter a name.'),
  locationId: yup.string().trim().required('Location required.')
  
}).required()

const validatorMap: Record<string, yup.Schema> = {
  [DiscoveryType.Azure]: azureDiscoveryValidation,
  [DiscoveryType.ICMP]: activeDiscoveryValidation,
  [DiscoveryType.SyslogSNMPTraps]: passiveDiscoveryValidation,
  [DiscoveryType.WindowsServer]: WindowsServerDiscoveryValidation,
  [DiscoveryType.ServiceDiscovery]: ServiceDiscoveryValidation
}

// customValidator should throw { message, path } on validation error
export const clientToServerValidation = async (
  selectedDiscovery: NewOrUpdatedDiscovery,
  customValidator?: ((discovery: NewOrUpdatedDiscovery) => void)
) => {
  const type = selectedDiscovery.type ?? ''
  const validatorToUse = validatorMap[type] ?? {validate: () => ({})}

  let isValid = true
  let validationErrors = {}
  const convertedDiscovery = discoveryFromClientToServer(selectedDiscovery)

  try {
    if (customValidator) {
      customValidator(selectedDiscovery)
    }

    await validatorToUse.validate(convertedDiscovery, { abortEarly: false })
  } catch (e) {
    validationErrors = validationErrorsToStringRecord(e as yup.ValidationError)
    isValid = false
  }

  return { isValid, validationErrors }
}
