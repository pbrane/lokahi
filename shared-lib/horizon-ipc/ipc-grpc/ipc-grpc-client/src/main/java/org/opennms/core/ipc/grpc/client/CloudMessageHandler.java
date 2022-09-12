package org.opennms.core.ipc.grpc.client;

import org.opennms.cloud.grpc.minion.CloudToMinionMessage;

public interface CloudMessageHandler {

    void handle(CloudToMinionMessage message);

}
