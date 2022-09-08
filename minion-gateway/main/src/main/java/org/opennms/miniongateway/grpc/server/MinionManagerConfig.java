package org.opennms.miniongateway.grpc.server;

import org.opennms.core.ipc.grpc.server.manager.MinionManager;
import org.opennms.miniongateway.detector.api.LocalDetectorAdapter;
import org.opennms.miniongateway.detector.server.LocalDetectorAdapterStubImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinionManagerConfig {
    //TODO: need to deploy the router service here?
    @Bean("minionManager")
    public MinionManager localDetectorAdapter() {
        return new MinionManagerImpl();
    }
}
