package org.opennms.miniongateway.grpc.server;

import org.opennms.core.ipc.grpc.server.manager.MinionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinionManagerConfig {
    @Bean("minionManager")
    public MinionManager localDetectorAdapter() {
        MinionManager minionManager =  new MinionManagerImpl();
        minionManager.addMinionListener(new MinionRegistrationCacheListener());
        return minionManager;
    }
}
