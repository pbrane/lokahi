package org.opennms.miniongateway.ignitedetector;

import org.opennms.core.ipc.grpc.server.manager.rpc.RpcProxyHandler;
import org.opennms.miniongateway.detector.api.LocalDetectorAdapter;
import org.opennms.miniongateway.detector.server.IgniteRpcRequestDispatcher;
import org.opennms.miniongateway.detector.server.LocalDetectorAdapterStubImpl;
import org.opennms.miniongateway.ignite.LocalIgniteRpcRequestDispatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IgniteDetectorConfig {
    @Bean("localDetectorAdapter")
    public LocalDetectorAdapter localDetectorAdapter() {
        return new LocalDetectorAdapterStubImpl();
    }

    @Bean("igniteRpcRequestDispatcher")
    public IgniteRpcRequestDispatcher requestDispatcher(RpcProxyHandler handler) {
        return new LocalIgniteRpcRequestDispatcher(handler);
    }

}
