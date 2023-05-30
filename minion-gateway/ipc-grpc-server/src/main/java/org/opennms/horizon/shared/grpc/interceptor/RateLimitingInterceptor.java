package org.opennms.horizon.shared.grpc.interceptor;

import io.grpc.*;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.opennms.horizon.shared.constants.GrpcConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class RateLimitingInterceptor implements ServerInterceptor {
    private final Logger logger = LoggerFactory.getLogger(RateLimitingInterceptor.class);
//    private final RateLimitingService rateLimitingService;

//    @Autowired
    private Ignite ignite;

    public RateLimitingInterceptor(Ignite ignite) {
//        this.rateLimitingService = rateLimitingService;
        this.ignite = ignite;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        logger.debug("Received metadata: {}", headers);
        String tenantId = headers.get(GrpcConstants.TENANT_ID_REQUEST_KEY);
        IgniteCache<String, RateLimitingService> cache = ignite.getOrCreateCache("rateLimitingPolicy");
        logger.info("tryConsume: " + cache.get("opennms-prime").tryConsume());
        RateLimitingService rateLimitingService = cache.get(tenantId);

        if (!rateLimitingService.tryConsume()) {
            logger.info(">>>>>>>> Rate limit exceeded");
            call.close(Status.RESOURCE_EXHAUSTED.withDescription("Rate limit exceeded"), new Metadata());
            return new ServerCall.Listener<ReqT>() {};
        }

        return next.startCall(call, headers);
    }
}
