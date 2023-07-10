import { createTestingPinia } from '@pinia/testing'
import { getAlertsList } from '../../fixture/alerts'
import { useAlertsQueries } from '@/store/Queries/alertsQueries'
import { TimeRange } from '@/types/graphql'
import { AlertsFilters, Pagination } from '@/types/alerts'

describe('Alerts queries', () => {
  beforeEach(() => {
    createTestingPinia()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('fetchAlerts sets expected store value', async () => {
    const expectedAlerts = getAlertsList()

    vi.mock('villus', () => ({
      useQuery: vi.fn().mockImplementation(() => ({
        data: {
          value: {
            findAllAlerts: expectedAlerts
          }
        },
        isFetching: {
          value: false
        }
      }))
    }))

    const alertsQueries = useAlertsQueries()
    const alertsFilters: AlertsFilters = {
      nodeLabel: '', search: '', severities: [], sortAscending: false, sortBy: '', timeRange: TimeRange.All
    }
    const pagination: Pagination = {
      page: 1,
      pageSize: 10,
      total: 0
    }
    await alertsQueries.fetchAlerts(alertsFilters, pagination)
    expect(alertsQueries.fetchAlertsData).toStrictEqual(expectedAlerts)
  })

  it('fetchCountAlerts sets expected store value', async () => {
    const expectedResult = {
      count: 0
    }

    vi.mock('villus', () => ({
      useQuery: vi.fn().mockImplementation(() => ({
        data: {
          value: {
            countAlerts: expectedResult
          }
        },
        isFetching: {
          value: false
        }
      }))
    }))

    const alertsQueries = useAlertsQueries()
    await alertsQueries.fetchCountAlerts([], TimeRange.All)
    expect(alertsQueries.fetchCountAlertsData).toStrictEqual(expectedResult)
  })
})
