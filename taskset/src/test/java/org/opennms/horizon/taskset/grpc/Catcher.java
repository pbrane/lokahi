package org.opennms.horizon.taskset.grpc;

import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Catcher<T> implements StreamObserver<T> {

    private final Logger logger = LoggerFactory.getLogger(Catcher.class);

    private final CompletableFuture<T> future;
    private String name;

    public Catcher(String name) {
        this(new CompletableFuture<>(), name);
    }

    public Catcher(CompletableFuture<T> future, String name) {
        this.future = future;
        this.name = name;
    }

    @Override
    public void onNext(T value) {
        logger.info("Catcher {} received an element {} from stream", name, value);
        future.complete(value);
    }

    @Override
    public void onError(Throwable t) {
        logger.info("Catcher {} received error has been reported", name, t);
        future.completeExceptionally(t);
    }

    @Override
    public void onCompleted() {
        logger.info("Catcher {} completed", name);
    }

    public CompletableFuture<T> asFuture() {
        return future;
    }

}
