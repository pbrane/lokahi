package org.opennms.miniongateway.router;

import org.apache.ignite.Ignite;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinionRouterConfig {

    @IgniteInstanceResource
    private Ignite ignite;

    public MinionRouterConfig() {
        ignite.services().deployNodeSingleton(MinionRouterIgniteServiceImpl.IGNITE_SERVICE_NAME, new MinionRouterIgniteServiceImpl());
    }
}
