package org.opennms.horizon.shared.grpc.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.io.Serializable;

public class RateLimitingService implements Serializable {

    @Serial
    private static final long serialVersionUID = 6754783666280381320L;
    private final Logger logger = LoggerFactory.getLogger(RateLimitingService.class);
    private final SerializableBucket bucket;

    public RateLimitingService(SerializableBucket bucket) {
        this.bucket = bucket;
    }

    public boolean tryConsume() {
        return bucket.tryConsume(1);
    }

    public boolean tryConsume(long numTokens) {
        return bucket.tryConsume(numTokens);
    }
}
