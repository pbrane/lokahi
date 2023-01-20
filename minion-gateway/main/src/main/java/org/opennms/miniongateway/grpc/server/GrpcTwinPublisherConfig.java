package org.opennms.miniongateway.grpc.server;

import org.opennms.horizon.shared.grpc.common.TenantIDGrpcServerInterceptor;
import org.opennms.miniongateway.grpc.twin.GrpcTwinPublisher;
import org.opennms.miniongateway.grpc.twin.TwinRpcHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcTwinPublisherConfig {

    @Bean(initMethod = "start", destroyMethod = "close")
    public GrpcTwinPublisher grpcTwinPublisher(TenantIDGrpcServerInterceptor interceptor) {
        return new GrpcTwinPublisher(interceptor);
    }

    @Bean
    public ServerHandler serverHandler(GrpcTwinPublisher twinPublisher, TenantIDGrpcServerInterceptor interceptor) {
        return new TwinRpcHandler(twinPublisher, interceptor);
    }

}
