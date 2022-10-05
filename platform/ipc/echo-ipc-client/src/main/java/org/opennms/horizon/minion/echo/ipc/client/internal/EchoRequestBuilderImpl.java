package org.opennms.horizon.minion.echo.ipc.client.internal;

import java.util.concurrent.CompletableFuture;
import org.opennms.horizon.grpc.echo.contract.EchoRequest;
import org.opennms.horizon.grpc.echo.contract.EchoRequest.Builder;
import org.opennms.horizon.grpc.echo.contract.EchoResponse;
import org.opennms.horizon.minion.echo.ipc.client.EchoRequestBuilder;

public class EchoRequestBuilderImpl implements EchoRequestBuilder {

    private LocationAwareEchoClientImpl client;
    private Builder request;
    private String location;
    private String systemId;
    private Long timeToLive;

    public EchoRequestBuilderImpl(LocationAwareEchoClientImpl client) {
        this.client = client;
        this.request = EchoRequest.newBuilder();
    }

    @Override
    public EchoRequestBuilder withTime(long time) {
        request.setTime(time);
        return this;
    }

    @Override
    public EchoRequestBuilder withLocation(String location) {
        this.location = location;
        return this;
    }

    @Override
    public EchoRequestBuilder withSystemId(String systemId) {
        this.systemId = systemId;
        return this;
    }

    @Override
    public EchoRequestBuilder withMessage(String message) {
        request.setMessage(message);
        return this;
    }

    @Override
    public EchoRequestBuilder withTimeToLive(Long timeToLive) {
        this.timeToLive = timeToLive;
        return this;
    }

    @Override
    public CompletableFuture<EchoResponse> execute() {
        return client.execute(systemId, location, timeToLive, request.build());
    }
}
