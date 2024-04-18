import { createTestingPinia } from '@pinia/testing'
import { useNodeStatusQueries } from '@/store/Queries/nodeStatusQueries'
import { eventsFixture } from '../../fixture/events'

describe('Events queries', () => {
  beforeEach(() => {
    createTestingPinia()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('fetched events, nodes and metrics', () => {
    vi.mock('villus', () => ({
      useQuery: vi.fn().mockImplementation(() => ({
        data: {
          value: {
            nextPage: 1,
            lastPage: 1,
            totalEvents: 0,
            eventsList: eventsFixture()
          }
        }
      }))
    }))

    const nodeStatusQueries = useNodeStatusQueries()
    nodeStatusQueries.setNodeId(1)

    const expectedFetchedData = {
      nextPage: 1,
      lastPage: 1,
      totalEvents: 0,
      eventsList: eventsFixture()
    }

    nodeStatusQueries.fetchedEventsByNodeData = expectedFetchedData
    expect(nodeStatusQueries.fetchedEventsByNodeData).toStrictEqual(expectedFetchedData)
  })
})
