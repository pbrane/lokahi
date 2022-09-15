package org.opennms.miniongateway.router;

import org.opennms.horizon.shared.ignite.remoteasync.MinionRouterService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinionRouterServiceConfig {
    @Bean("minionRouterService")
    public MinionRouterService minionRouterService() {
        return new MinionRouterServiceImpl();
    }
}
