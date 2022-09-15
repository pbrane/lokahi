package org.opennms.horizon.shared.ignite.remoteasync;

import java.util.UUID;

public interface MinionRouterService {

    String IGNITE_SERVICE_NAME = "minionRouter";

    UUID findGatewayNodeWithId(String id);

    UUID findGatewayNodeWithLocation(String location);

    void sendTwin(String location, String kind, Object payload);
}
