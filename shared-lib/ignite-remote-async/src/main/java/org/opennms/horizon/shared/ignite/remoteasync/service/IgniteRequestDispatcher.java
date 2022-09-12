package org.opennms.horizon.shared.ignite.remoteasync.service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteMessaging;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.services.Service;
import org.opennms.horizon.shared.ignite.remoteasync.Broadcast;
import org.opennms.horizon.shared.ignite.remoteasync.Request;
import org.opennms.horizon.shared.ignite.remoteasync.RequestDispatcher;

public class IgniteRequestDispatcher implements Service, RequestDispatcher {

    public static final String LOCATION = "location";
    public static final String SYSTEM_ID = "systemId";
    public static final String REQUEST = "request";
    public static final String BROADCAST = "broadcast";
    public static final String REPLY = "reply";
    @IgniteInstanceResource
    private transient Ignite ignite;

    private final Supplier<String> uuidGenerator = () -> UUID.randomUUID().toString();

    @Override
    public <T> CompletableFuture<T> dispatchToLocation(String location, Request request) {
        return request(location, LOCATION, request);
    }

    @Override
    public <T> CompletableFuture<T> dispatchToMinion(String systemId, Request request) {
        return request(systemId, SYSTEM_ID, request);
    }

    private <T> CompletableFuture<T> request(String coordinate, String coordinateType, Request request) {
        CompletableFuture<T> future = new CompletableFuture<>();

        IgniteBiPredicate<UUID, T> receiver = (uuid, value) -> {
            future.complete(value);
            // effectively we do not prologue our subscription
            return false;
        };

        String operationId = uuidGenerator.get();
        IgniteMessaging messaging = ignite.message();

        String replyTopic = REPLY + "/" + coordinateType + "/" + coordinate + "/" + operationId;
        messaging.localListen(replyTopic, receiver);
        messaging.send(REQUEST + "/" + coordinateType + "/" + coordinate, request.getPayload());

        return future.orTimeout(request.getTimeToLiveMs(), TimeUnit.MILLISECONDS).whenComplete((r, e) -> {
            if (e instanceof TimeoutException) {
                messaging.stopLocalListen(replyTopic, receiver);
            }
        });
    }

    @Override
    public CompletableFuture<Void> sendToLocation(String location, Broadcast broadcast) {
        return broadcast(location, LOCATION, broadcast);
    }

    @Override
    public CompletableFuture<Void> sendToMinion(String systemId, Broadcast broadcast) {
        return broadcast(systemId, LOCATION, broadcast);
    }

    private <T> CompletableFuture<T> broadcast(String coordinate, String coordinateType, Broadcast broadcast) {
        IgniteMessaging messaging = ignite.message();
        messaging.send(BROADCAST + "/" + coordinateType + "/" + broadcast.getType() + "/" + coordinate, broadcast.getPayload());
        // force hardcoded timeout in case if messaging hangs for any reason
        return CompletableFuture.completedFuture(null);
    }
}
