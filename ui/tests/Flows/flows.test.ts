import Flows from '@/containers/Flows.vue'
import mountWithPiniaVillus from 'tests/mountWithPiniaVillus'
import { useFlowsStore } from '@/store/Views/flowsStore'
import { TimeRange } from '@/types/graphql'
import { LineGraphData } from './flowsData'

describe('Flows', () => {
  let wrapper: any

  beforeAll(async () => {
    wrapper = await mountWithPiniaVillus({
      shallow: false,
      component: Flows,
      stubActions: false
    })
  })

  afterAll(() => {
    wrapper.unmount()
  })

  test('The Flows page container mounts correctly', () => {
    expect(wrapper).toBeTruthy()
  })

  test('The Flows page title should be Flows', () => {
    const title = wrapper.get('[data-test="flows-page-header"]')
    expect(title.text()).toBe('Flows')
  })

  test('The Flows Store randomColours() should return a Hex', () => {
    const store = useFlowsStore()
    //Get random colour user store method
    const randomHex = store.randomColours(0)
    function isHex(num: string) {
      return Boolean(num.match(/^#[0-9A-F]{6}$/i))
    }
    //Check colour is a Hex
    const isValueHex = isHex(randomHex)
    expect(isValueHex).toBeTruthy()

    function isRGBA(num: string) {
      return Boolean(
        num.match(
          /^rgba[(](?:\s*0*(?:\d\d?(?:\.\d+)?(?:\s*%)?|\.\d+\s*%|100(?:\.0*)?\s*%|(?:1\d\d|2[0-4]\d|25[0-5])(?:\.\d+)?)\s*,){3}\s*0*(?:\.\d+|1(?:\.0*)?)\s*[)]$/gm
        )
      )
    }
    const randomRGBA = store.randomColours(0, true)
    const isValueRGBA = isRGBA(randomRGBA)
    expect(isValueRGBA).toBeTruthy()
  })

  test('The Flows store getDataset and createCharts should be run onMount', () => {
    const store = useFlowsStore()
    expect(store.getDatasets).toHaveBeenCalledOnce()
    expect(store.getApplications).toHaveBeenCalledOnce()
    expect(store.createCharts).toHaveBeenCalledOnce()
  })

  test('The Flows page should have date radio buttons and can be clicked', async () => {
    const store = useFlowsStore()

    const dateSelector = wrapper.get('[data-test="text-radio-group-Date"]')
    const today = dateSelector.get('[data-test="text-radio-button-TODAY"]')
    const twentyFour = dateSelector.get('[data-test="text-radio-button-LAST_24_HOURS"]')
    const sevenDays = dateSelector.get('[data-test="text-radio-button-SEVEN_DAYS"]')

    //Ensure all 3 options are available
    expect(today.exists()).toBeTruthy()
    expect(twentyFour.exists()).toBeTruthy()
    expect(sevenDays.exists()).toBeTruthy()

    //Change selected from today to 24H
    expect(today.get('[aria-checked="true"]').exists()).toBeTruthy()
    await twentyFour.get('span.label').trigger('click')
    expect(store.onDateFilterUpdate).toHaveBeenCalledOnce()
    expect(twentyFour.get('[aria-checked="true"]').exists()).toBeTruthy()
  })

  test('The Flows store get time range should return starttime and endtime object', () => {
    const store = useFlowsStore()
    const returnedObject = store.getTimeRange(TimeRange.Today)
    const startTime = new Date(new Date().setHours(0, 0, 0, 0)).getTime()
    const expectedObject = { startTime: startTime, endTime: Date.now() }
    expect(returnedObject).toStrictEqual(expectedObject)
  })

  test('The Flows store convert to date should convert time range to string for labels', () => {
    const store = useFlowsStore()
    store.filters.dateFilter = TimeRange.Today
    const returnedTodayObject = store.convertToDate('2023-01-10T01:01:25Z')
    const expectedTodayString = '01:01'
    expect(returnedTodayObject).toStrictEqual(expectedTodayString)

    store.filters.dateFilter = TimeRange.Last_24Hours
    const returned24Object = store.convertToDate('2023-01-10T01:01:25Z')
    const expected24String = '01:01'
    expect(returned24Object).toStrictEqual(expected24String)

    store.filters.dateFilter = TimeRange.SevenDays
    const returnedSevenDayObject = store.convertToDate('2023-01-10T01:01:25Z')
    const expectedSevenDayString = '10/Jan 01:01'
    expect(returnedSevenDayObject).toStrictEqual(expectedSevenDayString)

    store.filters.dateFilter = TimeRange.All
    const returnedDefaultObject = store.convertToDate('2023-01-10T01:01:25Z')
    const expectedDefaultString = '10/Jan 01:01'
    expect(returnedDefaultObject).toStrictEqual(expectedDefaultString)
  })

  test('The Flows store createLine Chart should populate lineChartData', () => {
    const store = useFlowsStore()
    store.lineDatasets = LineGraphData
    expect(store.applications.lineChartData.datasets.length).toBe(0)
    store.applications.lineChartData
    store.createLineChartData()
    expect(store.applications.lineChartData.datasets.length).toBe(2)
  })
})
