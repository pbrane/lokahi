package org.opennms.horizon.alarmservice;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import org.opennms.horizon.alarms.proto.AlarmServiceGrpc;

import java.util.concurrent.TimeUnit;

public class AlarmGrpcClientUtils {
    private static final int DEADLINE_DURATION = 30;
    private static final String LOCALHOST = "localhost";

    private final DynamicTenantIdInterceptor dynamicTenantIdInterceptor = new DynamicTenantIdInterceptor(
        // Pull private key directly from container
        CucumberRunnerIT.testContainerRunnerClassRule.getJwtKeyPair());
    private final AlarmServiceGrpc.AlarmServiceBlockingStub alarmServiceStub;

    public AlarmGrpcClientUtils() {
        alarmServiceStub = createGrpcConnectionForInventory();
    }

    private AlarmServiceGrpc.AlarmServiceBlockingStub createGrpcConnectionForInventory() {
        NettyChannelBuilder channelBuilder =
            NettyChannelBuilder.forAddress(LOCALHOST,
                // Pull gRPC server port directly from container
                CucumberRunnerIT.testContainerRunnerClassRule.getGrpcPort());

        ManagedChannel managedChannel = channelBuilder.usePlaintext().build();
        managedChannel.getState(true);
        return AlarmServiceGrpc.newBlockingStub(managedChannel)
            .withInterceptors(dynamicTenantIdInterceptor)
            .withDeadlineAfter(DEADLINE_DURATION, TimeUnit.SECONDS);
    }

    public void setTenantId(String tenantId) {
        dynamicTenantIdInterceptor.setTenantId(tenantId);
    }

    public AlarmServiceGrpc.AlarmServiceBlockingStub getAlarmServiceStub() {
        return alarmServiceStub;
    }

}
