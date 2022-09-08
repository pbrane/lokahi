package org.opennms.miniongateway.registry;

import org.apache.ignite.services.Service;
import org.opennms.core.ipc.grpc.server.manager.MinionInfo;

public interface MinionRouter extends Service {

    void sendToMinion(MinionInfo minionInfo);

}
