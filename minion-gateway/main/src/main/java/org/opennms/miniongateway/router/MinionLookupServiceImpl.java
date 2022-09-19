package org.opennms.miniongateway.router;

import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.opennms.core.ipc.grpc.server.manager.MinionInfo;
import org.opennms.core.ipc.grpc.server.manager.MinionManagerListener;
import org.opennms.horizon.shared.ignite.remoteasync.MinionLookupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinionLookupServiceImpl implements MinionLookupService {

    public static final String MINIONS_BY_ID = "minionsById";
    public static final String MINIONS_BY_LOCATION = "minionsByLocation";

    private final Logger logger = LoggerFactory.getLogger(MinionLookupServiceImpl.class);

    private Ignite ignite;

    private IgniteCache<String, UUID> minionByIdCache;
    private IgniteCache<String, Queue<UUID>> minionByLocationCache;

    public MinionLookupServiceImpl(Ignite ignite) {
        logger.info("############ MINION ROUTER SERVICE INITIALIZED");

        minionByIdCache = ignite.getOrCreateCache(MINIONS_BY_ID);
        minionByLocationCache = ignite.getOrCreateCache(MINIONS_BY_LOCATION);
    }

    @Override
    public UUID findGatewayNodeWithId(String id) {
        return minionByIdCache.get(id);
    }

    @Override
    public Queue<UUID> findGatewayNodeWithLocation(String location) {
        return minionByLocationCache.get(location);
    }

    @Override
    public void onMinionAdded(long sequence, MinionInfo minionInfo) {

        UUID localUUID = ignite.cluster().localNode().id();

        minionByIdCache.put(minionInfo.getId(), localUUID);

        Queue<UUID> existingMinions = minionByLocationCache.get(minionInfo.getLocation());
        if (existingMinions.isEmpty()) {
            existingMinions = new ConcurrentLinkedDeque();
        }
        //TODO: for now, seems we can modify in place and not have to put this back in.
        existingMinions.add(localUUID);
    }

    @Override
    public void onMinionRemoved(long sequence, MinionInfo minionInfo) {

        UUID localUUID = ignite.cluster().localNode().id();

        minionByIdCache.remove(minionInfo.getId());
        minionByLocationCache.remove(minionInfo.getLocation());

        Queue<UUID> existingMinions = minionByLocationCache.get(minionInfo.getLocation());
        if (!existingMinions.isEmpty()) {
            existingMinions.remove(localUUID);
        }
    }
}
