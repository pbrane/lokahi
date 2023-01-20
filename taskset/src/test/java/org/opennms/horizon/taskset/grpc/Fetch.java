package org.opennms.horizon.taskset.grpc;

import io.grpc.stub.StreamObserver;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Fetch<T> implements StreamObserver<T> {

    private final Logger logger = LoggerFactory.getLogger(Fetch.class);
    private final CompletableFuture<T> future;

    public Fetch() {
        this(new CompletableFuture<>());
    }

    public Fetch(CompletableFuture<T> future) {
        this.future = future;
    }

    @Override
    public void onNext(T value) {
        future.complete(value);
    }

    @Override
    public void onError(Throwable t) {
        future.completeExceptionally(t);
    }

    @Override
    public void onCompleted() {
        logger.info("Operation has been completed");
    }

    public CompletableFuture<T> asFuture() {
        return future;
    }

}
