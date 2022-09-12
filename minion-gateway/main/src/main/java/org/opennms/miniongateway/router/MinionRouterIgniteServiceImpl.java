package org.opennms.miniongateway.router;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.cluster.ClusterGroup;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.resources.LoggerResource;
import org.apache.ignite.resources.ServiceContextResource;
import org.apache.ignite.resources.SpringResource;
import org.apache.ignite.services.ServiceContext;
import org.opennms.core.ipc.grpc.server.manager.MinionInfo;
import org.opennms.core.ipc.grpc.server.manager.MinionManager;
import org.opennms.core.ipc.grpc.server.manager.MinionManagerListener;
import org.opennms.horizon.shared.ignite.remoteasync.MinionRouterIgniteService;
import org.opennms.horizon.shared.ignite.remoteasync.manager.IgniteRemoteAsyncManager;
import org.opennms.miniongateway.detector.client.IgniteDetectorRemoteOperation;
import org.springframework.beans.factory.annotation.Autowired;

//TODO MMF: Break this into an ignite service with routing impl injected?
public class MinionRouterIgniteServiceImpl implements MinionRouterIgniteService {

    public static final String IGNITE_SERVICE_NAME = "minionRouter";
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
    public CompletableFuture<Boolean> sendToMinionUsingId(String id) {

        UUID foundMinion = minionByIdCache.get(id);

        return getBooleanCompletableFuture(foundMinion);
    }

    @Override
    public CompletableFuture<Boolean> sendToMinionUsingLocation(String location) {

        UUID foundMinion = minionByLocationCache.get(location);

        return getBooleanCompletableFuture(foundMinion);
    }

    private CompletableFuture<Boolean> getBooleanCompletableFuture(UUID foundMinion) {
        CompletableFuture<Boolean> future;

        if (foundMinion != null) {
            ClusterGroup clusterGroup = ignite.cluster().forNodeId(foundMinion);

            future = igniteRemoteAsyncManager.submit(clusterGroup, prepareRemoteOperation());
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

    private IgniteDetectorRemoteOperation prepareRemoteOperation() {
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
}
