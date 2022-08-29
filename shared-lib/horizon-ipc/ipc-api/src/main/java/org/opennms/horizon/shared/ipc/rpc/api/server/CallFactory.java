package org.opennms.horizon.shared.ipc.rpc.api.server;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface CallFactory {

    <Request extends Message, Response extends Message>
    CallBuilder<Response> create(Request request, Deserializer<Any, Response> response);

    interface CallBuilder<T extends Message> {
        CallBuilder<T> withTimeToLive(long ttl);
        CallBuilder<T> withSystem(String system);
        CallBuilder<T> withModule(String module);
        CallBuilder<T> withLocation(String location);
        Call<T> build();
    }

    interface Call<T extends Message> {
        CompletableFuture<T> execute();
    }

    @FunctionalInterface
    interface Deserializer<T, R> {
        R apply(T input) throws IOException;
    }
}
