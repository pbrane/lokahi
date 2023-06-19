import { useNodeMutations } from "@/store/Mutations/nodeMutations"
import { TagCreateInput } from "@/types/graphql"

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