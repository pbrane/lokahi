package org.opennms.horizon.shared.ipc.rpc.api.client;

import com.google.protobuf.Message;

public interface CloudMessageReceiver {

    void handle(Message message);

    boolean canHandle(Message message);

}
