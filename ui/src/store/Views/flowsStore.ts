import { flowsAppDataToChartJS } from '@/dtos/chartJS.dto'
import { format } from 'date-fns'
import { defineStore } from 'pinia'
import { useflowsQueries } from '@/store/Queries/flowsQueries'
import { RequestCriteriaInput, TimeRange } from '@/types/graphql'
import { FlowsApplicationData, FlowsApplicationSummaries, ChartData } from '@/types'
import { IAutocompleteItemType } from '@featherds/autocomplete/src/components/types'

export const useFlowsStore = defineStore('flowsStore', {
  state: () => ({
    tableDatasets: [{} as any],
    lineDatasets: [{} as any],
    topApplications: [] as FlowsApplicationSummaries[],
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
  }),
  actions: {
    async getDatasets() {
      const flowsQueries = useflowsQueries()
      const requestData = {
        count: 10,
        step: 2000000,
        timeRange: this.getTimeRange(this.filters.dateFilter),
        applications: this.filters.selectedApplications.map((app: any) => app.value)
      } as RequestCriteriaInput

      //Get Table Data
      this.applications.isTableLoading = true
      const applicationTableData = await flowsQueries.getApplicationsSummaries(requestData)
      this.tableDatasets = [
        ...((applicationTableData.value?.findApplicationSummaries as FlowsApplicationSummaries[]) || [])
      ]
      this.applications.isTableLoading = false

      //Get Line Graph Data
      this.applications.isLineLoading = true
      const applicationsLineData = await flowsQueries.getApplicationsSeries(requestData)
      this.lineDatasets = applicationsLineData.value?.findApplicationSeries
        ? [...flowsAppDataToChartJS(applicationsLineData.value?.findApplicationSeries as FlowsApplicationData[])]
        : []
      this.applications.isLineLoading = false
    },
    async getApplications() {
      const flowsQueries = useflowsQueries()
      const requestData = {
        count: 50,
        step: 2000000,
        timeRange: this.getTimeRange(this.filters.dateFilter)
      } as RequestCriteriaInput

      //Get Applications Data
      const applications = (await flowsQueries.getApplications(requestData)) || []
      const applicationsAutocompleteObject = applications.value?.findApplications?.map((item: string) => ({
        _text: item.toUpperCase(),
        value: item
      })) as IAutocompleteItemType[]
      this.filters.applications = applicationsAutocompleteObject
    },

    createTableChartData() {
      if (this.tableDatasets) {
        this.exporters.tableChartData = {
          labels: this.tableDatasets.map((row) => row.label),
          datasets: [
            {
              label: 'Inbound',
              data: this.tableDatasets.map((row) => row.bytesIn),
              barThickness: 13,
              backgroundColor: '#0043A4',
              hidden: this.filters.traffic.selectedItem === 'outbound'
            },
            {
              label: 'Outbound',
              data: this.tableDatasets.map((row) => row.bytesOut),
              barThickness: 13,
              backgroundColor: '#EE7D00',
              hidden: this.filters.traffic.selectedItem === 'inbound'
            }
          ]
        }
      }
      if (this.tableDatasets) {
        this.applications.tableChartData = {
          labels: this.tableDatasets.map((row) => row.label),
          datasets: [
            {
              label: 'Inbound',
              data: this.tableDatasets.map((row) => row.bytesIn),
              barThickness: 13,
              backgroundColor: '#0043A4',
              hidden: this.filters.traffic.selectedItem === 'outbound'
            },
            {
              label: 'Outbound',
              data: this.tableDatasets.map((row) => row.bytesOut),
              barThickness: 13,
              backgroundColor: '#EE7D00',
              hidden: this.filters.traffic.selectedItem === 'inbound'
            }
          ]
        }
      }
    },
    createLineChartData() {
      if (this.lineDatasets) {
        const datasetArr = {
          type: 'line',
          datasets: this.lineDatasets.map((element, index) => {
            return {
              label: element.label,
              data: element.data.map((data: any) => {
                return {
                  x: this.convertToDate(data.timestamp),
                  y: data.value
                }
              }),
              fill: true,
              borderColor: this.randomColours(index),
              backgroundColor: this.randomColours(index, true)
            }
          })
        }
        this.applications.lineChartData = datasetArr
        this.exporters.lineChartData = datasetArr
      }
    },
    async updateCharts() {
      await this.getApplications()
      await this.getDatasets()
      this.createCharts()
    },
    createCharts() {
      this.createTableChartData()
      this.createLineChartData()
    },
    async trafficRadioOnChange(selectedItem: string) {
      this.filters.traffic.selectedItem = selectedItem
      await this.updateCharts()
    },
    convertToDate(ts: string) {
      const dateFormat = () => {
        switch (this.filters.dateFilter) {
          case TimeRange.Today:
            return 'HH:mm'
          case TimeRange.Last_24Hours:
            return 'HH:mm'
          case TimeRange.SevenDays:
            return 'dd/MMM HH:mm'
          default:
            return 'dd/MMM HH:mm'
        }
      }
      return format(new Date(ts), dateFormat())
    },
    async onDateFilterUpdate(e: any) {
      this.filters.dateFilter = e
      await this.updateCharts()
    },
    getTimeRange(range: string) {
      const now = new Date()
      let startTime
      switch (range) {
        case TimeRange.Today:
          startTime = new Date(new Date().setHours(0, 0, 0, 0)).getTime()
          return { startTime: startTime, endTime: Date.now() }
        case TimeRange.Last_24Hours:
          startTime = now.setDate(now.getDate() - 1)
          return { startTime: startTime, endTime: Date.now() }
        case TimeRange.SevenDays:
          startTime = now.setDate(now.getDate() - 7)
          return { startTime: startTime, endTime: Date.now() }
        default:
          startTime = new Date(new Date().setHours(0, 0, 0, 0)).getTime()
          return { startTime: startTime, endTime: Date.now() }
      }
    },
    applicationsAutoCompleteSearch(searchString: string) {
      let timeout = -1
      this.filters.isApplicationsLoading = true
      clearTimeout(timeout)
      timeout = window.setTimeout(() => {
        this.filters.filteredApplications = this.filters.applications
          .filter((x: any) => x._text.toLowerCase().indexOf(searchString) > -1)
          .map((x: any) => ({
            value: x.value,
            _text: x._text
          }))
        this.filters.isApplicationsLoading = false
      }, 500)
    },
    exportersAutoCompleteSearch(searchString: string) {
      let timeout = -1
      this.filters.isExportersLoading = true
      clearTimeout(timeout)
      timeout = window.setTimeout(() => {
        this.filters.filteredExporters = this.filters.exporters
          .filter((x: any) => x._text.toLowerCase().indexOf(searchString) > -1)
          .map((x: any) => ({
            value: x.value,
            _text: x._text
          }))
        this.filters.isExportersLoading = false
      }, 500)
    },
    // This method is needed as currently on update of chart data, new data values are not being assigned a colour.
    randomColours(index: number, opacity = false) {
      const defaultColors = [
        '#3366CC',
        '#DC3912',
        '#FF9900',
        '#109618',
        '#990099',
        '#3B3EAC',
        '#0099C6',
        '#DD4477',
        '#66AA00',
        '#B82E2E',
        '#316395',
        '#994499',
        '#22AA99',
        '#AAAA11',
        '#6633CC',
        '#E67300',
        '#8B0707',
        '#329262',
        '#5574A6',
        '#651067'
      ]
      const addOpacity = function (hex: string, opacity: number) {
        const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
        return result
          ? 'rgba(' +
              parseInt(result[1], 16) +
              ', ' +
              parseInt(result[2], 16) +
              ', ' +
              parseInt(result[3], 16) +
              ', ' +
              opacity +
              ')'
          : hex
      }
      if (opacity) {
        return addOpacity(defaultColors[index], 0.3)
      } else {
        return defaultColors[index]
      }
    },
    async getApplicationDataset() {
      const flowsQueries = useflowsQueries()
      const requestData = {
        count: 10,
        step: 2000000,
        timeRange: this.getTimeRange(TimeRange.Last_24Hours)
      } as RequestCriteriaInput

      const topApplications = await flowsQueries.getApplicationsSummaries(requestData)
      this.topApplications = [
        ...((topApplications.value?.findApplicationSummaries as FlowsApplicationSummaries[]) || [])
      ]
    }
  }
})
