package org.opennms.miniongateway.router;

import java.util.UUID;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.resources.LoggerResource;
import org.opennms.horizon.shared.ignite.remoteasync.MinionRouterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinionRouterServiceImpl implements MinionRouterService {

    public static final String MINIONS_BY_ID = "minionsById";
    public static final String MINIONS_BY_LOCATION = "minionsByLocation";

    private final Logger logger = LoggerFactory.getLogger(MinionRouterServiceImpl.class);

    private Ignite ignite;

    private IgniteCache<String, UUID> minionByIdCache;
    private IgniteCache<String, UUID> minionByLocationCache;

    public MinionRouterServiceImpl(Ignite ignite) {
        logger.info("############ MINION ROUTER SERVICE INITIALIZED");

        minionByIdCache = ignite.getOrCreateCache(MINIONS_BY_ID);
        minionByLocationCache = ignite.getOrCreateCache(MINIONS_BY_LOCATION);
    }

    @Override
    public UUID findGatewayNodeWithId(String id) {
        return minionByIdCache.get(id);
    }

    @Override
    public UUID findGatewayNodeWithLocation(String location) {
        return minionByLocationCache.get(location);
    }

    @Override
    @Deprecated
    public void sendTwin(String location, String kind, Object payload) {

    }

}
