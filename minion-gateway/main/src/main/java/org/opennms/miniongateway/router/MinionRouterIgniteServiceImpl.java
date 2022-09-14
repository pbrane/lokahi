package org.opennms.miniongateway.router;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.cluster.ClusterGroup;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.resources.LoggerResource;
import org.apache.ignite.resources.ServiceContextResource;
import org.apache.ignite.services.ServiceContext;
import org.opennms.horizon.shared.ignite.remoteasync.MinionRouterIgniteService;
import org.opennms.horizon.shared.ignite.remoteasync.manager.IgniteRemoteAsyncManager;
import org.opennms.horizon.shared.ignite.remoteasync.manager.model.RemoteOperation;
import org.opennms.miniongateway.detector.client.IgniteDetectorRemoteOperation;
import org.opennms.miniongateway.detector.client.IgniteEchoRemoteOperation;
import org.springframework.beans.factory.annotation.Autowired;

//TODO MMF: Break this into an ignite service with routing impl injected?
public class MinionRouterIgniteServiceImpl implements MinionRouterIgniteService {

    public static final String MINIONS_BY_ID = "minionsById";
    public static final String MINIONS_BY_LOCATION = "minionsByLocation";

    @LoggerResource
    private IgniteLogger igniteLogger;

    @IgniteInstanceResource
    private Ignite ignite;

    @ServiceContextResource
    private ServiceContext serviceContext;

    @Autowired
    private IgniteRemoteAsyncManager igniteRemoteAsyncManager;

    private IgniteCache<String, UUID> minionByIdCache;
    private IgniteCache<String, UUID> minionByLocationCache;

    @Override
    public CompletableFuture<Boolean> sendDetectorRequestToMinionUsingId(String id) {
        if (id == null) {
            return CompletableFuture.completedFuture(false);
        }
        UUID foundMinion = minionByIdCache.get(id);

        return executeRemoteOperation(foundMinion, buildDetectorRemoteOperation());
    }

    @Override
    public CompletableFuture<Boolean> sendDetectorRequestToMinionUsingLocation(String location) {
        if (location == null) {
            return CompletableFuture.completedFuture(false);
        }
        UUID nodeId = minionByLocationCache.get(location);

        return executeRemoteOperation(nodeId, buildDetectorRemoteOperation());
    }

    @Override
    public CompletableFuture<Boolean> sendMonitorRequestToMinionUsingId(String id) {
        return CompletableFuture.failedFuture(new Exception("not implemented"));
    }

    @Override
    public CompletableFuture<Boolean> sendMonitorRequestToMinionUsingLocation(String location) {
        return CompletableFuture.failedFuture(new Exception("not implemented"));
    }

    @Override
    public void sendTwin(String location, String kind, Object payload) {

    }

    @Override
    public CompletableFuture<Boolean> sendEchoRequestToMinionUsingId(String id) {
        UUID nodeId = minionByIdCache.get(id);

        return executeRemoteOperation(nodeId, buildEchoRemoteOperation());
    }

    @Override
    public CompletableFuture<Boolean> sendEchoRequestToMinionUsingLocation(String location) {
        UUID nodeId = minionByLocationCache.get(location);

        return executeRemoteOperation(nodeId, buildEchoRemoteOperation());
    }

    private CompletableFuture<Boolean> executeRemoteOperation(UUID nodeId, RemoteOperation remoteOperation) {
        CompletableFuture<Boolean> future;

        if (nodeId != null) {
            ClusterGroup clusterGroup = ignite.cluster().forNodeId(nodeId);

            future = igniteRemoteAsyncManager.submit(clusterGroup, remoteOperation);
        }
        else {
            future = CompletableFuture.completedFuture(false);
        }

        return future;
    }

    @Override
    public void cancel() {
        igniteLogger.info("MINION ROUTER SERVICE STOPPED");
    }

    @Override
    public void init() throws Exception {
        igniteLogger.info("############ MINION ROUTER SERVICE INITIALIZED");

        minionByIdCache = ignite.getOrCreateCache(MINIONS_BY_ID);
        minionByLocationCache = ignite.getOrCreateCache(MINIONS_BY_LOCATION);
    }

    @Override
    public void execute() throws Exception {

    }

    private IgniteDetectorRemoteOperation buildDetectorRemoteOperation() {
        IgniteDetectorRemoteOperation result = new IgniteDetectorRemoteOperation();
        //TODO MMF: pass these in on the route call
//        result.setLocation(location);
//        result.setSystemId(systemId);
//        result.setServiceName(serviceName);
//        result.setDetectorName(detectorName);
//        result.setAddress(address);
//        result.setAttributes(attributes);
//        result.setNodeId(nodeId);

        return result;
    }

    private IgniteEchoRemoteOperation buildEchoRemoteOperation() {
        return new IgniteEchoRemoteOperation();
    }
}
