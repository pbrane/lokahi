package org.opennms.miniongateway.grpc.server;

import static org.opennms.miniongateway.router.MinionRouterIgniteServiceImpl.MINIONS_BY_ID;
import static org.opennms.miniongateway.router.MinionRouterIgniteServiceImpl.MINIONS_BY_LOCATION;

import java.util.UUID;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.opennms.core.ipc.grpc.server.manager.MinionInfo;
import org.opennms.core.ipc.grpc.server.manager.MinionManagerListener;

public class MinionRegistrationCacheListener implements MinionManagerListener {

    @IgniteInstanceResource
    private Ignite ignite;

    @Override
    public void onMinionAdded(long sequence, MinionInfo minionInfo) {

        // TODO MMF:should we do a getOrCreate() ?
        IgniteCache<String, UUID> minionByIdCache = ignite.cache(MINIONS_BY_ID);
        IgniteCache<String, UUID> minionByLocationCache = ignite.cache(MINIONS_BY_LOCATION);

        UUID localUUID = ignite.cluster().localNode().id();

        minionByIdCache.put(minionInfo.getId(), localUUID);
        minionByLocationCache.put(minionInfo.getLocation(), localUUID);
    }

    @Override
    public void onMinionRemoved(long sequence, MinionInfo minionInfo) {
        IgniteCache<String, MinionInfo> minionByIdCache = ignite.cache(MINIONS_BY_ID);
        IgniteCache<String, MinionInfo> minionByLocationCache = ignite.cache(MINIONS_BY_LOCATION);

        minionByIdCache.remove(minionInfo.getId());
        minionByLocationCache.remove(minionInfo.getLocation());
    }
}
