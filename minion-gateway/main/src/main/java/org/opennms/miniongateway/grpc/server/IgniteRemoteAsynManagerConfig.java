package org.opennms.miniongateway.grpc.server;

import org.apache.ignite.Ignite;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.opennms.core.ipc.grpc.server.manager.MinionManager;
import org.opennms.horizon.shared.ignite.remoteasync.manager.IgniteRemoteAsyncManager;
import org.opennms.horizon.shared.ignite.remoteasync.manager.impl.IgniteRemoteAsyncManagerFactoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IgniteRemoteAsynManagerConfig {

    @IgniteInstanceResource
    private Ignite ignite;

    @Bean("igniteRemoteAsyncManager")
    public IgniteRemoteAsyncManager localDetectorAdapter() {
        IgniteRemoteAsyncManager igniteRemoteAsyncManager = new IgniteRemoteAsyncManagerFactoryImpl().create(ignite);

        return igniteRemoteAsyncManager;
    }
}
