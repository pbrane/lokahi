package org.opennms.miniongateway.router;

import javax.annotation.PostConstruct;
import org.apache.ignite.Ignite;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.opennms.horizon.shared.ignite.remoteasync.MinionRouterIgniteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinionRouterIgniteServiceConfig {

    @Autowired
    private Ignite ignite;

    @PostConstruct
    public void startService() {
        ignite.services().deployNodeSingleton(MinionRouterIgniteService.IGNITE_SERVICE_NAME, new MinionRouterIgniteServiceImpl());
    }
}
