package org.opennms.horizon.minion.opentelemetry.core.internal;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.CollectionRegistration;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.export.MetricProducer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

public class CompositeMetricReader implements MetricReader, MetricProducer {

    private final Set<CollectionRegistration> collectionRegistrations = new CopyOnWriteArraySet<>();
    private final List<MetricReader> readers = new CopyOnWriteArrayList<>();

    @Override
    public void register(CollectionRegistration collectionRegistration) {
        collectionRegistrations.add(collectionRegistration);
        readers.forEach(reader -> reader.register(collectionRegistration));
    }

    @Override
    public CompletableResultCode forceFlush() {
        return readers.stream().map(MetricReader::forceFlush)
            .collect(Collectors.collectingAndThen(Collectors.toList(), CompletableResultCode::ofAll));
    }

    @Override
    public CompletableResultCode shutdown() {
        return readers.stream().map(MetricReader::shutdown)
            .collect(Collectors.collectingAndThen(Collectors.toList(), CompletableResultCode::ofAll));
    }

    @Override
    public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
        return AggregationTemporality.CUMULATIVE;
    }

    @Override
    public Collection<MetricData> collectAllMetrics() {
        List<MetricData> metrics = new ArrayList<>();
        for (CollectionRegistration registration : collectionRegistrations) {
            if (registration instanceof MetricProducer) {
                metrics.addAll(((MetricProducer) registration).collectAllMetrics());
            }
        }
        return metrics;
    }

    public void registerReader(MetricReader reader) {
        this.readers.add(reader);
        this.collectionRegistrations.forEach(reader::register);
    }

    public void unregisterReader(MetricReader reader) {
        this.readers.remove(reader);
    }
}
