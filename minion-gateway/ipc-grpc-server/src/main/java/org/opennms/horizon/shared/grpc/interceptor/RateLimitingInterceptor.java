package org.opennms.horizon.shared.grpc.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.Status;
import io.grpc.ServerInterceptor;

public class RateLimitingInterceptor implements ServerInterceptor {
    private final Logger logger = LoggerFactory.getLogger(RateLimitingInterceptor.class);
    private final RateLimitingService rateLimitingService;

    public RateLimitingInterceptor(RateLimitingService rateLimitingService) {
        this.rateLimitingService = rateLimitingService;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        logger.info("Received metadata:" + headers);
        if (!rateLimitingService.tryConsume()) {
            logger.info(">>>>>>>> Rate limit exceeded");
            call.close(Status.RESOURCE_EXHAUSTED.withDescription("Rate limit exceeded"), new Metadata());
            return new ServerCall.Listener<ReqT>() {};
        }

        return next.startCall(call, headers);
    }
}
