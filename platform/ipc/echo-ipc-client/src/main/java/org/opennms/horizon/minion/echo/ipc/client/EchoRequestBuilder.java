package org.opennms.horizon.minion.echo.ipc.client;

import java.util.concurrent.CompletableFuture;
import org.opennms.horizon.grpc.echo.contract.EchoResponse;

public interface EchoRequestBuilder {

    EchoRequestBuilder withTime(long time);
    EchoRequestBuilder withLocation(String location);
    EchoRequestBuilder withSystemId(String systemId);
    EchoRequestBuilder withMessage(String message);

    EchoRequestBuilder withTimeToLive(Long timeToLive);

    CompletableFuture<EchoResponse> execute();
}
