package org.opennms.horizon.shared.ignite.remoteasync;

import java.util.concurrent.CompletableFuture;
import org.apache.ignite.services.Service;
import org.opennms.core.ipc.grpc.server.manager.MinionInfo;

public interface MinionRouterIgniteService extends Service {

    CompletableFuture<Boolean> sendToMinionUsingId(String id);

    CompletableFuture<Boolean> sendToMinionUsingLocation(String location);

}
