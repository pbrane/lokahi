package org.opennms.miniongateway.router;

import java.util.List;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.resources.LoggerResource;
import org.apache.ignite.resources.ServiceContextResource;
import org.apache.ignite.resources.SpringResource;
import org.apache.ignite.services.ServiceContext;
import org.opennms.core.ipc.grpc.server.manager.MinionInfo;
import org.opennms.core.ipc.grpc.server.manager.MinionManager;

public class MinionRouterImpl implements MinionRouter {

    public static final String MINIONS_BY_ID = "minionsById";
    public static final String MINIONS_BY_LOCATION = "minionsByLocation";

    @LoggerResource
    private IgniteLogger igniteLogger;

    @IgniteInstanceResource
    private Ignite ignite;

    @ServiceContextResource
    private ServiceContext serviceContext;

    @SpringResource(resourceName = "minionManager")
    MinionManager minionManager;

    private IgniteCache<String, MinionInfo> minionByIdCache;
    private IgniteCache<String, MinionInfo> minionByLocationCache;

    @Override
    public void sendToMinion(MinionInfo minionInfo) {
        List<MinionInfo> minionsList = minionManager.getMinions();

        MinionInfo foundMinion = minionsList.stream().filter(m1 -> m1.equals(minionInfo.getId())).findFirst().get();

        //TODO MMF: do we really just try both? Or is the request more specific?
        if (foundMinion == null) {
            foundMinion = minionByIdCache.get(minionInfo.getId());
        }
        else {
            foundMinion = minionByLocationCache.get(minionInfo.getLocation());
        }

        // TODO MMF: Now use that minion to send the message, probably a call to
        // IgniteDetectorRequestExecutor or IgniteRemoteAsyncManagerImpl???
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
}
