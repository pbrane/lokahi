package org.opennms.horizon.taskset.grpc;

import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Collector<T> implements StreamObserver<T> {

    private final Logger logger = LoggerFactory.getLogger(Collector.class);

    private final List<T> values = new CopyOnWriteArrayList<>();
    private String name;

    public Collector(String name) {
        this.name = name;
    }

    @Override
    public void onNext(T value) {
        logger.info("Collector {} received an element {} from stream", name, value);
        values.add(value);
    }

    @Override
    public void onError(Throwable t) {
        logger.info("Collector {} received error has been reported", name, t);
        throw new RuntimeException(t);
    }

    @Override
    public void onCompleted() {
        logger.info("Collector {} completed", name);
    }

    public List<T> getCollectedValues() {
        return values;
    }

}
