import { useNodeMutations } from "@/store/Mutations/nodeMutations"
import { ListNodeMetricsDocument, ListNodesForTableDocument, TagCreateInput, TimeRangeUnit } from "@/types/graphql"
import { useQuery } from "villus"
import { QueryService } from "."
import { Monitor } from "@/types"

export const addTagsToNodes = async (id: number, tags: TagCreateInput[] | undefined) => {

    const mutations = useNodeMutations()

    return mutations.addTagsToNodes({
        nodeIds: [id],
        tags
    })
}

export const addDeviceToNode = async (deviceName: string, ip: string, locationName: string, tags: TagCreateInput[] | undefined) => {

    const mutations = useNodeMutations()

    return mutations.addNode({ // adding new device
        node: {
            label: deviceName,
            managementIp: ip,
            location: locationName,
            tags
        }
    })
}

export const getNodeDetails = async () => {
    const details = await QueryService.executeQuery({ query: ListNodesForTableDocument })
    const firstDetail = details?.findAllNodes?.[0]
    const metrics = await QueryService.executeQuery({
        query: ListNodeMetricsDocument, variables: {
            id: firstDetail.id,
            instance: firstDetail.ipInterfaces[0].ipAddress,
            monitor: Monitor.ICMP, timeRange: 1, timeRangeUnit: TimeRangeUnit.Minute
        }
    })

    console.log('FIRST PART OF NODE DETAILS!', details, metrics);
}