package org.opennms.horizon.shared.ipc.rpc.api;

import com.google.protobuf.Message;
import org.opennms.cloud.grpc.minion.RpcRequestProto;

public interface RequestBuilder {

    RequestBuilder withExpirationTime(long ttl);
    RequestBuilder withLocation(String location);
    RequestBuilder withSystemId(String systemId);
    RequestBuilder withPayload(Message payload);
    RpcRequestProto build();

}
