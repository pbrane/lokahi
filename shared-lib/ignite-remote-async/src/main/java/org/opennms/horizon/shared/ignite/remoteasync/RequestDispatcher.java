package org.opennms.horizon.shared.ignite.remoteasync;

import java.util.concurrent.CompletableFuture;

public interface RequestDispatcher {

    <T> CompletableFuture<T> dispatchToLocation(String location, Request request);
    <T> CompletableFuture<T> dispatchToMinion(String systemId, Request request);

    CompletableFuture<Void> sendToLocation(String location, Broadcast broadcast);
    CompletableFuture<Void> sendToMinion(String systemId, Broadcast broadcast);

}
