package org.opennms.miniongateway.grpc.server.tasks;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.apache.ignite.IgniteException;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.compute.ComputeJob;
import org.apache.ignite.compute.ComputeJobResult;
import org.apache.ignite.compute.ComputeJobResultPolicy;
import org.apache.ignite.compute.ComputeTask;
import org.apache.ignite.compute.ComputeTaskName;
import org.apache.ignite.resources.SpringResource;
import org.jetbrains.annotations.NotNull;
import org.opennms.horizon.shared.ignite.remoteasync.MinionRouterService;
import org.opennms.horizon.shared.ignite.remoteasync.compute.RoutingRequest;
import org.opennms.miniongateway.detector.api.LocalEchoAdapter;

@ComputeTaskName("echoRoutingTask")
public class EchoRoutingTask implements ComputeTask<RoutingRequest, Object> {

    @SpringResource(resourceName = "minionRouterService")
    private MinionRouterService minionRouterService;

    @Override
    public @NotNull Map<? extends ComputeJob, ClusterNode> map(List<ClusterNode> subgrid, @Nullable RoutingRequest arg) throws IgniteException {
        UUID gatewayNodeId=null;
        switch (arg.getType() ) {
            case ID:
                gatewayNodeId = minionRouterService.findGatewayNodeWithId(arg.getValue());
                break;
            case LOCATION:
                gatewayNodeId = minionRouterService.findGatewayNodeWithLocation(arg.getValue());
                break;
        }
        RoutingJob job = new RoutingJob(arg.getValue(), null);
        Map<ComputeJob, ClusterNode> map = new HashMap<>();
        UUID finalGatewayNodeId = gatewayNodeId;
        ClusterNode node = subgrid.stream().filter(clusterNode -> finalGatewayNodeId.equals(clusterNode.id())).findFirst().get();
        map.put(job, node);
        return map;
    }

    @Override
    public ComputeJobResultPolicy result(ComputeJobResult res, List<ComputeJobResult> rcvd) throws IgniteException {
        if (rcvd.isEmpty()) {
            return ComputeJobResultPolicy.WAIT;
        }

        return ComputeJobResultPolicy.REDUCE;
    }

    @Override
    public @Nullable Object reduce(List<ComputeJobResult> results) throws IgniteException {
        return results.get(0);
    }

    @RequiredArgsConstructor
    private class RoutingJob implements ComputeJob {
        //TODO MMF: do we really need both of these? Or just one?
        private final String id;
        private final String location;
        
        @SpringResource(resourceName = "localEchoAdapter")
        private LocalEchoAdapter localEchoAdapter;

        @Override
        public void cancel() {

        }

        @Override
        public Object execute() throws IgniteException {
            System.out.println(LocalDateTime.now() +  "Routing task executing");
            return localEchoAdapter.echo(location, id);
        }
    }
}
