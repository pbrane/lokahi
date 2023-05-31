package org.opennms.horizon.shared.grpc.interceptor;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;

public class SerializableBucket implements Serializable {
    @QuerySqlField(index = true)
    private long capacity;
    @QuerySqlField(index = true)
    private long refillPeriodMillis;
    @QuerySqlField(index = true)
    private long refillTokens;

    private transient Bucket bucket;

    public SerializableBucket(long capacity, Duration refillPeriod, long refillTokens) {
        this.capacity = capacity;
        this.refillPeriodMillis = refillPeriod.toMillis();
        this.refillTokens = refillTokens;

//        bucket.addLimit(Bandwidth.classic(capacity, Refill.intervally(tokens, Duration.ofMinutes(1))))
//              .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofSeconds(20))))

        this.bucket = Bucket4j.builder()
            .addLimit(Bandwidth.classic(capacity, Refill.intervally(refillTokens, Duration.ofMillis(refillPeriodMillis))))
            .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofSeconds(20))))
            .build();
    }

    public boolean tryConsume(long tokens) {
        return bucket.tryConsume(tokens);
    }

    // Add any other methods you need for interacting with the bucket

    // Implement custom serialization and deserialization methods
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        bucket = Bucket4j.builder()
            .addLimit(Bandwidth.classic(capacity, Refill.intervally(refillTokens, Duration.ofMillis(refillPeriodMillis))))
            .build();
    }
}

