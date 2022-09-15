package org.opennms.horizon.shared.ignite.remoteasync;

import java.util.concurrent.CompletableFuture;
import org.apache.ignite.services.Service;

public interface MinionRouterIgniteService extends Service {

    String IGNITE_SERVICE_NAME = "minionRouter";

    CompletableFuture<Boolean> sendDetectorRequestToMinionUsingId(String id);

    CompletableFuture<Boolean> sendDetectorRequestToMinionUsingLocation(String location);

    CompletableFuture<Boolean> sendEchoRequestToMinionUsingId(String id);

    CompletableFuture<Boolean> sendEchoRequestToMinionUsingLocation(String location);

    //TODO MMF: return type is proably not boolean
    CompletableFuture<Boolean> sendMonitorRequestToMinionUsingId(String id);

    CompletableFuture<Boolean> sendMonitorRequestToMinionUsingLocation(String location);

    void sendTwin(String location, String kind, Object payload);
}
