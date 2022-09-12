package org.opennms.miniongateway.router;

import javax.annotation.PostConstruct;
import org.apache.ignite.Ignite;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinionRouterIgniteServiceConfig {

    @Autowired
    private Ignite ignite;

    @PostConstruct
    public void startService() {
        ignite.services().deployNodeSingleton("minionRouter", new MinionRouterImpl());
    }
}
