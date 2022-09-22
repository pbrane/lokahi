package org.opennms.miniongateway.grpc.server.tasks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;
import org.apache.ignite.IgniteException;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.compute.ComputeJob;
import org.apache.ignite.compute.ComputeJobResult;
import org.apache.ignite.compute.ComputeJobResultPolicy;
import org.apache.ignite.compute.ComputeTask;
import org.apache.ignite.compute.ComputeTaskName;
import org.apache.ignite.resources.LoggerResource;
import org.apache.ignite.resources.SpringResource;
import org.jetbrains.annotations.NotNull;
import org.opennms.cloud.grpc.minion.RpcRequestProto;
import org.opennms.cloud.grpc.minion.RpcResponseProto;
import org.opennms.horizon.shared.ignite.remoteasync.MinionLookupService;
import org.opennms.miniongateway.detector.server.IgniteRpcRequestDispatcher;

@ComputeTaskName(EchoRoutingTask.ECHO_ROUTING_TASK)
public class EchoRoutingTask implements ComputeTask<RpcRequestProto, RpcResponseProto> {

    public static final String ECHO_ROUTING_TASK = "echoRoutingTask";

    @SpringResource(resourceName = MinionLookupService.IGNITE_SERVICE_NAME)
    private transient MinionLookupService minionLookupService;

    @Override
    public @NotNull Map<? extends ComputeJob, ClusterNode> map(List<ClusterNode> subgrid, @Nullable RpcRequestProto arg) throws IgniteException {
        UUID gatewayNodeId = null;
        Map<ComputeJob, ClusterNode> map = new HashMap<>();

        if (!arg.getSystemId().isBlank()) {
            gatewayNodeId = minionLookupService.findGatewayNodeWithId(arg.getSystemId());
        } else {
            //TODO: For now just get the first one
            gatewayNodeId =  minionLookupService.findGatewayNodeWithLocation(arg.getLocation()).stream().findFirst().get();
        }
        //TODO: is it possible for this to be null? Or just assume there will always be at least one?
        if (gatewayNodeId != null) {
            RoutingJob job = new RoutingJob(arg);
            UUID finalGatewayNodeId = gatewayNodeId;
            ClusterNode node = subgrid.stream().filter(clusterNode -> finalGatewayNodeId.equals(clusterNode.id()))
                .findFirst().get();
            map.put(job, node);
        }
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
    public @Nullable RpcResponseProto reduce(List<ComputeJobResult> results) throws IgniteException {
        if (results.isEmpty()) {
            return null;
        }
        ComputeJobResult jobResult = results.get(0);
        if (jobResult.getException() != null) {
            throw jobResult.getException();
        }
        return jobResult.getData();
    }

    public static class RoutingJob implements ComputeJob {
        private final RpcRequestProto request;

        @LoggerResource
        private transient IgniteLogger logger;

        @SpringResource(resourceClass = IgniteRpcRequestDispatcher.class)
        private transient IgniteRpcRequestDispatcher requestDispatcher;

        private CompletableFuture<RpcResponseProto> responseFuture;

        public RoutingJob(RpcRequestProto request) {
            this.request = request;
        }

        @Override
        public void cancel() {
            if (responseFuture != null) {
                responseFuture.cancel(true);
            }
        }

        @Override
        public Object execute() throws IgniteException {
            try {
                if (logger.isTraceEnabled()) {
                    logger.trace("Dispatching RPC request " + request);
                } else if (logger.isDebugEnabled()) {
                    logger.debug("Dispatching rpc request " + request.getRpcId());
                }
                responseFuture = requestDispatcher.execute(request).whenComplete((response, error) -> {
                    if (error != null) {
                        logger.warning("Failure found while execution of " + request.getRpcId() + " " + error);
                        return;
                    }
                    if (logger.isTraceEnabled()) {
                        logger.trace("Received RPC response " + response);
                    } else if (logger.isDebugEnabled()) {
                        logger.debug("Received answer for rpc request " + request.getRpcId());
                    }
                });
                return responseFuture.get();
            } catch (InterruptedException e) {
                throw new IgniteException("Failed to dispatch request", e);
            } catch (ExecutionException e) {
                logger.warning("Failure while executing request " + request.getRpcId(), e);
                throw new IgniteException("Could not execute RPC request", e);
            }
        }
    }
}
