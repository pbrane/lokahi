import { TimeRange, RequestCriteriaInput } from '@/types/graphql'
import { IAutocompleteItemType } from '@featherds/autocomplete'
import { ChartData } from 'chart.js'

export const FlowsState = {
  tableDatasets: [{} as any],
  lineDatasets: [{} as any],
  tableChartOptions: {},
  totalFlows: '1,957',
  filters: {
    dateFilter: TimeRange.Today,
    traffic: {
      selectedItem: 'total'
    },
    dataStyle: {
      selectedItem: 'table'
    },
    //Application AutoComplete
    applications: [] as IAutocompleteItemType[],
    selectedApplications: [],
    isApplicationsLoading: false,
    filteredApplications: [] as IAutocompleteItemType[],
    //Exporter AutoComplete
    exporters: [] as IAutocompleteItemType[],
    selectedExporters: [],
    isExportersLoading: false,
    filteredExporters: [] as IAutocompleteItemType[]
  },
  applications: {
    isTableLoading: false,
    isLineLoading: false,
    tableChartData: {} as ChartData,
    lineChartData: {} as ChartData,
    expansionOpen: true,
    filterDialogOpen: false
  },
  exporters: {
    isLoading: false,
    tableChartData: {} as ChartData,
    lineChartData: {} as ChartData,
    expansionOpen: true,
    filterDialogOpen: false
  },
  requestCriteria: {
    count: 10,
    step: 2000000,
    timeRange: { startTime: 0, endTime: 0 },
    applications: [] as string[],
    exporters: []
  } as RequestCriteriaInput
}

export const LineGraphData = [
  {
    label: 'app0',
    data: [
      {
        timestamp: '2023-01-10T01:01:25Z',
        value: 138790.1750375429,
        direction: 'EGRESS'
      },
      {
        timestamp: '2023-01-10T01:09:47Z',
        value: 216861.9119974472,
        direction: 'EGRESS'
      },
      {
        timestamp: '2023-01-10T01:18:07Z',
        value: 202966.96568806906,
        direction: 'EGRESS'
      },
      {
        timestamp: '2023-01-10T01:26:27Z',
        value: 264173.0346710393,
        direction: 'EGRESS'
      }
    ]
  },
  {
    label: 'app1',
    data: [
      {
        timestamp: '2023-01-10T01:01:25Z',
        value: 138790.1750375429,
        direction: 'EGRESS'
      },
      {
        timestamp: '2023-01-10T01:09:47Z',
        value: 216861.9119974472,
        direction: 'EGRESS'
      },
      {
        timestamp: '2023-01-10T01:18:07Z',
        value: 202966.96568806906,
        direction: 'EGRESS'
      },
      {
        timestamp: '2023-01-10T01:26:27Z',
        value: 264173.0346710393,
        direction: 'EGRESS'
      }
    ]
  }
]
