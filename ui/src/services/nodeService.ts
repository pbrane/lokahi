import { ListNodeMetricsDocument, ListNodesForTableDocument, TagCreateInput, TimeRangeUnit } from "@/types/graphql"
import { QueryService } from "."
import { Monitor } from "@/types"


export const getNodeDetails = async (name: string) => {
    const details = await QueryService.executeQuery({ query: ListNodesForTableDocument })
    const firstDetail = details?.findAllNodes?.[0]
    let metrics;
    if (firstDetail) {
        metrics = await QueryService.executeQuery({
            query: ListNodeMetricsDocument, variables: {
                id: firstDetail.id,
                instance: firstDetail.ipInterfaces[0].ipAddress,
                monitor: Monitor.ICMP, timeRange: 1, timeRangeUnit: TimeRangeUnit.Minute
            }
        })
    }
    return { detail: firstDetail, metrics }
}