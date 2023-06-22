import { Monitor } from "@/types";
import { ListNodeMetricsDocument, ListNodesForTableDocument, TimeRangeUnit } from "@/types/graphql";
import { defineStore } from "pinia";
import { useQuery } from "villus";

export const useNodeQueries = defineStore('nodeQueries', () => {

    const getNodeDetails = async (name: string) => {

        const { execute: getDetails } = useQuery({
            query: ListNodesForTableDocument,
            cachePolicy: 'network-only',
            fetchOnMount: false
        })
        const details = await getDetails();
        const firstDetail = details?.data?.findAllNodes?.[0]
        let metrics;
        if (firstDetail) {
            const { execute: getMetrics } = useQuery({
                query: ListNodeMetricsDocument,
                cachePolicy: 'network-only',
                fetchOnMount: false,
                variables: {
                    id: firstDetail.id,
                    instance: firstDetail.ipInterfaces?.[0].ipAddress || '',
                    monitor: Monitor.ICMP, timeRange: 1, timeRangeUnit: TimeRangeUnit.Minute
                }
            })
            metrics = await getMetrics();
        }
        return { detail: firstDetail, metrics: metrics?.data }
    }
    return { getNodeDetails }
})