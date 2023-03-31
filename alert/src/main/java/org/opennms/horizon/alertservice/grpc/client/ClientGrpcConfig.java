package org.opennms.horizon.alertservice.grpc.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientGrpcConfig {

    @Value("${grpc.url.inventory}")
    private String inventoryGrpcAddress;
    @Value("${grpc.server.deadline:60000}")
    private long deadline;

    @Bean(name = "inventory")
    public ManagedChannel createInventoryChannel() {
        return ManagedChannelBuilder.forTarget(inventoryGrpcAddress)
            .keepAliveWithoutCalls(true)
            .usePlaintext().build();
    }

    @Bean(destroyMethod = "shutdown", initMethod = "initialStubs")
    public InventoryClient createInventoryClient(@Qualifier("inventory") ManagedChannel channel) {
        return new InventoryClient(channel, deadline);
    }
}
