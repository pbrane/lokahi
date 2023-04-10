import mount from 'tests/mountWithPiniaVillus'
import AlertsCardList from '@/components/Alerts/AlertsCardList.vue'
import { getAlertsList } from '../fixture/alerts'

let wrapper: any

describe('Alerts list', () => {
  afterAll(() => {
    wrapper.unmount()
  })

  test('Mount', () => {
    wrapper = mount({
      component: AlertsCardList,
      props: {
        alerts: []
      }
    })

    expect(wrapper).toBeTruthy()
  })

  test('Should have alerts list if list not empty', async () => {
    wrapper = mount({
      component: AlertsCardList,
      props: {
        alerts: getAlertsList()
      }
    })

    const elem = wrapper.find('[data-test="alerts-list"]')
    expect(elem.exists()).toBeTruthy()
  })

  describe('Alerts list empty', () => {
    beforeAll(() => {
      wrapper = mount({
        component: AlertsCardList,
        props: {
          alerts: []
        }
      })
    })

    test('Should not have alerts list if list empty', () => {
      const elem = wrapper.find('[data-test="empty-list"]')
      expect(elem.exists()).toBeTruthy()
    })

    test('Should have a message', () => {
      const elem = wrapper.get('[data-test="msg"]')
      expect(elem.exists()).toBeTruthy()
    })

    test('Should have clear all filters button', async () => {
      const btn = wrapper.find('[data-test="clear-all-filters-btn"]')
      expect(btn.exists()).toBeTruthy()

      // TODO not working
      // const alertsStore = useAlertsStore()
      // const spy = vi.spyOn(alertsStore, 'clearAllFilters')
      // await btn.trigger('click')
      // expect(spy).toHaveBeenCalled()
    })
  })
})
