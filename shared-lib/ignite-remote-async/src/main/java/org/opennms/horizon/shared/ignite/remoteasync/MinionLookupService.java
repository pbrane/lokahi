package org.opennms.horizon.shared.ignite.remoteasync;

import java.util.List;
import java.util.Queue;
import java.util.UUID;
import org.opennms.core.ipc.grpc.server.manager.MinionManagerListener;

public interface MinionLookupService extends MinionManagerListener {

    String IGNITE_SERVICE_NAME = "minionLookup";

    UUID findGatewayNodeWithId(String id);

    Queue<UUID> findGatewayNodeWithLocation(String location);
}
