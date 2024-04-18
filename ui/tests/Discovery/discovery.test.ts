import mount from '../mountWithPiniaVillus'
import Discovery from '@/containers/Discovery.vue'
import router from '@/router'
import { useDiscoveryStore } from '@/store/Views/discoveryStore'

let wrapper: any

describe('DiscoveryPage', () => {
  beforeAll(() => {
    wrapper = mount({
      component: Discovery,
      global: {
        plugins: [router]
      },
      stubActions: false
    })
  })

  afterAll(() => {
    wrapper.unmount()
  })

  test('The Discovery page container mounts correctly', () => {
    expect(wrapper).toBeTruthy()
  })

  test('Store fn startNewDiscovery', () => {
    const store = useDiscoveryStore()
    store.startNewDiscovery()
    expect(store.discoveryTypePageActive).toBe(true)
    expect(store.setupDefaultDiscovery).toHaveBeenCalled()
  })

  test('Store fn backToDiscovery', () => {
    const store = useDiscoveryStore()

    store.newDiscoveryModalActive = true
    store.validationErrors = { name: 'test' }
    store.selectedDiscovery = { name: 'Azure' }

    store.backToDiscovery()

    expect(store.newDiscoveryModalActive).toBe(false)
    expect(JSON.stringify(store.validationErrors)).toBe('{}')
    expect(JSON.stringify(store.selectedDiscovery)).toBe('{}')
  })

  test('Store fn cancelUpdate', async () => {
    const store = useDiscoveryStore()
    store.selectedDiscovery = { name: 'Azure' }
    store.discoveryFormActive = true
    store.discoveryTypePageActive = true

    await store.cancelUpdate()

    expect(JSON.stringify(store.selectedDiscovery)).toBe('{}')
    expect(store.discoveryFormActive).toBe(false)
    expect(store.discoveryTypePageActive).toBe(false)
  })

  test('Store fn setMetaSelectedDiscoveryValue', () => {
    const store = useDiscoveryStore()
    store.selectedDiscovery.meta = undefined as any
    store.setMetaSelectedDiscoveryValue('communityStrings', 'public')
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    expect(store.selectedDiscovery.meta.communityStrings).toBe('public')
  })

  test('Store fn applyDefaultLocation', () => {
    const store = useDiscoveryStore()

    expect(store.selectedDiscovery.locations).toBeFalsy()
    const defaultLocation = { id: 1, location: 'Default' }
    const locationsSingle = [defaultLocation]
    const locationsMultiple = [defaultLocation, { id: 2, location: 'Test' }]

    store.applyDefaultLocation(locationsMultiple, defaultLocation)
    expect(store.selectedDiscovery.locations).toBeFalsy()

    store.applyDefaultLocation(locationsSingle, defaultLocation)
    // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
    expect(store.selectedDiscovery.locations![0].location).toBe('Default')
  })
})
