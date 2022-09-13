package org.opennms.horizon.shared.ignite.remoteasync;

import java.util.concurrent.CompletableFuture;
import org.apache.ignite.services.Service;

public interface MinionRouterIgniteService extends Service {

    CompletableFuture<Boolean> sendDetectorRequestToMinionUsingId(String id);

    CompletableFuture<Boolean> sendDetectorRequestToMinionUsingLocation(String location);

    CompletableFuture<Boolean> sendMonitorRequestToMinionUsingId(String id);

    CompletableFuture<Boolean> sendMonitorRequestToMinionUsingLocation(String location);

    //TODO MMF: return type is probably not boolean
    CompletableFuture<Boolean> sendEchoRequestToMinionUsingId(String id);

    CompletableFuture<Boolean> sendEchoRequestToMinionUsingLocation(String location);

}
