package org.opennms.miniongateway.ratelimiting;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.opennms.horizon.shared.grpc.interceptor.RateLimitingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("PolicyLoader")
public class PolicyLoader {

    private final Logger logger = LoggerFactory.getLogger(PolicyLoader.class);

    private PolicyProperties policyProperties;
    private Ignite ignite;

    public PolicyLoader(@Autowired PolicyProperties policyProperties, @Autowired Ignite ignite) {
        this.policyProperties = policyProperties;
        this.ignite = ignite;
    }
    @PostConstruct
    public void igniteCache() {

        IgniteCache<String, RateLimitingService> cache = ignite.getOrCreateCache("rateLimitingPolicy");
        Map<String, RateLimitingService> rateLimitingServices = new HashMap<>();
        List<PolicyProperties.Tenant> tenants = new ArrayList<>(); //TO-DO: policyProperties.getTenants(); it returns null.
        PolicyProperties.Tenant t1 = new PolicyProperties.Tenant();
        t1.setTenantID("opennms-prime");
        t1.setTraps(100);
        tenants.add(t1);
        tenants.forEach( (t) -> {
                int capacity = t.getTraps();
                int tokens = capacity;
                Bucket bucket = Bucket.builder()
                    .addLimit(Bandwidth.classic(capacity, Refill.intervally(tokens, Duration.ofMinutes(1))))
                    .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofSeconds(20))))
                    .build();
            rateLimitingServices.put(t.getTenantID(), new RateLimitingService(bucket));
            });
        cache.putAll(rateLimitingServices);

        logger.info("tryConsume: " + cache.get("opennms-prime").tryConsume());
    }
}

