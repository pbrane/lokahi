package org.opennms.miniongateway.router;

import javax.annotation.PostConstruct;
import org.apache.ignite.Ignite;
import org.apache.ignite.lang.IgniteFuture;
import org.apache.ignite.lang.IgniteInClosure;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.opennms.horizon.shared.ignite.remoteasync.MinionRouterIgniteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinionRouterIgniteServiceConfig {

    @Autowired
    private Ignite ignite;

    @PostConstruct
    public void startService() {
        IgniteInClosure<? super IgniteFuture<Void>> closure = new IgniteInClosure<IgniteFuture<Void>>() {
            @Override
            public void apply(IgniteFuture<Void> future) {
                Logger logger = LoggerFactory.getLogger(MinionRouterIgniteServiceConfig.class);
                logger.info("Deployment of ignite router service. Done {}, cancelled {}", future.isDone(), future.isCancelled());
            }
        };
        ignite.services().deployNodeSingletonAsync(MinionRouterIgniteService.IGNITE_SERVICE_NAME, new MinionRouterIgniteServiceImpl()).listen(closure);
    }
}
