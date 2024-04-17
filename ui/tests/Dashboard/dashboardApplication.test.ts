import { createClient, setActiveClient } from 'villus'
import mount from '../mountWithPiniaVillus'
import DashboardApplications from '@/components/Dashboard/DashboardApplications.vue'
import { useFlowsApplicationStore } from '@/store/Views/flowsApplicationStore'
import { useFlowsStore } from '@/store/Views/flowsStore'
import { TimeRange } from '@/types/graphql'

const wrapper = mount({
  component: DashboardApplications,
  shallow: false,
  stubActions: false
})

setActiveClient(
  createClient({
    url: 'http://test/graphql'
  })
)

const timeRange = { startTime: 1682025158863, endTime: 1682111558863 }
const filterValuesMock = {
  count: 10,
  exporter: [{ nodeId: 1, ipInterfaceId: 1 }],
  timeRange,
  applications: []
}

test('The DashboardApplications component mounts correctly', () => {
  expect(wrapper).toBeTruthy()
})

test('The DashboardApplications should call querie with paramters', () => {
  const store = useFlowsStore()
  const appStore = useFlowsApplicationStore()

  store.filters.dateFilter = TimeRange.Last_24Hours
  store.filters.selectedExporterTopApplication = { value: { nodeId: 1, ipInterfaceId: 1 } }
  const spyDate = vi.spyOn(store, 'getTimeRange')
  const spy = vi.spyOn(store, 'getRequestData')
  spyDate.mockReturnValue(timeRange)
  appStore.getApplicationDataset()
  expect(spy).toHaveBeenCalled()
  expect(spy).toHaveReturnedWith(filterValuesMock)
})

test('Resetting the flows date filter to Today', () => {
  const store = useFlowsStore()
  expect(store.filters.dateFilter).toBe(TimeRange.Last_24Hours)
  wrapper.unmount()
  expect(store.filters.dateFilter).toBe(TimeRange.Today)
})
